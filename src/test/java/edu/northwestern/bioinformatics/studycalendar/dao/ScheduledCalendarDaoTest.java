package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.nwu.bioinformatics.commons.DateUtils;
import static edu.nwu.bioinformatics.commons.testing.CoreTestCase.assertSameDay;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Occurred;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.ScheduledEventState;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.DatedScheduledEventState;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Canceled;
import static edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase.assertDayOfDate;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;

/**
 * @author Rhett Sutphin
 */
public class ScheduledCalendarDaoTest extends ContextDaoTestCase<ScheduledCalendarDao> {
    private ParticipantDao participantDao
        = (ParticipantDao) getApplicationContext().getBean("participantDao");
    private ArmDao armDao
        = (ArmDao) getApplicationContext().getBean("armDao");
    private AmendmentDao amendmentDao
        = (AmendmentDao) getApplicationContext().getBean("amendmentDao");

    public void testGetById() throws Exception {
        ScheduledCalendar cal = getDao().getById(-20);
        assertScheduledCalendar20(cal);
    }

    private void assertScheduledCalendar20(ScheduledCalendar cal) {
        assertEquals("Wrong assignment", -1, (int) cal.getAssignment().getId());

        assertEquals("Wrong number of arms", 2, cal.getScheduledArms().size());
        assertEquals("Wrong arm 0", -22, (int) cal.getScheduledArms().get(0).getId());
        assertEquals("Wrong arm 1", -21, (int) cal.getScheduledArms().get(1).getId());

        assertEquals("Wrong number of events in arm 0", 5, cal.getScheduledArms().get(0).getEvents().size());
        assertEquals("Wrong number of events in arm 1", 0, cal.getScheduledArms().get(1).getEvents().size());

        ScheduledEvent event = cal.getScheduledArms().get(0).getEvents().get(0);
        assertEquals("Wrong event", -10, (int) event.getId());
        assertEquals("Wrong base event for event", -6, (int) event.getPlannedEvent().getId());
        assertDayOfDate("Wrong ideal date", 2006, Calendar.OCTOBER, 31, event.getIdealDate());
        assertEquals("Wrong notes", "Boo!", event.getNotes());
        assertEquals("Wrong amendment", -17, (int) event.getSourceAmendment().getId());

        ScheduledEventState currentState = event.getCurrentState();
        assertTrue(currentState instanceof Occurred);
        assertDayOfDate("Wrong current state date", 2006, Calendar.OCTOBER, 25, ((DatedScheduledEventState) currentState).getDate());
        assertEquals("Wrong current state mode", ScheduledEventMode.OCCURRED, currentState.getMode());
        assertEquals("Wrong current state reason", "Success", currentState.getReason());

        List<ScheduledEventState> states = cal.getScheduledArms().get(0).getEvents().get(0).getAllStates();
        assertEquals("Wrong number of states", 4, states.size());
        assertEventState(-11, ScheduledEventMode.SCHEDULED, "Initial input", DateUtils.createDate(2006, Calendar.OCTOBER, 22), states.get(0));
        assertEventState(-12, ScheduledEventMode.CANCELED,  "Called to cancel", null, states.get(1));
        assertEventState(-13, ScheduledEventMode.SCHEDULED, "Called to reschedule", DateUtils.createDate(2006, Calendar.OCTOBER, 25), states.get(2));
        assertEventState(null, ScheduledEventMode.OCCURRED, "Success", DateUtils.createDate(2006, Calendar.OCTOBER, 25), states.get(3));
    }

    public void testChangeStateAndSave() throws Exception {
        {
            ScheduledCalendar cal = getDao().getById(-20);
            cal.getScheduledArms().get(0).getEvents().get(0).changeState(new Canceled("For great victory"));
            getDao().save(cal);
        }

        interruptSession();

        {
            ScheduledCalendar loaded = getDao().getById(-20);
            List<ScheduledEventState> states = loaded.getScheduledArms().get(0).getEvents().get(0).getAllStates();
            assertEquals("Wrong number of states", 5, states.size());
            // second to last should now have an ID
            assertNotNull(states.get(3).getId());
            assertEventState(states.get(3).getId(), ScheduledEventMode.OCCURRED, "Success", DateUtils.createDate(2006, Calendar.OCTOBER, 25), states.get(3));

            assertEventState(null, ScheduledEventMode.CANCELED, "For great victory", null, states.get(4));
        }
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
        Activity expectedActivity = Fixtures.setId(-100, Fixtures.createNamedInstance("Infusion", Activity.class));
        expectedActivity.setVersion(0);
        Amendment expectedAmendment = amendmentDao.getById(-17);

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
            event.setActivity(expectedActivity);
            event.setSourceAmendment(expectedAmendment);
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

        Activity currentActivity = reloaded.getScheduledArms().get(2).getEvents().get(0).getActivity();
        assertNotNull("Activity null", currentActivity);
        assertEquals("Wrong Activity", expectedActivity.getName(), currentActivity.getName());
    }

    public void testInitialize() throws Exception {
        ScheduledCalendar cal = getDao().getById(-20);
        getDao().initialize(cal);
        interruptSession();

        assertScheduledCalendar20(cal);
    }
}
