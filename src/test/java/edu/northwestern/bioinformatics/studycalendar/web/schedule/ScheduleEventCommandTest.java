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
import java.util.Collection;

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
        assertNull(event.getNotes());

        command.setNewMode(ScheduledEventMode.OCCURRED);
        command.setNewNotes("Change-o");
        scheduledCalendarDao.save(event.getScheduledArm().getScheduledCalendar());

        replayMocks();
        command.apply();
        verifyMocks();
        assertEquals("Wrong number of states", 2, event.getAllStates().size());
        assertEquals("Wrong mode for current state", ScheduledEventMode.OCCURRED, event.getCurrentState().getMode());
        assertEquals("Wrong reason for current state", NEW_REASON, event.getCurrentState().getReason());
        assertEquals("Wrong notes", "Change-o", event.getNotes());
    }

    public void testEventSpecificModesForNonConditionalEvent() throws Exception {
        command.setNewMode(ScheduledEventMode.SCHEDULED);
        replayMocks();
        Collection<ScheduledEventMode> collection = command.getEventSpecificMode();
        System.out.println("collection " + collection);
        assertEquals("Wrong number of modes", 3, collection.size());
    }


    public void testEventSpecificModesForConditionalEvent() throws Exception {
        ScheduledEvent conditionalEvent = Fixtures.createConditionalEvent("ABC", 2003, Calendar.MARCH, 13);
        conditionalEvent.changeState(new Scheduled("Schedule", DateUtils.createDate(2003, Calendar.MARCH, 13)));
        command.setEvent(conditionalEvent);
        command.setNewMode(ScheduledEventMode.SCHEDULED);
        replayMocks();
        Collection<ScheduledEventMode> collection = command.getEventSpecificMode();
        System.out.println("collection " + collection);
        assertEquals("Wrong number of modes", 5, collection.size());
    }

}
