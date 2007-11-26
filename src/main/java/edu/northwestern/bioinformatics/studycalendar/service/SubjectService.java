package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Canceled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.DatedScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.NotApplicable;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
// TODO: split out schedule-specific parts into ScheduleService
@Transactional
public class SubjectService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public static final String RESCHEDULED = "Rescheduled. ";

    private SubjectDao subjectDao;
    private SiteService siteService;

    public StudySubjectAssignment assignSubject(Subject subject, StudySite study, StudySegment studySegmentOfFirstEpoch, Date startDate, User subjectCoordinator) {
        return this.assignSubject(subject, study, studySegmentOfFirstEpoch, startDate, null, subjectCoordinator);
    }

    public StudySubjectAssignment assignSubject(Subject subject, StudySite studySite, StudySegment studySegmentOfFirstEpoch, Date startDate, String assignmentGridIdentifier, User subjectCoordinator) {
        StudySubjectAssignment spa = new StudySubjectAssignment();
        spa.setSubject(subject);
        spa.setStudySite(studySite);
        spa.setStartDateEpoch(startDate);
        spa.setGridId(assignmentGridIdentifier);
        spa.setSubjectCoordinator(subjectCoordinator);
        spa.setCurrentAmendment(studySite.getStudy().getAmendment());
        subject.addAssignment(spa);
        scheduleStudySegment(spa, studySegmentOfFirstEpoch, startDate, NextStudySegmentMode.PER_PROTOCOL);
        subjectDao.save(subject);
        return spa;
    }

    public List<StudySubjectAssignment> getAssignedStudySubject(String userName, List<StudySubjectAssignment> assignments) {
        List<StudySubjectAssignment> actualAssignments = new ArrayList<StudySubjectAssignment>();
        List<Site> sites =  new ArrayList<Site>(siteService.getSitesForSubjectCoordinator(userName));
        for (StudySubjectAssignment assignment : assignments) {
            for (Site site : sites) {
                if (site.getId() == assignment.getStudySite().getSite().getId())
                    actualAssignments.add(assignment);
            }
        }
        return actualAssignments;
    }

    // TODO: should take SPA#currentAmendment into account when scheduling.
    // That's out of scope for construction 2, though.  RMS20071023.
    public ScheduledStudySegment scheduleStudySegment(
        StudySubjectAssignment assignment, StudySegment studySegment, Date startDate, NextStudySegmentMode mode
    ) {
        if (assignment.getEndDateEpoch() != null) return null;
        ScheduledCalendar calendar = assignment.getScheduledCalendar();
        if (calendar == null) {
            calendar = new ScheduledCalendar();
            assignment.setScheduledCalendar(calendar);
        }

        if (mode == NextStudySegmentMode.IMMEDIATE) {
            String cancellationReason = "Immediate transition to " + studySegment.getQualifiedName();
            for (ScheduledStudySegment existingStudySegment : calendar.getScheduledStudySegments()) {
                existingStudySegment.unscheduleOutstandingEvents(cancellationReason);
            }
        }

        Amendment sourceAmendment = assignment.getCurrentAmendment();
        Integer studySegmentStartDay = studySegment.getDayRange().getStartDay();

        ScheduledStudySegment scheduledStudySegment = new ScheduledStudySegment();
        scheduledStudySegment.setStudySegment(studySegment);
        scheduledStudySegment.setStartDate(startDate);
        scheduledStudySegment.setStartDay(studySegmentStartDay);
        calendar.addStudySegment(scheduledStudySegment);

        for (Period period : studySegment.getPeriods()) {
            schedulePeriod(period, sourceAmendment, scheduledStudySegment);
        }

        // Sort in the same order they'll be coming out of the database (for consistency)
        Collections.sort(scheduledStudySegment.getEvents(), DatabaseEventOrderComparator.INSTANCE);

        Site site = calendar.getAssignment().getStudySite().getSite();
        avoidBlackoutDates(scheduledStudySegment, site);
        subjectDao.save(assignment.getSubject());

        return scheduledStudySegment;
    }

    /**
     * Derives scheduled events from the given period and applies them to the given scheduled studySegment.
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public void schedulePeriod(Period period, Amendment sourceAmendment, ScheduledStudySegment targetStudySegment) {
        log.debug("Adding events from period {}", period);
        for (PlannedActivity plannedActivity : period.getPlannedActivities()) {
            schedulePlannedActivity(plannedActivity, period, sourceAmendment, targetStudySegment);
        }
    }

    /**
     * Derives one repetition's worth of scheduled events from the given period and applies them to
     * the given scheduled studySegment.
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public void schedulePeriod(
        Period period, Amendment sourceAmendment, ScheduledStudySegment targetStudySegment, int repetitionNumber
    ) {
        log.debug("Adding events for rep {} from period {}", repetitionNumber, period);
        for (PlannedActivity plannedActivity : period.getPlannedActivities()) {
            schedulePlannedActivity(plannedActivity, period, sourceAmendment, targetStudySegment, repetitionNumber);
        }
    }

    /**
     * Derives scheduled events from the given planned event and applies them to the given scheduled studySegment.
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public void schedulePlannedActivity(
        PlannedActivity plannedActivity, Period period, Amendment sourceAmendment, ScheduledStudySegment targetStudySegment
    ) {
        for (int r = 0 ; r < period.getRepetitions() ; r++) {
            schedulePlannedActivity(plannedActivity, period, sourceAmendment, targetStudySegment, r);
        }
    }

    /**
     * Derives a single scheduled event from the given planned event and applies it to the given scheduled studySegment.
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public void schedulePlannedActivity(
        PlannedActivity plannedActivity, Period period, Amendment sourceAmendment, ScheduledStudySegment targetStudySegment,
        int repetitionNumber
    ) {
        log.debug("Adding event {} from planned activity {}", repetitionNumber, plannedActivity);

        // amount needed to shift the relative days in the period such that
        // the relative day 0 falls on studySegmentStateDate.  E.g., if the studySegment starts on
        // day 1, the normalizationFactor needs to shift everything down 1.
        // if it starts on -7, it needs to shift everything up 7.
        int normalizationFactor = targetStudySegment.getStartDay() * -1;

        int repOffset = normalizationFactor + period.getStartDay() + period.getDuration().getDays() * repetitionNumber;
        log.debug(" - rep {}; offset: {}", repetitionNumber, repOffset);
        ScheduledActivity event = createEmptyScheduledActivityFor(plannedActivity);
        event.setRepetitionNumber(repetitionNumber);
        event.setIdealDate(idealDate(repOffset + plannedActivity.getDay(), targetStudySegment.getStartDate()));

        DatedScheduledActivityState initialState
            = (DatedScheduledActivityState) plannedActivity.getInitialScheduledMode().createStateInstance();
        initialState.setReason("Initialized from template");
        initialState.setDate(event.getIdealDate());
        event.changeState(initialState);

        event.setDetails(plannedActivity.getDetails());
        event.setActivity(plannedActivity.getActivity());
        event.setSourceAmendment(sourceAmendment);

        targetStudySegment.addEvent(event);
    }

    // factored out to allow tests to use the logic in the schedule* methods on semimock instances
    protected ScheduledActivity createEmptyScheduledActivityFor(PlannedActivity plannedActivity) {
        ScheduledActivity event = new ScheduledActivity();
        event.setPlannedActivity(plannedActivity);
        return event;
    }

    private void avoidBlackoutDates(ScheduledStudySegment studySegment, Site site) {
       List<ScheduledActivity> listOfEvents = studySegment.getEvents();
        for (ScheduledActivity event : listOfEvents) {
            avoidBlackoutDates(event, site);
        }
    }

    public void avoidBlackoutDates(ScheduledActivity event) {
        avoidBlackoutDates(event, event.getScheduledStudySegment().getScheduledCalendar().getAssignment().getStudySite().getSite());
    }

    private void avoidBlackoutDates(ScheduledActivity event, Site site) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Calendar dateCalendar = Calendar.getInstance();
        Date date = event.getActualDate();
        dateCalendar.setTime(date);

        int year = dateCalendar.get(Calendar.YEAR);
        Calendar holidayCalendar = Calendar.getInstance();
        List<Holiday> holidayList = site.getHolidaysAndWeekends();

        for (Holiday holiday: holidayList) {
            // TODO: instanceof indicates abstraction failure -- this logic should be in each BlackoutDate class
            if (holiday instanceof MonthDayHoliday) {
                //month needs to be decremented, because we are using 00 for January in the Calendar
                MonthDayHoliday h = (MonthDayHoliday)holiday;
                if (h.getYear() == null) {
                    holidayCalendar.set(year, h.getMonth(), h.getDay());
                } else {
                    holidayCalendar.set(h.getYear(), h.getMonth(), h.getDay());
                }
                String originalDateFormatted = df.format(date.getTime());
                String holidayDateFormatted = df.format(holidayCalendar.getTime());
                if (originalDateFormatted.equals(holidayDateFormatted)) {
                    shiftToAvoidBlackoutDate(date, event, site, holiday.getDescription());
                }
            } else if(holiday instanceof DayOfTheWeek) {
                DayOfTheWeek dayOfTheWeek = (DayOfTheWeek) holiday;
                int intValueOfTheDay = dayOfTheWeek.getDayOfTheWeekInteger();
                if (dateCalendar.get(Calendar.DAY_OF_WEEK) == intValueOfTheDay) {
                    shiftToAvoidBlackoutDate(date, event, site, holiday.getDescription());
                }
            } else if (holiday instanceof RelativeRecurringHoliday) {
                RelativeRecurringHoliday relativeRecurringHoliday =
                        (RelativeRecurringHoliday) holiday;
                Integer numberOfTheWeek = relativeRecurringHoliday.getWeekNumber();
                Integer month = relativeRecurringHoliday.getMonth();
                int dayOfTheWeekInt = relativeRecurringHoliday.getDayOfTheWeekInteger();
                Calendar c = Calendar.getInstance();

                try {
                    c.setTime(
                        new SimpleDateFormat("dd/MM/yyyy").parse("01/" + month + '/' + year));
                    List<Date> dates = findRecurringHoliday(c, dayOfTheWeekInt);
                    //-1, since we start from position 0 in the list
                    Date specificDay = dates.get(numberOfTheWeek-1);

                    String originalDateFormatted = df.format(date.getTime());
                    String holidayDateFormatted = df.format(specificDay);
                    if (originalDateFormatted.equals(holidayDateFormatted)) {
                        shiftToAvoidBlackoutDate(date, event, site, holiday.getDescription());
                    }
                } catch (ParseException e) {
                    throw new StudyCalendarSystemException(e);
                }
            }
        }
    }

    // package level for testing
    void shiftToAvoidBlackoutDate(Date date, ScheduledActivity event, Site site, String reason) {
        date = shiftDayByOne(date);
        Scheduled s = new Scheduled(RESCHEDULED + reason, date);
        event.changeState(s);
        avoidBlackoutDates(event, site);
    }

    // package level for testing
    //looking for a specific day = dayOfTheWeek (like Monday, or Tuesday)
    List<Date> findRecurringHoliday(Calendar cal, int dayOfTheWeek) {
        List<Date> weekendList = new ArrayList<Date>();
        for (int i = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
             i < cal.getActualMaximum(Calendar.DAY_OF_MONTH) ; i ++) {
            int dayOfTheWeekCal = cal.get(Calendar.DAY_OF_WEEK) ;
            if (dayOfTheWeekCal == dayOfTheWeek) {
                weekendList.add(cal.getTime());
            }
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return weekendList;
    }

    // package level for testing
    Date shiftDayByOne(Date date) {
        java.sql.Timestamp timestampTo = new java.sql.Timestamp(date.getTime());
        long oneDay = 24 * 60 * 60 * 1000;
        timestampTo.setTime(timestampTo.getTime() + oneDay);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = df.format(timestampTo);
        Date d1;
        try {
            d1 = df.parse(dateString);
        } catch (ParseException e) {
            log.debug("Exception " + e);
            d1 = date;
        }
        return d1;
    }

    private Date idealDate(int studySegmentDay, Date startDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);

        cal.add(Calendar.DAY_OF_YEAR, studySegmentDay - 1);
        return cal.getTime();
    }

    public StudySubjectAssignment takeSubjectOffStudy(StudySubjectAssignment studyAssignment, Date offStudyDate) {
        ScheduledCalendar calendar = studyAssignment.getScheduledCalendar();

        List<ScheduledActivity> upcomingScheduledActivities = getPotentialUpcomingEvents(calendar, offStudyDate);

        for(ScheduledActivity event: upcomingScheduledActivities) {
            if (ScheduledActivityMode.SCHEDULED == event.getCurrentState().getMode()) {
                event.changeState(new Canceled("Off Study"));
            } else if (ScheduledActivityMode.CONDITIONAL == event.getCurrentState().getMode()) {
                event.changeState(new NotApplicable("Off Study"));
            }
        }

        studyAssignment.setEndDateEpoch(offStudyDate);
        subjectDao.save(studyAssignment.getSubject());
        return studyAssignment;
    }

    private List<ScheduledActivity> getPotentialUpcomingEvents(ScheduledCalendar calendar, Date offStudyDate) {
        List<ScheduledActivity> upcomingScheduledActivities = new ArrayList<ScheduledActivity>();
        for (ScheduledStudySegment studySegment : calendar.getScheduledStudySegments()) {
            if (!studySegment.isComplete()) {
                Map<Date, List<ScheduledActivity>> eventsByDate = studySegment.getEventsByDate();
                for(Date date: eventsByDate.keySet()) {
                    List<ScheduledActivity> events = eventsByDate.get(date);
                    for(ScheduledActivity event : events) {
                        if ((offStudyDate.before(event.getActualDate()) || offStudyDate.equals(event.getActualDate()))
                                && (ScheduledActivityMode.SCHEDULED == event.getCurrentState().getMode()
                                || ScheduledActivityMode.CONDITIONAL == event.getCurrentState().getMode())) {
                            upcomingScheduledActivities.add(event);
                        }
                    }
                }
            }
        }
        return upcomingScheduledActivities;
    }

    ////// CONFIGURATION

    public void setSubjectDao(SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private static class DatabaseEventOrderComparator implements Comparator<ScheduledActivity> {
        public static final Comparator<? super ScheduledActivity> INSTANCE = new DatabaseEventOrderComparator();

        public int compare(ScheduledActivity e1, ScheduledActivity e2) {
            int dateCompare = e1.getIdealDate().compareTo(e2.getIdealDate());
            if (dateCompare != 0) return dateCompare;

            if (e1.getPlannedActivity() == null && e2.getPlannedActivity() == null) {
                return 0;
            } else if (e1.getPlannedActivity() == null) {
                return -1;
            } else if (e2.getPlannedActivity() == null) {
                return 1;
            }

            return e1.getPlannedActivity().getId().compareTo(e2.getPlannedActivity().getId());
        }
    }
}
