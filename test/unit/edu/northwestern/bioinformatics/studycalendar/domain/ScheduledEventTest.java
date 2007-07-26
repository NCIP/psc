package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.nwu.bioinformatics.commons.DateUtils;

import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Canceled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.ScheduledEventState;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class ScheduledEventTest extends StudyCalendarTestCase {
    private ScheduledEvent scheduledEvent = new ScheduledEvent();

    public void testGetActualDateCurrentDate() throws Exception {
        Date expected = DateUtils.createDate(2006, Calendar.AUGUST, 3);
        scheduledEvent.setIdealDate(DateUtils.createDate(2006, Calendar.AUGUST, 1));
        scheduledEvent.changeState(new Scheduled(null, expected));
        assertEquals(expected, scheduledEvent.getActualDate());
    }

    public void testGetActualDateIsPreviousDate() throws Exception {
        Date expected = DateUtils.createDate(2006, Calendar.AUGUST, 3);
        scheduledEvent.setIdealDate(DateUtils.createDate(2006, Calendar.AUGUST, 1));
        scheduledEvent.changeState(new Scheduled(null, DateUtils.createDate(2006, Calendar.AUGUST, 4)));
        scheduledEvent.changeState(new Scheduled(null, expected));
        scheduledEvent.changeState(new Canceled());
        assertEquals(expected, scheduledEvent.getActualDate());
    }

    public void testGetActualDateIsIdealDate() throws Exception {
        Date expected = DateUtils.createDate(2006, Calendar.AUGUST, 3);
        scheduledEvent.setIdealDate(expected);
        assertEquals(expected, scheduledEvent.getActualDate());
    }

    public void testChangeStateWithNoEvents() throws Exception {
        scheduledEvent.changeState(new Canceled("A"));
        assertEquals("Wrong number of states in history", 1, scheduledEvent.getAllStates().size());
        assertTrue("Wrong curent state", scheduledEvent.getCurrentState() instanceof Canceled);
        assertEquals("Wrong curent state", "A", scheduledEvent.getCurrentState().getReason());
        assertEquals(0, scheduledEvent.getPreviousStates().size());
    }

    public void testChangeStateWithCurrentEventOnly() throws Exception {
        scheduledEvent.changeState(new Canceled("A"));
        scheduledEvent.changeState(new Canceled("B"));
        assertEquals("Wrong number of states in history", 2, scheduledEvent.getAllStates().size());
        assertEquals("Wrong current state", "B", scheduledEvent.getCurrentState().getReason());
        assertEquals("Wrong number of previous states", 1, scheduledEvent.getPreviousStates().size());
        assertEquals("Wrong previous state", "A", scheduledEvent.getPreviousStates().get(0).getReason());
    }
    
    public void testChangeStateWithExistingHistory() throws Exception {
        scheduledEvent.changeState(new Canceled("A"));
        scheduledEvent.changeState(new Canceled("B"));
        scheduledEvent.changeState(new Canceled("C"));
        assertEquals("Wrong number of states in history", 3, scheduledEvent.getAllStates().size());
        assertEquals("Wrong current state", "C", scheduledEvent.getCurrentState().getReason());
        assertEquals("Wrong number of previous states", 2, scheduledEvent.getPreviousStates().size());
        assertEquals("Wrong previous state 0", "A", scheduledEvent.getPreviousStates().get(0).getReason());
        assertEquals("Wrong previous state 1", "B", scheduledEvent.getPreviousStates().get(1).getReason());
    }

    public void testGetAllWithNone() throws Exception {
        assertEquals(0, scheduledEvent.getAllStates().size());
    }

    public void testGetAllWithCurrentOnly() throws Exception {
        scheduledEvent.changeState(new Canceled("A"));
        List<ScheduledEventState> all = scheduledEvent.getAllStates();
        assertEquals(1, all.size());
        assertEquals("A", all.get(0).getReason());
    }
    
    public void testGetAllWithHistory() throws Exception {
        scheduledEvent.changeState(new Canceled("A"));
        scheduledEvent.changeState(new Canceled("B"));
        scheduledEvent.changeState(new Canceled("C"));

        List<ScheduledEventState> all = scheduledEvent.getAllStates();
        assertEquals(3, all.size());
        assertEquals("A", all.get(0).getReason());
        assertEquals("B", all.get(1).getReason());
        assertEquals("C", all.get(2).getReason());
    }

    public void testGetCurrentDate() throws Exception {
        Date currentDate = scheduledEvent.getCurrentDate();

        Calendar c2 = Calendar.getInstance();

        assertSameDay(currentDate, c2.getTime());
    }
}
