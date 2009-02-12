package edu.northwestern.bioinformatics.studycalendar.web.subject;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import static edu.northwestern.bioinformatics.studycalendar.test.ServicedFixtures.*;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import static gov.nih.nci.cabig.ctms.lang.DateTools.createDate;
import gov.nih.nci.cabig.ctms.lang.StaticNowFactory;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

/**
 * @author Rhett Sutphin
 */
public class SubjectCentricScheduleTest extends StudyCalendarTestCase {
    private Subject subject;
    private Site site;
    private Study nu1400, nu2332;
    private StudySubjectAssignment nu1400assignment;
    private StudySubjectAssignment nu2332assignment;

    private StaticNowFactory nowFactory;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        subject = createSubject("Jo", "Miller");
        site = createSite("W");
        nu1400 = createSingleEpochStudy("NU 1400", "Treatment");
        nu2332 = createSingleEpochStudy("NU 2332", "QoL");
        nu1400assignment = createAssignment(nu1400, site, subject);
        nu2332assignment = createAssignment(nu2332, site, subject);

        nu1400assignment.getScheduledCalendar().addStudySegment(
            createScheduledStudySegment(createDate(2006, Calendar.APRIL, 1), 365));
        nu1400assignment.getScheduledCalendar().addStudySegment(
            createScheduledStudySegment(createDate(2006, Calendar.APRIL, 1), 14));
        nu2332assignment.getScheduledCalendar().addStudySegment(
            createScheduledStudySegment(createDate(2004, Calendar.JULY, 1), 14));

        nowFactory = new StaticNowFactory();
    }

    private SubjectCentricSchedule createOneStudySchedule() {
        return new SubjectCentricSchedule(Arrays.asList(nu1400assignment), Collections.<StudySubjectAssignment>emptyList(), nowFactory);
    }

    private SubjectCentricSchedule createTwoStudySchedule() {
        return new SubjectCentricSchedule(Arrays.asList(nu1400assignment, nu2332assignment), Collections.<StudySubjectAssignment>emptyList(), nowFactory);
    }

    private SubjectCentricSchedule createOneVisibleOneHiddenSchedule() {
        return new SubjectCentricSchedule(Arrays.asList(nu1400assignment), Arrays.asList(nu2332assignment), nowFactory);
    }

    public void testCreateRowsWithOneAssignment() throws Exception {
        nu1400assignment.getScheduledCalendar().addStudySegment(
            createScheduledStudySegment(createDate(2006, Calendar.JULY, 1), 14));
        SubjectCentricSchedule schedule = createOneStudySchedule();
        assertEquals("Wrong number of rows created", 2, schedule.getSegmentRows().size());
    }

    public void testCreateRowsWithTwoAssignments() throws Exception {
        SubjectCentricSchedule schedule = createTwoStudySchedule();
        assertEquals("Wrong number of rows created", 3, schedule.getSegmentRows().size());
    }

    public void testDateRange() throws Exception {
        SubjectCentricSchedule schedule = createTwoStudySchedule();
        assertDayOfDate("Wrong start date", 2004, Calendar.JULY, 1, schedule.getDateRange().getStart());
        assertDayOfDate("Wrong end date", 2007, Calendar.MARCH, 31, schedule.getDateRange().getStop());
    }

    public void testDateRangeIncludesHiddenSchedules() throws Exception {
        SubjectCentricSchedule schedule = createOneVisibleOneHiddenSchedule();
        assertDayOfDate("Wrong start date", 2004, Calendar.JULY, 1, schedule.getDateRange().getStart());
        assertDayOfDate("Wrong end date", 2007, Calendar.MARCH, 31, schedule.getDateRange().getStop());
    }

    public void testDaysIncludesEntryForEveryDateInRange() throws Exception {
        SubjectCentricSchedule schedule = createTwoStudySchedule();
        assertDayOfDate("Wrong start date", 2004, Calendar.JULY, 1, schedule.getDays().get(0).getDate());
        assertDayOfDate("Wrong end date", 2007, Calendar.MARCH, 31, schedule.getDays().get(schedule.getDays().size() - 1).getDate());
        assertEquals("Wrong number of days", 1004, schedule.getDays().size());
    }

    public void testDaysCollectsScheduledActivitiesFromAllSegmentsOfAllAssignments() throws Exception {
        nu1400assignment.getScheduledCalendar().getScheduledStudySegments().get(0).addEvent(createScheduledActivity("A", 2006, Calendar.APRIL, 1));
        nu1400assignment.getScheduledCalendar().getScheduledStudySegments().get(1).addEvent(createScheduledActivity("B", 2006, Calendar.APRIL, 1));
        nu2332assignment.getScheduledCalendar().getScheduledStudySegments().get(0).addEvent(createScheduledActivity("C", 2004, Calendar.JULY, 1));

        SubjectCentricSchedule schedule = createTwoStudySchedule();
        ScheduleDay afd2006 = schedule.getDays().get(639);
        assertDayOfDate("Test setup failure", 2006, Calendar.APRIL, 1, afd2006.getDate());
        assertEquals("Wrong number of activities accumulated on 2006-04-01", 2, afd2006.getActivities().size());
        assertEquals("Missing A from 2006-04-01", "A", afd2006.getActivities().get(0).getActivity().getName());
        assertEquals("Missing B from 2006-04-01", "B", afd2006.getActivities().get(1).getActivity().getName());
        assertEquals("Wrong number of activities accumulated on 2004-07-01", 1, schedule.getDays().get(0).getActivities().size());
    }

    public void testDaysIncludesHiddenElementsMarker() throws Exception {
        nu1400assignment.getScheduledCalendar().getScheduledStudySegments().get(0).addEvent(createScheduledActivity("A", 2006, Calendar.APRIL, 1));
        nu1400assignment.getScheduledCalendar().getScheduledStudySegments().get(1).addEvent(createScheduledActivity("B", 2006, Calendar.APRIL, 1));
        nu2332assignment.getScheduledCalendar().getScheduledStudySegments().get(0).addEvent(createScheduledActivity("C", 2004, Calendar.JULY, 1));

        SubjectCentricSchedule schedule = createOneVisibleOneHiddenSchedule();
        assertTrue("Should have hidden activities on 2004-07-01", schedule.getDays().get(0).getHasHiddenActivities());
    }

    public void testIncludesTodayWhenItDoes() throws Exception {
        nu1400assignment.getScheduledCalendar().getScheduledStudySegments().get(0).addEvent(createScheduledActivity("A", 2006, Calendar.APRIL, 1));
        SubjectCentricSchedule schedule = createOneVisibleOneHiddenSchedule();
        nowFactory.setNowTimestamp(DateTools.createTimestamp(2006, Calendar.JULY, 4));
        assertTrue(schedule.getIncludesToday());
    }

    public void testIncludesTodayWhenItDoesNot() throws Exception {
        nu1400assignment.getScheduledCalendar().getScheduledStudySegments().get(0).addEvent(createScheduledActivity("A", 2006, Calendar.APRIL, 1));
        SubjectCentricSchedule schedule = createOneVisibleOneHiddenSchedule();
        nowFactory.setNowTimestamp(DateTools.createTimestamp(2008, Calendar.JULY, 4));
        assertFalse(schedule.getIncludesToday());
    }
}
