package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.BlackoutDate;
import edu.northwestern.bioinformatics.studycalendar.domain.Gender;
import edu.northwestern.bioinformatics.studycalendar.domain.NextStudySegmentMode;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.RelativeRecurringBlackout;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.SpecificDateBlackout;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.WeekdayBlackout;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Canceled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.NotApplicable;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.Registration;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
// TODO: split out schedule-specific parts into ScheduleService
@Transactional
public class SubjectService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public static final String RESCHEDULED = "Rescheduled. ";

    private SubjectDao subjectDao;
    private AmendmentService amendmentService;

    public StudySubjectAssignment assignSubject(StudySite studySite, Registration registration) {
        return this.assignSubject(
            registration.getSubject(), studySite, registration.getFirstStudySegment(),
            registration.getDate(), registration.getDesiredStudySubjectAssignmentId(),
            registration.getStudySubjectId(), registration.getPopulations(),
            registration.getStudySubjectCalendarManager());
    }

    private StudySubjectAssignment assignSubject(
        Subject subject, StudySite studySite,
        StudySegment studySegmentOfFirstEpoch, Date startDate,
        String assignmentGridIdentifier,
        String studySubjectId, Set<Population> populations,
        PscUser manager
    ) {
        Amendment currentAmendment = studySite.getCurrentApprovedAmendment();
        if (currentAmendment == null) {
            throw new StudyCalendarSystemException("The template for %s has not been approved by %s",
                studySite.getStudy().getName(), studySite.getSite().getName());
        }

        populations = (populations == null) ? Collections.<Population>emptySet() : populations;

        StudySubjectAssignment spa = new StudySubjectAssignment();
        spa.setSubject(subject);
        spa.setStudySite(studySite);
        spa.setStartDate(startDate);
        spa.setGridId(assignmentGridIdentifier);
        spa.setStudySubjectCalendarManager(manager == null ? null : manager.getCsmUser());
        spa.setCurrentAmendment(currentAmendment);
        spa.setStudySubjectId(studySubjectId);
        spa.setPopulations(populations);
        subject.addAssignment(spa);
        scheduleStudySegment(spa, studySegmentOfFirstEpoch, startDate, NextStudySegmentMode.PER_PROTOCOL);
        subjectDao.save(subject);
        return spa;
    }

    public ScheduledStudySegment scheduleStudySegment(
        StudySubjectAssignment assignment, StudySegment studySegment, Date startDate, NextStudySegmentMode mode
    ) {
        if (assignment.getEndDate() != null) return null;
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
        StudySegment amendedSegment = amendmentService.getAmendedNode(studySegment, sourceAmendment);

        if (amendedSegment == null) {
            throw new StudyCalendarSystemException
                    ("Could not find a node %s in the target study", studySegment);
        }
        Integer studySegmentStartDay = studySegment.getDayRange().getStartDay();

        ScheduledStudySegment scheduledStudySegment = new ScheduledStudySegment();
        scheduledStudySegment.setStudySegment(studySegment);
        scheduledStudySegment.setStartDate(startDate);
        scheduledStudySegment.setStartDay(studySegmentStartDay);
        calendar.addStudySegment(scheduledStudySegment);

        for (Period period : amendedSegment.getPeriods()) {
            schedulePeriod(period, sourceAmendment, "Initialized from template", scheduledStudySegment);
        }

        // Sort in the same order they'll be coming out of the database (for consistency)
        Collections.sort(scheduledStudySegment.getActivities(), DatabaseEventOrderComparator.INSTANCE);

        Site site = calendar.getAssignment().getStudySite().getSite();
        avoidBlackoutDates(scheduledStudySegment, site);
        subjectDao.save(assignment.getSubject());

        return scheduledStudySegment;
    }

    /**
     * Derives Scheduled StudySegment for the preview.
     */
    public void scheduleStudySegmentPreview(ScheduledCalendar calendar,StudySegment studySegment, Date startDate) {
        log.debug("Appending {} to preview starting at {}", studySegment, startDate);
        Integer studySegmentStartDay = studySegment.getDayRange().getStartDay();

        ScheduledStudySegment scheduledStudySegment = new ScheduledStudySegment();
        scheduledStudySegment.setStudySegment(studySegment);
        scheduledStudySegment.setStartDate(startDate);
        scheduledStudySegment.setStartDay(studySegmentStartDay);
        calendar.addStudySegment(scheduledStudySegment);

        for (Period period : studySegment.getPeriods()) {
            schedulePeriod(period, null, "Initialized from template", scheduledStudySegment);
        }

        // Sort in the same order they'll be coming out of the database (for consistency)
        Collections.sort(scheduledStudySegment.getActivities(), DatabaseEventOrderComparator.INSTANCE);
    }

    /**
     * Derives scheduled events from the given period and applies them to the given scheduled studySegment.
     * <p>
     * The input period should already match the provided sourceAmendment.
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public void schedulePeriod(Period period, Amendment sourceAmendment, String reason, ScheduledStudySegment targetStudySegment) {
        schedulePeriod(period, sourceAmendment, reason, null, targetStudySegment);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    protected void schedulePeriod(Period period, Amendment sourceAmendment, String reason, Population restrictToPopulation, ScheduledStudySegment targetStudySegment) {
        log.debug("Adding events from period {}", period);
        for (PlannedActivity plannedActivity : period.getPlannedActivities()) {
            schedulePlannedActivity(plannedActivity, period, sourceAmendment, reason, restrictToPopulation, targetStudySegment);
        }
    }

    /**
     * Derives one repetition's worth of scheduled events from the given period and applies them to
     * the given scheduled studySegment.
     * <p>
     * The input period should already match the provided sourceAmendment.
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public void schedulePeriod(
        Period period, Amendment sourceAmendment, String reason, ScheduledStudySegment targetStudySegment, int repetitionNumber
    ) {
        schedulePeriod(period, sourceAmendment, reason, null, targetStudySegment, repetitionNumber);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    protected void schedulePeriod(
        Period period, Amendment sourceAmendment, String reason, Population restrictToPopulation, ScheduledStudySegment targetStudySegment,
        int repetitionNumber
    ) {
        log.debug("Adding events for rep {} from period {}", repetitionNumber, period);
        for (PlannedActivity plannedActivity : period.getPlannedActivities()) {
            schedulePlannedActivity(plannedActivity, period, sourceAmendment, reason, restrictToPopulation, targetStudySegment, repetitionNumber);
        }
    }

    /**
     * Derives scheduled events from the given planned event and applies them to the given scheduled studySegment.
     * <p>
     * The input plannedActivity should already match the provided sourceAmendment.
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public void schedulePlannedActivity(
        PlannedActivity plannedActivity, Period period, Amendment sourceAmendment, String reason, ScheduledStudySegment targetStudySegment
    ) {
        // TODO: this seems to be ignoring the PA's population.  Examine and determine whether this is a bug.  RMS20080919.
        schedulePlannedActivity(plannedActivity, period, sourceAmendment, reason, null, targetStudySegment);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    protected void schedulePlannedActivity(
        PlannedActivity plannedActivity, Period period, Amendment sourceAmendment, String reason,
        Population restrictToPopulation, ScheduledStudySegment targetStudySegment
    ) {
        for (int r = 0 ; r < period.getRepetitions() ; r++) {
            schedulePlannedActivity(plannedActivity, period, sourceAmendment, reason, restrictToPopulation, targetStudySegment, r);
        }
    }

    /**
     * Derives a single scheduled event from the given planned event and applies it to the given scheduled studySegment.
     * <p>
     * The input plannedActivity should already match the provided sourceAmendment.
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    protected void schedulePlannedActivity(
        PlannedActivity plannedActivity, Period period, Amendment sourceAmendment, String reason,
        Population restrictToPopulation, ScheduledStudySegment targetStudySegment, int repetitionNumber
    ) {
        if (targetStudySegment.getScheduledCalendar().getAssignment() != null) {
            Set<Population> subjectPopulations = targetStudySegment.getScheduledCalendar().getAssignment().getPopulations();
            if (plannedActivity.getPopulation() != null && !subjectPopulations.contains(plannedActivity.getPopulation())) {
                log.debug("Skipping {} since the subject is not in population {}", plannedActivity, plannedActivity.getPopulation().getAbbreviation());
                return;
            }
        }
        if (restrictToPopulation != null && !restrictToPopulation.equals(plannedActivity.getPopulation())) {
            log.debug("Only adding planned activities for the {} population", restrictToPopulation);
            return;
        }

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

        ScheduledActivityState initialState
            =  plannedActivity.getInitialScheduledMode().createStateInstance();
        initialState.setReason(reason);
        initialState.setDate(event.getIdealDate());
        event.changeState(initialState);

        event.setDetails(plannedActivity.getDetails());
        event.setActivity(plannedActivity.getActivity());
        event.setLabels(plannedActivity.getLabelsForRepetition(repetitionNumber));
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
       List<ScheduledActivity> listOfEvents = studySegment.getActivities();
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
        List<BlackoutDate> holidayList = site.getBlackoutDates();

        for (BlackoutDate blackoutDate : holidayList) {
            // TODO: instanceof indicates abstraction failure -- this logic should be in each BlackoutDate class
            if (blackoutDate instanceof SpecificDateBlackout) {
                //month needs to be decremented, because we are using 00 for January in the Calendar
                SpecificDateBlackout h = (SpecificDateBlackout) blackoutDate;
                if (h.getYear() == null) {
                    holidayCalendar.set(year, h.getMonth(), h.getDay());
                } else {
                    holidayCalendar.set(h.getYear(), h.getMonth(), h.getDay());
                }
                String originalDateFormatted = df.format(date.getTime());
                String holidayDateFormatted = df.format(holidayCalendar.getTime());
                if (originalDateFormatted.equals(holidayDateFormatted)) {
                    shiftToAvoidBlackoutDate(date, event, site, blackoutDate.getDescription());
                }
            } else if(blackoutDate instanceof WeekdayBlackout) {
                WeekdayBlackout dayOfTheWeek = (WeekdayBlackout) blackoutDate;
                int intValueOfTheDay = dayOfTheWeek.getDayOfTheWeekInteger();
                if (dateCalendar.get(Calendar.DAY_OF_WEEK) == intValueOfTheDay) {
                    shiftToAvoidBlackoutDate(date, event, site, blackoutDate.getDescription());
                }
            } else if (blackoutDate instanceof RelativeRecurringBlackout) {
                RelativeRecurringBlackout relativeRecurringHoliday =
                        (RelativeRecurringBlackout) blackoutDate;
                Integer numberOfTheWeek = relativeRecurringHoliday.getWeekNumber();
                Integer month = relativeRecurringHoliday.getMonth();
                int dayOfTheWeekInt = relativeRecurringHoliday.getDayOfTheWeekInteger();
                List<Date> dates = findDaysOfWeekInMonth(year, month, dayOfTheWeekInt);
                //-1, since we start from position 0 in the list
                Date specificDay = dates.get(numberOfTheWeek-1);

                String originalDateFormatted = df.format(date.getTime());
                String holidayDateFormatted = df.format(specificDay);
                if (originalDateFormatted.equals(holidayDateFormatted)) {
                    shiftToAvoidBlackoutDate(date, event, site, blackoutDate.getDescription());
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

    /**
     * Finds all the dates which represent the particular day of the week (Monday, Tuesday, etc.)
     * in the specified month.
     */
    // package level for testing
    List<Date> findDaysOfWeekInMonth(int year, int month, int dayOfTheWeek) {
        List<Date> matches = new ArrayList<Date>();
        Calendar search = Calendar.getInstance();
        search.set(year, month, 1);
        while (
            search.get(Calendar.MONTH) == month &&
            search.get(Calendar.DAY_OF_MONTH) <= search.getActualMaximum(Calendar.DAY_OF_MONTH)
            ) {
            System.out.println(search.getTime());
            if (search.get(Calendar.DAY_OF_WEEK) == dayOfTheWeek) {
                matches.add(search.getTime());
            }
            search.add(Calendar.DAY_OF_MONTH, 1);
        }
        return matches;
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
                event.changeState(new Canceled("Off Study",event.getCurrentState().getDate()));
            } else if (ScheduledActivityMode.CONDITIONAL == event.getCurrentState().getMode()) {
                event.changeState(new NotApplicable("Off Study",event.getCurrentState().getDate()));
            }
        }

        studyAssignment.setEndDate(offStudyDate);
        subjectDao.save(studyAssignment.getSubject());
        return studyAssignment;
    }

    private List<ScheduledActivity> getPotentialUpcomingEvents(ScheduledCalendar calendar, Date offStudyDate) {
        List<ScheduledActivity> upcomingScheduledActivities = new ArrayList<ScheduledActivity>();
        for (ScheduledStudySegment studySegment : calendar.getScheduledStudySegments()) {
            if (!studySegment.isComplete()) {
                Map<Date, List<ScheduledActivity>> eventsByDate = studySegment.getActivitiesByDate();
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

    public void updatePopulations(StudySubjectAssignment assignment, Set<Population> newPopulations) {
        log.debug("updating populations for {} to {}", assignment, newPopulations);
        if (newPopulations == null) newPopulations = Collections.emptySet();
        for (Iterator<Population> it = assignment.getPopulations().iterator(); it.hasNext();) {
            Population currentPopulation = it.next();
            if (!newPopulations.contains(currentPopulation)) {
                removePopulation(assignment, currentPopulation, it);
            }
        }
        for (Population newPopulation : newPopulations) {
            if (!assignment.getPopulations().contains(newPopulation)) {
                addPopulation(assignment, newPopulation);
            }
        }
    }

    private void removePopulation(StudySubjectAssignment assignment, Population toRemove, Iterator<Population> iterator) {
        log.debug("removing population {} from {}", toRemove.getAbbreviation(), assignment);
        iterator.remove();
        for (ScheduledStudySegment segment : assignment.getScheduledCalendar().getScheduledStudySegments()) {
            segment.unschedulePopulationEvents("Subject removed from population " + toRemove.getName(), toRemove);
        }
    }

    private void addPopulation(StudySubjectAssignment assignment, Population toAdd) {
        log.debug("adding population {} for {}", toAdd.getAbbreviation(), assignment);
        assignment.addPopulation(toAdd);
        for (ScheduledStudySegment segment : assignment.getScheduledCalendar().getScheduledStudySegments()) {
            if (!segment.isComplete()) {
                StudySegment amendedSourceSegment
                    = amendmentService.getAmendedNode(segment.getStudySegment(), assignment.getCurrentAmendment());
                for (Period period : amendedSourceSegment.getPeriods()) {
                    schedulePeriod(period, assignment.getCurrentAmendment(),
                        "Subject added to population " + toAdd.getName(), toAdd, segment);
                }
            }
        }
    }

    ////// CONFIGURATION

    @Required
    public void setSubjectDao(SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }

    @Required
    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    @SuppressWarnings("unchecked")
    public Subject findSubjectByPersonId(final String mrn) {
        Subject subject = subjectDao.findSubjectByPersonId(mrn);
        if (subject != null) {
            Hibernate.initialize(subject.getAssignments());
            List<StudySubjectAssignment> studySubjectAssignments = subject.getAssignments();
            for (StudySubjectAssignment studySubjectAssignment : studySubjectAssignments) {
                Hibernate.initialize(studySubjectAssignment.getScheduledCalendar());
                if (studySubjectAssignment.getScheduledCalendar() != null) {
                    Hibernate.initialize(studySubjectAssignment.getScheduledCalendar().getScheduledStudySegments());
                }
            }
        }

        return subject;

    }

    @SuppressWarnings("unchecked")
    /**
     * Searches all the subjects in the system for those that match the given
     * criteria.  Returns a list of transient Subject elements containing just the
     * matching activities.
     */
    public List<Subject> getFilteredSubjects(String subjSearch) {
        return subjectDao.getSubjectsBySearchText(subjSearch);
    }

    public List<Subject> findSubjects(Subject searchCriteria) {
        if (searchCriteria == null) return null;

        validateSubjectAttributes(searchCriteria.getPersonId(), searchCriteria.getFirstName(), searchCriteria.getLastName(), searchCriteria.getDateOfBirth(), searchCriteria.getGender());

        if (searchCriteria.getPersonId() != null) {
            Subject subject = subjectDao.findSubjectByPersonId(searchCriteria.getPersonId());
            if (subject == null) {
                return Collections.emptyList();
            } else {
                return Collections.singletonList(subject);
            }
        } else {
            List<Subject> subjects = subjectDao.findSubjectByFirstNameLastNameAndDoB(searchCriteria.getFirstName(), searchCriteria.getLastName(), searchCriteria.getDateOfBirth());

            if (subjects == null) {
                return Collections.emptyList();
            } else {
                return subjects;
            }
        }
    }
    public Subject findSubject(Subject searchCriteria) {
        List<Subject> subjects = findSubjects(searchCriteria);
        if (subjects.isEmpty()) {
            return null;
        } else if (subjects.size() == 1) {
            return subjects.get(0);
        } else {
            throw new StudyCalendarValidationException(
                    "Multiple subjects found for %s, %s.  With a birth date of %s",
                    searchCriteria.getLastName(), searchCriteria.getFirstName(), searchCriteria.getDateOfBirth());
        }
    }

    private void validateSubjectAttributes(String personId, String firstName, String lastName, Date birthDate, Gender gender) {
        if (StringUtils.isEmpty(personId)) {
            if (StringUtils.isEmpty(firstName)) {
                throw new StudyCalendarValidationException(
                        "Subject first name is required if person id is empty");
            } else if (StringUtils.isEmpty(lastName)) {
                throw new StudyCalendarValidationException(
                        "Subject last name is required if person id is empty");
            } else if (birthDate == null) {
                throw new StudyCalendarValidationException(
                        "Subject birth date is required if person id is empty");
            }
        }
        if (gender==null ) {
            throw new StudyCalendarValidationException(
                    "Subject gender is required");
        }
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
