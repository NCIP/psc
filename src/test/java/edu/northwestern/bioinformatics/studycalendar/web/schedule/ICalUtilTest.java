package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import net.fortuna.ical4j.model.Calendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;

/**
 * @author Saurabh Agrawal
 */
@SuppressWarnings("unchecked")
public class ICalUtilTest extends junit.framework.TestCase {

	private StudyParticipantAssignment studyParticipantAssignment;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

	}

	public void testGenerateCalendar() throws Exception {

		Calendar calendar = ICalUtil.generateICalendar(studyParticipantAssignment);
		assertTrue(calendar.getComponents().size() == 0);

		studyParticipantAssignment = new StudyParticipantAssignment();
		calendar = ICalUtil.generateICalendar(studyParticipantAssignment);
		assertTrue(calendar.getComponents().size() == 0);

		ScheduledCalendar scheduledCalendar = new ScheduledCalendar();
		studyParticipantAssignment.setScheduledCalendar(scheduledCalendar);

		calendar = ICalUtil.generateICalendar(studyParticipantAssignment);
		assertTrue(calendar.getComponents().size() == 0);
		assertTrue(calendar.getProperties().size() == 4);

	}

}