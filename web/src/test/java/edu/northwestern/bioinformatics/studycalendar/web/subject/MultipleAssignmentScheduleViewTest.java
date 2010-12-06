package edu.northwestern.bioinformatics.studycalendar.web.subject;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserStudySubjectAssignmentRelationship;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import gov.nih.nci.cabig.ctms.lang.StaticNowFactory;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static gov.nih.nci.cabig.ctms.lang.DateTools.createDate;

/**
 * @author Rhett Sutphin
 */
public class MultipleAssignmentScheduleViewTest extends StudyCalendarTestCase {
    private StudySubjectAssignment nu1400assignment;
    private StudySubjectAssignment nu2332assignment;
    private UserStudySubjectAssignmentRelationship nu1400assignmentForUser;
    private UserStudySubjectAssignmentRelationship nu2332assignmentForUser;
    private PscUser user;

    private StaticNowFactory nowFactory;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Subject subject = createSubject("Jo", "Miller");
        Site site = createSite("W");
        Study nu1400 = createSingleEpochStudy("NU 1400", "Treatment");
        Study nu2332 = createSingleEpochStudy("NU 2332", "QoL");
        nu1400assignment = createAssignment(nu1400, site, subject);
        nu2332assignment = createAssignment(nu2332, site, subject);

        nu1400assignment.getScheduledCalendar().addStudySegment(
            createScheduledStudySegment(createDate(2006, Calendar.APRIL, 1), 365));
        nu1400assignment.getScheduledCalendar().addStudySegment(
            createScheduledStudySegment(createDate(2006, Calendar.APRIL, 1), 14));
        nu2332assignment.getScheduledCalendar().addStudySegment(
            createScheduledStudySegment(createDate(2004, Calendar.JULY, 1), 14));

        user = AuthorizationObjectFactory.createPscUser("ji", PscRole.STUDY_SUBJECT_CALENDAR_MANAGER);
        nu1400assignmentForUser = new UserStudySubjectAssignmentRelationship(user, nu1400assignment);
        nu2332assignmentForUser = new UserStudySubjectAssignmentRelationship(user, nu2332assignment);

        nowFactory = new StaticNowFactory();
    }

    private MultipleAssignmentScheduleView createOneStudySchedule() {
        return new MultipleAssignmentScheduleView(
            Arrays.asList(nu1400assignmentForUser), nowFactory);
    }

    private MultipleAssignmentScheduleView createTwoStudySchedule() {
        return new MultipleAssignmentScheduleView(
            Arrays.asList(nu1400assignmentForUser, nu2332assignmentForUser), nowFactory);
    }

    private MultipleAssignmentScheduleView createOneVisibleOneHiddenSchedule() {
        user.getMembership(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forStudies("NU 1400");
        return new MultipleAssignmentScheduleView(
            Arrays.asList(nu1400assignmentForUser, nu2332assignmentForUser), nowFactory);
    }

    public void testCreateRowsWithOneAssignment() throws Exception {
        nu1400assignment.getScheduledCalendar().addStudySegment(
            createScheduledStudySegment(createDate(2006, Calendar.JULY, 1), 14));
        MultipleAssignmentScheduleView schedule = createOneStudySchedule();
        assertEquals("Wrong number of rows created", 2, schedule.getSegmentRows().size());
    }

    public void testCreateRowsWithTwoAssignments() throws Exception {
        MultipleAssignmentScheduleView schedule = createTwoStudySchedule();
        assertEquals("Wrong number of rows created", 3, schedule.getSegmentRows().size());
    }

    public void testDateRange() throws Exception {
        MultipleAssignmentScheduleView schedule = createTwoStudySchedule();
        assertDayOfDate("Wrong start date", 2004, Calendar.JULY, 1, schedule.getDateRange().getStart());
        assertDayOfDate("Wrong end date", 2007, Calendar.MARCH, 31, schedule.getDateRange().getStop());
    }

    public void testDateRangeIncludesHiddenSchedules() throws Exception {
        MultipleAssignmentScheduleView schedule = createOneVisibleOneHiddenSchedule();
        assertDayOfDate("Wrong start date", 2004, Calendar.JULY, 1, schedule.getDateRange().getStart());
        assertDayOfDate("Wrong end date", 2007, Calendar.MARCH, 31, schedule.getDateRange().getStop());
    }

    public void testDaysIncludesEntryForEveryDateInRange() throws Exception {
        MultipleAssignmentScheduleView schedule = createTwoStudySchedule();
        assertDayOfDate("Wrong start date", 2004, Calendar.JULY, 1, schedule.getDays().get(0).getDate());
        assertDayOfDate("Wrong end date", 2007, Calendar.MARCH, 31, schedule.getDays().get(schedule.getDays().size() - 1).getDate());
        assertEquals("Wrong number of days", 1004, schedule.getDays().size());
    }

    public void testDaysCollectsScheduledActivitiesFromAllSegmentsOfAllAssignments() throws Exception {
        nu1400assignment.getScheduledCalendar().getScheduledStudySegments().get(0).addEvent(createScheduledActivity("A", 2006, Calendar.APRIL, 1));
        nu1400assignment.getScheduledCalendar().getScheduledStudySegments().get(1).addEvent(createScheduledActivity("B", 2006, Calendar.APRIL, 1));
        nu2332assignment.getScheduledCalendar().getScheduledStudySegments().get(0).addEvent(createScheduledActivity("C", 2004, Calendar.JULY, 1));

        MultipleAssignmentScheduleView schedule = createTwoStudySchedule();
        ScheduleDay afd2006 = schedule.getDays().get(639);
        assertDayOfDate("Test setup failure", 2006, Calendar.APRIL, 1, afd2006.getDate());
        assertEquals("Wrong number of activities accumulated on 2006-04-01", 2, afd2006.getActivities().size());
        Iterator<ScheduledActivity> actIt = afd2006.getActivities().iterator();
        assertEquals("Missing A from 2006-04-01", "A", actIt.next().getActivity().getName());
        assertEquals("Missing B from 2006-04-01", "B", actIt.next().getActivity().getName());
        assertEquals("Wrong number of activities accumulated on 2004-07-01", 1, schedule.getDays().get(0).getActivities().size());
    }

    public void testDaysIncludesHiddenElementsMarker() throws Exception {
        nu1400assignment.getScheduledCalendar().getScheduledStudySegments().get(0).addEvent(createScheduledActivity("A", 2006, Calendar.APRIL, 1));
        nu1400assignment.getScheduledCalendar().getScheduledStudySegments().get(1).addEvent(createScheduledActivity("B", 2006, Calendar.APRIL, 1));
        nu2332assignment.getScheduledCalendar().getScheduledStudySegments().get(0).addEvent(createScheduledActivity("C", 2004, Calendar.JULY, 1));

        MultipleAssignmentScheduleView schedule = createOneVisibleOneHiddenSchedule();
        assertTrue("Should have hidden activities on 2004-07-01", schedule.getDays().get(0).getHasHiddenActivities());
    }

    public void testIncludesTodayWhenItDoes() throws Exception {
        nu1400assignment.getScheduledCalendar().getScheduledStudySegments().get(0).addEvent(createScheduledActivity("A", 2006, Calendar.APRIL, 1));
        MultipleAssignmentScheduleView schedule = createOneVisibleOneHiddenSchedule();
        nowFactory.setNowTimestamp(DateTools.createTimestamp(2006, Calendar.JULY, 4));
        assertTrue(schedule.getIncludesToday());
    }

    public void testIncludesTodayWhenItDoesNot() throws Exception {
        nu1400assignment.getScheduledCalendar().getScheduledStudySegments().get(0).addEvent(createScheduledActivity("A", 2006, Calendar.APRIL, 1));
        MultipleAssignmentScheduleView schedule = createOneVisibleOneHiddenSchedule();
        nowFactory.setNowTimestamp(DateTools.createTimestamp(2008, Calendar.JULY, 4));
        assertFalse(schedule.getIncludesToday());
    }
}
