package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.DayOfTheWeek;
import edu.northwestern.bioinformatics.studycalendar.domain.Holiday;
import edu.northwestern.bioinformatics.studycalendar.domain.MonthDayHoliday;
import edu.northwestern.bioinformatics.studycalendar.domain.NextArmMode;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.RelativeRecurringHoliday;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Canceled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Conditional;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.NotAvailable;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Transactional
public class ParticipantService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public static final String RESCHEDULED = "Rescheduled. ";

    private ParticipantDao participantDao;
    private SiteService siteService;

    public StudyParticipantAssignment assignParticipant(Participant participant, StudySite study, Arm armOfFirstEpoch, Date startDate, User participantCoordinator) {
        return this.assignParticipant(participant, study, armOfFirstEpoch, startDate, null, participantCoordinator);
    }

    public StudyParticipantAssignment assignParticipant(Participant participant, StudySite studySite, Arm armOfFirstEpoch, Date startDate, String assignmentGridIdentifier, User participantCoordinator) {
        StudyParticipantAssignment spa = new StudyParticipantAssignment();
        spa.setParticipant(participant);
        spa.setStudySite(studySite);
        spa.setStartDateEpoch(startDate);
        spa.setGridId(assignmentGridIdentifier);
        spa.setParticipantCoordinator(participantCoordinator);
        spa.setCurrentAmendment(studySite.getStudy().getAmendment());
        participant.addAssignment(spa);
        scheduleArm(spa, armOfFirstEpoch, startDate, NextArmMode.PER_PROTOCOL);
        participantDao.save(participant);
        return spa;
    }

    public List<StudyParticipantAssignment> getAssignedStudyParticipant(String userName, List<StudyParticipantAssignment> assignments) {
        List<StudyParticipantAssignment> actualAssignments = new ArrayList<StudyParticipantAssignment>();
        List<Site> sites =  new ArrayList<Site>(siteService.getSitesForParticipantCoordinator(userName));
        for (StudyParticipantAssignment assignment : assignments) {
            for (Site site : sites) {
                if (site.getId() == assignment.getStudySite().getSite().getId())
                    actualAssignments.add(assignment);
            }
        }
        return actualAssignments;
    }

    // TODO: should take SPA#currentAmendment into account when scheduling.
    // That's out of scope for construction 2, though.  RMS20071023.
    public ScheduledArm scheduleArm(
        StudyParticipantAssignment assignment, Arm arm, Date startDate, NextArmMode mode
    ) {
        if (assignment.getEndDateEpoch() != null) return null;
        ScheduledCalendar calendar = assignment.getScheduledCalendar();
        if (calendar == null) {
            calendar = new ScheduledCalendar();
            assignment.setScheduledCalendar(calendar);
        }

        if (mode == NextArmMode.IMMEDIATE) {
            String cancellationReason = "Immediate transition to " + arm.getQualifiedName();
            for (ScheduledArm existingArm : calendar.getScheduledArms()) {
                for (ScheduledEvent event : existingArm.getEvents()) {
                    if (event.getCurrentState().getMode() == ScheduledEventMode.SCHEDULED) {
                        event.changeState(new Canceled(cancellationReason));
                    }
                }
            }
        }

        ScheduledArm scheduledArm = new ScheduledArm();
        scheduledArm.setArm(arm);
        calendar.addArm(scheduledArm);
        int normalizationFactor = arm.getDayRange().getStartDay() * -1 + 1;

        for (Period period : arm.getPeriods()) {
            for (PlannedEvent plannedEvent : period.getPlannedEvents()) {
                for (Integer armDay : plannedEvent.getDaysInArm()) {
                    // TODO: I think we might need to track which repetition an event is from
                    ScheduledEvent event = new ScheduledEvent();
                    event.setIdealDate(idealDate(armDay + normalizationFactor, startDate));
                    event.setPlannedEvent(plannedEvent);
                    if (plannedEvent.getConditionalDetails() == null || plannedEvent.getConditionalDetails().length()<0) {
                        //setting mode to conditional
                        event.changeState(new Scheduled("Initialized from template", event.getIdealDate()));
                    } else {
                        event.changeState(new Conditional("Initialized from template", event.getIdealDate()));
                    }
                    event.setDetails(plannedEvent.getDetails());
                    event.setActivity(plannedEvent.getActivity());
                    event.setSourceAmendment(assignment.getCurrentAmendment());
                    scheduledArm.addEvent(event);
                }
            }
        }

        // Sort in the same order they'll be coming out of the database (for consistency)
        Collections.sort(scheduledArm.getEvents(), new Comparator<ScheduledEvent>() {
            public int compare(ScheduledEvent e1, ScheduledEvent e2) {
                int dateCompare = e1.getIdealDate().compareTo(e2.getIdealDate());
                if (dateCompare != 0) return dateCompare;

                if (e1.getPlannedEvent() == null && e2.getPlannedEvent() == null)
                    return 0;
                else if (e1.getPlannedEvent() == null)
                    return -1;
                else if (e2.getPlannedEvent() == null)
                    return 1;

                return e1.getPlannedEvent().getId().compareTo(e2.getPlannedEvent().getId());
            }
        });

        Site site = calendar.getAssignment().getStudySite().getSite();
        avoidBlackoutDates(scheduledArm, site);
        participantDao.save(assignment.getParticipant());

        return scheduledArm;
    }

    private void avoidBlackoutDates(ScheduledArm arm, Site site) {
       List<ScheduledEvent> listOfEvents = arm.getEvents();
        for (ScheduledEvent event : listOfEvents) {
            avoidBlackoutDates(event, site);
        }
    }

    private void avoidBlackoutDates(ScheduledEvent event, Site site) {
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
    void shiftToAvoidBlackoutDate(Date date, ScheduledEvent event, Site site, String reason) {
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
        long oneDay = 1 * 24 * 60 * 60 * 1000;
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

    private Date idealDate(int armDay, Date startDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);

        cal.add(Calendar.DAY_OF_YEAR, armDay - 1);
        return cal.getTime();
    }

    public StudyParticipantAssignment takeParticipantOffStudy(StudyParticipantAssignment studyAssignment, Date offStudyDate) {
        ScheduledCalendar calendar = studyAssignment.getScheduledCalendar();

        List<ScheduledEvent> upcomingScheduledEvents = getPotentialUpcomingEvents(calendar, offStudyDate);

        for(ScheduledEvent event: upcomingScheduledEvents) {
            if (ScheduledEventMode.SCHEDULED == event.getCurrentState().getMode()) {
                event.changeState(new Canceled("Off Study"));
            } else if (ScheduledEventMode.CONDITIONAL == event.getCurrentState().getMode()) {
                event.changeState(new NotAvailable("Off Study"));
            }
        }

        studyAssignment.setEndDateEpoch(offStudyDate);
        participantDao.save(studyAssignment.getParticipant());
        return studyAssignment;
    }

    private List<ScheduledEvent> getPotentialUpcomingEvents(ScheduledCalendar calendar, Date offStudyDate) {
        List<ScheduledEvent> upcomingScheduledEvents = new ArrayList<ScheduledEvent>();
        for (ScheduledArm arm : calendar.getScheduledArms()) {
            if (!arm.isComplete()) {
                Map<Date, List<ScheduledEvent>> eventsByDate = arm.getEventsByDate();
                for(Date date: eventsByDate.keySet()) {
                    List<ScheduledEvent> events = eventsByDate.get(date);
                    for(ScheduledEvent event : events) {
                        if ((offStudyDate.before(event.getActualDate()) || offStudyDate.equals(event.getActualDate()))
                                && (ScheduledEventMode.SCHEDULED == event.getCurrentState().getMode()
                                || ScheduledEventMode.CONDITIONAL == event.getCurrentState().getMode())) {
                            upcomingScheduledEvents.add(event);
                        }
                    }
                }
            }
        }
        return upcomingScheduledEvents;
    }

    ////// CONFIGURATION

    public void setParticipantDao(ParticipantDao participantDao) {
        this.participantDao = participantDao;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
}
