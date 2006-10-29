package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.nwu.bioinformatics.commons.DateUtils;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Canceled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.DatedScheduledEventState;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Occurred;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.ScheduledEventState;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class ScheduleEventCommandTest extends StudyCalendarTestCase {
    private static final String NEW_REASON = "New Reason";
    private static final Date NEW_DATE = DateUtils.createDate(2003, Calendar.MARCH, 14);

    private ScheduleEventCommand command;

    private ScheduledCalendarDao scheduledCalendarDao;
    private ScheduledEvent event;

    protected void setUp() throws Exception {
        super.setUp();
        scheduledCalendarDao = registerMockFor(ScheduledCalendarDao.class);
        command = new ScheduleEventCommand(scheduledCalendarDao);

        event = Fixtures.createScheduledEvent("ABC", 2003, Calendar.MARCH, 13);
        event.setScheduledArm(new ScheduledArm());
        event.getScheduledArm().setScheduledCalendar(new ScheduledCalendar());

        command.setEvent(event);
        command.setNewReason(NEW_REASON);
        command.setNewDate(NEW_DATE);
    }

    public void testCreateCanceledState() throws Exception {
        command.setNewMode(ScheduledEventMode.CANCELED);
        replayMocks();

        ScheduledEventState created = command.createState();
        assertTrue(created instanceof Canceled);
        assertEquals(NEW_REASON, created.getReason());
    }

    public void testCreateScheduledState() throws Exception {
        command.setNewMode(ScheduledEventMode.SCHEDULED);
        replayMocks();

        ScheduledEventState created = command.createState();
        assertTrue(created instanceof Scheduled);
        assertEquals(NEW_REASON, created.getReason());
        assertEquals(NEW_DATE, ((DatedScheduledEventState) created).getDate());
    }
    
    public void testCreateOccurredState() throws Exception {
        command.setNewMode(ScheduledEventMode.OCCURRED);
        replayMocks();

        ScheduledEventState created = command.createState();
        assertTrue(created instanceof Occurred);
        assertEquals(NEW_REASON, created.getReason());
        assertEquals(NEW_DATE, ((DatedScheduledEventState) created).getDate());
    }

    public void testChangeState() throws Exception {
        assertEquals("Unexpeced number of initial states", 1, event.getAllStates().size());

        command.setNewMode(ScheduledEventMode.OCCURRED);
        scheduledCalendarDao.save(event.getScheduledArm().getScheduledCalendar());

        replayMocks();
        command.changeState();
        verifyMocks();
        assertEquals("Wrong number of states", 2, event.getAllStates().size());
        assertEquals("Wrong mode for current state", ScheduledEventMode.OCCURRED, event.getCurrentState().getMode());
        assertEquals("Wrong reason for current state", NEW_REASON, event.getCurrentState().getReason());
    }
}
