package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.nwu.bioinformatics.commons.DateUtils;
import static edu.nwu.bioinformatics.commons.testing.CoreTestCase.assertSameDay;

import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Occurred;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.ScheduledEventState;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.DatedScheduledEventState;
import static edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase.assertDayOfDate;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class ScheduledCalendarDaoTest extends ContextDaoTestCase<ScheduledCalendarDao> {
    private ParticipantDao participantDao
        = (ParticipantDao) getApplicationContext().getBean("participantDao");
    private ArmDao armDao
        = (ArmDao) getApplicationContext().getBean("armDao");

    public void testGetById() throws Exception {
        ScheduledCalendar cal = getDao().getById(-20);

        assertEquals("Wrong assignment", -1, (int) cal.getAssignment().getId());

        assertEquals("Wrong number of arms", 2, cal.getScheduledArms().size());
        assertEquals("Wrong arm 0", -22, (int) cal.getScheduledArms().get(0).getId());
        assertEquals("Wrong arm 1", -21, (int) cal.getScheduledArms().get(1).getId());

        assertEquals("Wrong number of events in arm 0", 1, cal.getScheduledArms().get(0).getEvents().size());
        assertEquals("Wrong number of events in arm 1", 0, cal.getScheduledArms().get(1).getEvents().size());

        ScheduledEvent event = cal.getScheduledArms().get(0).getEvents().get(0);
        assertEquals("Wrong event", -10, (int) event.getId());
        assertEquals("Wrong base event for event", -6, (int) event.getPlannedEvent().getId());
        assertDayOfDate("Wrong ideal date", 2006, Calendar.OCTOBER, 31, event.getIdealDate());
        assertEquals("Wrong notes", "Boo!", event.getNotes());

        ScheduledEventState currentState = event.getCurrentState();
        assertTrue(currentState instanceof Occurred);
        assertDayOfDate("Wrong current state date", 2006, Calendar.OCTOBER, 25, ((DatedScheduledEventState) currentState).getDate());
        assertEquals("Wrong current state mode", ScheduledEventMode.OCCURRED, currentState.getMode());
        assertEquals("Wrong current state reason", "Success", currentState.getReason());
    }

    public void testLoadStateHistory() throws Exception {
        ScheduledCalendar cal = getDao().getById(-20);

        List<ScheduledEventState> states = cal.getScheduledArms().get(0).getEvents().get(0).getAllStates();
        assertEquals("Wrong number of states", 4, states.size());
        assertEventState(-11, ScheduledEventMode.SCHEDULED, "Initial input", DateUtils.createDate(2006, Calendar.OCTOBER, 22), states.get(0));
        assertEventState(-12, ScheduledEventMode.CANCELED,  "Called to cancel", null, states.get(1));
        assertEventState(-13, ScheduledEventMode.SCHEDULED, "Called to reschedule", DateUtils.createDate(2006, Calendar.OCTOBER, 25), states.get(2));
        assertEventState(null, ScheduledEventMode.OCCURRED, "Success", DateUtils.createDate(2006, Calendar.OCTOBER, 25), states.get(3));
    }

    private void assertEventState(
        Integer expectedId, ScheduledEventMode expectedMode, String expectedReason,
        Date expectedDate, ScheduledEventState actualState
    ) {
        assertEquals("Wrong ID", expectedId, actualState.getId());
        assertEquals("Wrong mode", expectedMode, actualState.getMode());
        assertEquals("Wrong reason", expectedReason, actualState.getReason());
        if (expectedDate != null) assertSameDay("Wrong date", expectedDate, ((DatedScheduledEventState)actualState).getDate());
    }

    public void testSave() throws Exception {
        int savedId;

        Date expectedIdealDate = DateUtils.createDate(2006, Calendar.SEPTEMBER, 20);
        Date expectedActualDate = DateUtils.createDate(2006, Calendar.SEPTEMBER, 22);
        ScheduledEventMode expectedMode = ScheduledEventMode.OCCURRED;
        String expectedReason = "All done";

        {
            Participant participant = participantDao.getById(-1);

            ScheduledCalendar calendar = new ScheduledCalendar();
            calendar.setAssignment(participant.getAssignments().get(0));
            assertEquals(-2, (int) calendar.getAssignment().getId());
            Arm arm4 = armDao.getById(-4);
            Arm arm3 = armDao.getById(-3);
            calendar.addArm(Fixtures.createScheduledArm(arm4));
            calendar.addArm(Fixtures.createScheduledArm(arm3));
            ScheduledArm lastScheduledArm = Fixtures.createScheduledArm(arm4);
            calendar.addArm(lastScheduledArm);

            ScheduledEvent event = new ScheduledEvent();
            event.setIdealDate(expectedIdealDate);
            event.setPlannedEvent(arm3.getPeriods().iterator().next().getPlannedEvents().get(0));
            event.changeState(new Occurred(expectedReason, expectedActualDate));
            lastScheduledArm.addEvent(event);

            assertNull(calendar.getId());
            getDao().save(calendar);
            assertNotNull("Saved calendar not assigned an ID", calendar.getId());
            savedId = calendar.getId();
        }

        interruptSession();

        ScheduledCalendar reloaded = getDao().getById(savedId);
        assertEquals("Wrong assignment", -2, (int) reloaded.getAssignment().getId());
        assertEquals("Wrong number of arms: " + reloaded.getScheduledArms(), 3, reloaded.getScheduledArms().size());
        assertEquals("Wrong arm 0", -4, (int) reloaded.getScheduledArms().get(0).getArm().getId());
        assertEquals("Wrong arm 1", -3, (int) reloaded.getScheduledArms().get(1).getArm().getId());
        assertEquals("Wrong arm 2", -4, (int) reloaded.getScheduledArms().get(2).getArm().getId());

        assertEquals("Wrong number of events for last arm", 1, reloaded.getScheduledArms().get(2).getEvents().size());
        ScheduledEvent loadedEvent = reloaded.getScheduledArms().get(2).getEvents().get(0);
        assertSameDay("Wrong ideal date", expectedIdealDate, loadedEvent.getIdealDate());
        assertEquals("Wrong planned event", -7, (int) loadedEvent.getPlannedEvent().getId());

        ScheduledEventState currentState = loadedEvent.getCurrentState();
        assertTrue(currentState instanceof Occurred);
        assertSameDay("Wrong current state date", expectedActualDate, ((DatedScheduledEventState) currentState).getDate());
        assertEquals("Wrong current state mode", expectedMode, currentState.getMode());
        assertEquals("Wrong current state reason", expectedReason, currentState.getReason());
    }
}
