package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Summary;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Conditional;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;

/**
 * @author Saurabh Agrawal
 */
@SuppressWarnings("unchecked")
public class ICalToolsTest extends junit.framework.TestCase {

	private StudyParticipantAssignment studyParticipantAssignment;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

	}

	/**
	 * Test the generate calendar method for null or empty study participant assignment.
	 * 
	 * @throws Exception the exception
	 */
	public void testGenerateCalendarForNullOrEmptyStudyParticipantAssignment() throws Exception {

		Calendar calendar = ICalTools.generateICalendar(studyParticipantAssignment);
		assertNotNull(calendar);
		assertEquals(0, calendar.getComponents().size());

		studyParticipantAssignment = new StudyParticipantAssignment();
		calendar = ICalTools.generateICalendar(studyParticipantAssignment);
		assertNotNull(calendar);
		assertEquals(0, calendar.getComponents().size());

	}

	/**
	 * Test the generate calendar method for patient having empty schedule.
	 * 
	 */
	public void testGenerateCalendarForPatientHavingEmptySchedule() throws Exception {

		studyParticipantAssignment = new StudyParticipantAssignment();
		ScheduledCalendar scheduledCalendar = new ScheduledCalendar();
		studyParticipantAssignment.setScheduledCalendar(scheduledCalendar);

		Calendar calendar = ICalTools.generateICalendar(studyParticipantAssignment);
		assertEquals(0, calendar.getComponents().size());
		assertEquals("calendar should be empty but it should have 4 properties..", 4, calendar.getProperties().size());

	}

	/**
	 * Test the generate calendar method for patient having non empty schedule.
	 * 
	 */
	public void testGenerateCalendarForPatientHavingNonEmptySchedule() throws Exception {

		studyParticipantAssignment = new StudyParticipantAssignment();
		Participant participant = Fixtures.createParticipant("firstName", "lastName");
		studyParticipantAssignment.setParticipant(participant);

		ScheduledCalendar scheduledCalendar = new ScheduledCalendar();

		ScheduledArm scheduledArm1 = createScheduleArmWithSomeEvents("arm1", 3, ScheduledActivityMode.SCHEDULED);
		ScheduledArm scheduledArm2 = createScheduleArmWithSomeEvents("arm2", 5, ScheduledActivityMode.SCHEDULED);
		ScheduledArm scheduledArm3 = createScheduleArmWithSomeEvents("arm3", 6, ScheduledActivityMode.CANCELED);
		scheduledCalendar.addArm(scheduledArm1);
		scheduledCalendar.addArm(scheduledArm2);
		scheduledCalendar.addArm(scheduledArm3);

		studyParticipantAssignment.setScheduledCalendar(scheduledCalendar);

		Calendar calendar = ICalTools.generateICalendar(studyParticipantAssignment);
		FileOutputStream outputStream = new FileOutputStream("abc.ics");
		final CalendarOutputter output = new CalendarOutputter();
		output.setValidating(false);
		output.output(calendar, outputStream);

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
			assertEquals("the summary value should have 'activity name:event:arm' string", 0, summary.getValue()
					.indexOf("activity name:event:arm"));

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

	private ScheduledArm createScheduleArmWithSomeEvents(final String name, final int count,
			final ScheduledActivityMode eventMode) {
		ScheduledArm scheduledArm = new ScheduledArm();

		// add few schedule events
		scheduledArm.setEvents(createScheduleEvents("event:" + name, count, eventMode));
		return scheduledArm;
	}

	private List<ScheduledActivity> createScheduleEvents(final String name, final int count,
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
			}
			else if (eventMode.equals(ScheduledActivityMode.CONDITIONAL)) {
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
		Activity activity = new Activity();
		activity.setName("activity name:" + name);
		activity.setType(ActivityType.PROCEDURE);
		activity.setDescription("desc:" + name);
		return activity;
	}
}