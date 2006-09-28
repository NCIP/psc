package edu.northwestern.bioinformatics.studycalendar.dao;

import static edu.nwu.bioinformatics.commons.testing.CoreTestCase.assertSameDay;
import edu.nwu.bioinformatics.commons.DateUtils;

import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventState;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase.*;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class ScheduledCalendarDaoTest extends DaoTestCase {
    private ScheduledCalendarDao dao
        = (ScheduledCalendarDao) getApplicationContext().getBean("scheduledCalendarDao");
    private ParticipantDao participantDao
        = (ParticipantDao) getApplicationContext().getBean("participantDao");
    private ArmDao armDao
        = (ArmDao) getApplicationContext().getBean("armDao");

    public void testGetById() throws Exception {
        ScheduledCalendar cal = dao.getById(-20);

        assertEquals("Wrong assignment", -1, (int) cal.getAssignment().getId());

        assertEquals("Wrong number of arms", 2, cal.getArms().size());
        assertEquals("Wrong arm 0", -4, (int) cal.getArms().get(0).getId());
        assertEquals("Wrong arm 1", -3, (int) cal.getArms().get(1).getId());

        assertEquals("Wrong number of events", 1, cal.getEvents().size());
        ScheduledEvent event = cal.getEvents().get(0);
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
            Arm arm4 = armDao.getById(-4);
            Arm arm3 = armDao.getById(-3);
            calendar.addArm(arm4);
            calendar.addArm(arm3);
            calendar.addArm(arm4);

            ScheduledEvent event = new ScheduledEvent();
            event.setIdealDate(expectedIdealDate);
            event.setActualDate(expectedActualDate);
            event.setPlannedEvent(arm3.getPeriods().iterator().next().getPlannedEvents().get(0));
            event.setState(expectedState);
            calendar.addEvent(event);

            dao.save(calendar);
            assertNotNull("Saved calendar not assigned an ID", calendar.getId());
            savedId = calendar.getId();
        }

        interruptSession();

        ScheduledCalendar reloaded = dao.getById(savedId);
        assertEquals("Wrong assignment", -1, (int) reloaded.getAssignment().getId());
        assertEquals("Wrong number of arms", 3, reloaded.getArms().size());
        assertEquals("Wrong arm 0", -4, (int) reloaded.getArms().get(0).getId());
        assertEquals("Wrong arm 1", -3, (int) reloaded.getArms().get(1).getId());
        assertEquals("Wrong arm 2", -4, (int) reloaded.getArms().get(2).getId());

        assertEquals("Wrong number of events", 1, reloaded.getEvents().size());
        ScheduledEvent loadedEvent = reloaded.getEvents().get(0);
        assertSameDay("Wrong ideal date", expectedIdealDate, loadedEvent.getIdealDate());
        assertSameDay("Wrong actual date", expectedActualDate, loadedEvent.getActualDate());
        assertEquals("Wrong state", expectedState, loadedEvent.getState());
        assertEquals("Wrong planned event", -7, (int) loadedEvent.getPlannedEvent().getId());
    }
}
