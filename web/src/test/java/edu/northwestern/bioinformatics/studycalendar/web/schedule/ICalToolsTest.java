package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Conditional;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Summary;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author Saurabh Agrawal
 */
@SuppressWarnings("unchecked")
public class ICalToolsTest extends junit.framework.TestCase {

    private StudySubjectAssignment studySubjectAssignment;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

    }

    /**
     * Test the generate calendar method for null or empty study subject assignment.
     *
     * @throws Exception the exception
     */
    public void testGenerateCalendarForNullOrEmptyStudySubjectAssignment() throws Exception {

        Calendar calendar = ICalTools.generateICSCalendar(studySubjectAssignment);
        assertNotNull(calendar);
        assertEquals(0, calendar.getComponents().size());

        studySubjectAssignment = new StudySubjectAssignment();
        calendar = ICalTools.generateICSCalendar(studySubjectAssignment);
        assertNotNull(calendar);
        assertEquals(0, calendar.getComponents().size());

    }

    public void testGenerateCalendarFileName() throws Exception {
        Subject subject = Fixtures.createSubject("firstName", "lastName");
        StudySite studySite = new StudySite();
        Study study = new Study();
        study.setAssignedIdentifier("test-study");
        studySite.setStudy(study);
        ScheduledCalendar scheduledCalendar = new ScheduledCalendar();

        studySubjectAssignment = edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createAssignment(studySite, subject);
        studySubjectAssignment.setGridId("grid-0");

        String calendarFileName = ICalTools.generateICSfileName(studySubjectAssignment);
        assertNotNull(calendarFileName);
        assertEquals("lastName-firstName-test-study.ics", calendarFileName);


    }

    /**
     * Test the generate calendar method for patient having empty schedule.
     */
    public void testGenerateCalendarForPatientHavingEmptySchedule() throws Exception {

        studySubjectAssignment = new StudySubjectAssignment();
        ScheduledCalendar scheduledCalendar = new ScheduledCalendar();
        studySubjectAssignment.setScheduledCalendar(scheduledCalendar);

        Calendar calendar = ICalTools.generateICSCalendar(studySubjectAssignment);
        assertEquals(0, calendar.getComponents().size());
        assertEquals("calendar should be empty but it should have 4 properties..", 4, calendar.getProperties().size());

    }

    /**
     * Test the generate calendar method for patient having non empty schedule.
     */
    public void testGenerateCalendarForPatientHavingNonEmptySchedule() throws Exception {

        studySubjectAssignment = new StudySubjectAssignment();
        Subject subject = Fixtures.createSubject("firstName", "lastName");
        studySubjectAssignment.setSubject(subject);

        ScheduledCalendar scheduledCalendar = new ScheduledCalendar();

        ScheduledStudySegment scheduledStudySegment1 = createScheduleStudySegmentWithSomeEvents("studySegment1", 3, ScheduledActivityMode.SCHEDULED);
        ScheduledStudySegment scheduledStudySegment2 = createScheduleStudySegmentWithSomeEvents("studySegment2", 5, ScheduledActivityMode.SCHEDULED);
        ScheduledStudySegment scheduledStudySegment3 = createScheduleStudySegmentWithSomeEvents("studySegment3", 6, ScheduledActivityMode.CANCELED);
        scheduledCalendar.addStudySegment(scheduledStudySegment1);
        scheduledCalendar.addStudySegment(scheduledStudySegment2);
        scheduledCalendar.addStudySegment(scheduledStudySegment3);

        studySubjectAssignment.setScheduledCalendar(scheduledCalendar);

        Calendar calendar = ICalTools.generateICSCalendar(studySubjectAssignment);

        assertEquals("calendar should have 8(5+3) events", 8, calendar.getComponents().size());
        assertEquals("calendar  should have only 4 properties..", 4, calendar.getProperties().size());
        List<VEvent> vEvents = calendar.getComponents();

        for (VEvent vEvent : vEvents) {
            assertEquals("vEvent should have  4 properties(DtStamp,DtStart,SUMMARY,DESCRIPTION)", 4, vEvent
                    .getProperties().size());

            assertEquals("vEvent  should have only 1 SUMMARY property..", 1, vEvent.getProperties(Property.SUMMARY)
                    .size());
            Summary summary = (Summary) vEvent.getProperties("SUMMARY").get(0);
            assertNotNull(summary);
            assertEquals("there should not be any parameter in summary", 0, summary.getParameters().size());
            assertEquals("the summary value should have 'activity name:event:studySegment' string", 0, summary.getValue()
                    .indexOf("activity name:event:studySegment"));

            assertEquals("vEvent should have only 1 DESCRIPTION property ..", 1, vEvent.getProperties(
                    Property.DESCRIPTION).size());
            Description description = (Description) vEvent.getProperties(Property.DESCRIPTION).get(0);

            assertEquals("there should not be any parameter in DESCRIPTION", 0, description.getParameters().size());
            assertEquals("the descripton value should have 'lastName, firstName' string", 0, description.getValue()
                    .indexOf("lastName, firstName"));

            assertEquals("vEvent  should have only 1 DtStart property ..", 1, vEvent.getProperties(Property.DTSTART)
                    .size());

            assertEquals("vEvent  should have only 1 DtStamp property ..", 1, vEvent.getProperties(Property.DTSTAMP)
                    .size());

        }

    }

    private ScheduledStudySegment createScheduleStudySegmentWithSomeEvents(final String name, final int count,
                                                                           final ScheduledActivityMode eventMode) {
        ScheduledStudySegment scheduledStudySegment = new ScheduledStudySegment();

        // add few schedule events
        scheduledStudySegment.setActivities(createScheduleActivities("event:" + name, count, eventMode));
        return scheduledStudySegment;
    }

    private List<ScheduledActivity> createScheduleActivities(final String name, final int count,
                                                             final ScheduledActivityMode eventMode) {
        List<ScheduledActivity> events = new ArrayList<ScheduledActivity>();
        for (int i = 0; i < count; i++) {
            ScheduledActivity scheduledActivity = new ScheduledActivity();
            Activity activity = createActivity(name + i);
            scheduledActivity.setActivity(activity);
            scheduledActivity.setDetails("details:" + i);
            scheduledActivity.setNotes("notes:" + i);
            if (eventMode.equals(ScheduledActivityMode.SCHEDULED)) {
                Scheduled newState = new Scheduled();
                scheduledActivity.changeState(newState);
            } else if (eventMode.equals(ScheduledActivityMode.CONDITIONAL)) {
                Conditional newState = new Conditional();
                scheduledActivity.changeState(newState);
            }
            java.util.Calendar calendar = new GregorianCalendar(2007, java.util.Calendar.OCTOBER, 10 + i);
            scheduledActivity.setIdealDate(new Date(calendar.getTimeInMillis()));
            events.add(scheduledActivity);
        }
        return events;
    }

    private Activity createActivity(final String name) {
        ActivityType activityType = Fixtures.createActivityType("PROCEDURE");
        Activity activity = new Activity();
        activity.setName("activity name:" + name);
        activity.setType(activityType);
        activity.setDescription("desc:" + name);
        return activity;
    }
}