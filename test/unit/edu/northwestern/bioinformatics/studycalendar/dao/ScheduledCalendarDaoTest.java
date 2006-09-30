package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.nwu.bioinformatics.commons.DateUtils;
import static edu.nwu.bioinformatics.commons.testing.CoreTestCase.assertSameDay;

import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventState;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase.assertDayOfDate;

import java.util.Calendar;
import java.util.Date;

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
        assertDayOfDate("Wrong actual date", 2006, Calendar.OCTOBER, 25, event.getActualDate());
        assertEquals("Wrong notes", "Boo!", event.getNotes());
        assertEquals("Wrong event state", ScheduledEventState.OCCURRED, event.getState());
    }

    public void testSave() throws Exception {
        int savedId;

        Date expectedIdealDate = DateUtils.createDate(2006, Calendar.SEPTEMBER, 20);
        Date expectedActualDate = DateUtils.createDate(2006, Calendar.SEPTEMBER, 22);
        ScheduledEventState expectedState = ScheduledEventState.OCCURRED;

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
            event.setActualDate(expectedActualDate);
            event.setPlannedEvent(arm3.getPeriods().iterator().next().getPlannedEvents().get(0));
            event.setState(expectedState);
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
        assertSameDay("Wrong actual date", expectedActualDate, loadedEvent.getActualDate());
        assertEquals("Wrong state", expectedState, loadedEvent.getState());
        assertEquals("Wrong planned event", -7, (int) loadedEvent.getPlannedEvent().getId());
    }
}
