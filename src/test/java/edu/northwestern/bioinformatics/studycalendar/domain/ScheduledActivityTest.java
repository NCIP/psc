package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.nwu.bioinformatics.commons.DateUtils;

import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Canceled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Conditional;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Occurred;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.ScheduledEventState;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.NotApplicable;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class ScheduledActivityTest extends StudyCalendarTestCase {
    private ScheduledActivity scheduledActivity = new ScheduledActivity();

    public void testGetActualDateCurrentDate() throws Exception {
        Date expected = DateUtils.createDate(2006, Calendar.AUGUST, 3);
        scheduledActivity.setIdealDate(DateUtils.createDate(2006, Calendar.AUGUST, 1));
        scheduledActivity.changeState(new Scheduled(null, expected));
        assertEquals(expected, scheduledActivity.getActualDate());
    }

    public void testGetActualDateIsPreviousDate() throws Exception {
        Date expected = DateUtils.createDate(2006, Calendar.AUGUST, 3);
        scheduledActivity.setIdealDate(DateUtils.createDate(2006, Calendar.AUGUST, 1));
        scheduledActivity.changeState(new Scheduled(null, DateUtils.createDate(2006, Calendar.AUGUST, 4)));
        scheduledActivity.changeState(new Scheduled(null, expected));
        scheduledActivity.changeState(new Canceled());
        assertEquals(expected, scheduledActivity.getActualDate());
    }

    public void testGetActualDateIsIdealDate() throws Exception {
        Date expected = DateUtils.createDate(2006, Calendar.AUGUST, 3);
        scheduledActivity.setIdealDate(expected);
        assertEquals(expected, scheduledActivity.getActualDate());
    }

    public void testChangeStateWithNoEvents() throws Exception {
        scheduledActivity.changeState(new Canceled("A"));
        assertEquals("Wrong number of states in history", 1, scheduledActivity.getAllStates().size());
        assertTrue("Wrong curent state", scheduledActivity.getCurrentState() instanceof Canceled);
        assertEquals("Wrong curent state", "A", scheduledActivity.getCurrentState().getReason());
        assertEquals(0, scheduledActivity.getPreviousStates().size());
    }

    public void testChangeStateWithCurrentEventOnly() throws Exception {
        scheduledActivity.changeState(new Canceled("A"));
        scheduledActivity.changeState(new Canceled("B"));
        assertEquals("Wrong number of states in history", 2, scheduledActivity.getAllStates().size());
        assertEquals("Wrong current state", "B", scheduledActivity.getCurrentState().getReason());
        assertEquals("Wrong number of previous states", 1, scheduledActivity.getPreviousStates().size());
        assertEquals("Wrong previous state", "A", scheduledActivity.getPreviousStates().get(0).getReason());
    }
    
    public void testChangeStateWithExistingHistory() throws Exception {
        scheduledActivity.changeState(new Canceled("A"));
        scheduledActivity.changeState(new Canceled("B"));
        scheduledActivity.changeState(new Canceled("C"));
        assertEquals("Wrong number of states in history", 3, scheduledActivity.getAllStates().size());
        assertEquals("Wrong current state", "C", scheduledActivity.getCurrentState().getReason());
        assertEquals("Wrong number of previous states", 2, scheduledActivity.getPreviousStates().size());
        assertEquals("Wrong previous state 0", "A", scheduledActivity.getPreviousStates().get(0).getReason());
        assertEquals("Wrong previous state 1", "B", scheduledActivity.getPreviousStates().get(1).getReason());
    }

    public void testGetAllWithNone() throws Exception {
        assertEquals(0, scheduledActivity.getAllStates().size());
    }

    public void testGetAllWithCurrentOnly() throws Exception {
        scheduledActivity.setPreviousStates(null); // paranoia
        scheduledActivity.changeState(new Canceled("A"));
        List<ScheduledEventState> all = scheduledActivity.getAllStates();
        assertEquals(1, all.size());
        assertEquals("A", all.get(0).getReason());
    }
    
    public void testGetAllWithHistory() throws Exception {
        scheduledActivity.changeState(new Canceled("A"));
        scheduledActivity.changeState(new Canceled("B"));
        scheduledActivity.changeState(new Canceled("C"));

        List<ScheduledEventState> all = scheduledActivity.getAllStates();
        assertEquals(3, all.size());
        assertEquals("A", all.get(0).getReason());
        assertEquals("B", all.get(1).getReason());
        assertEquals("C", all.get(2).getReason());
    }

    public void testChangeStateCanceledToOccurredWithOffStudy() throws Exception {
        scheduledActivity.changeState(new Scheduled("New", DateUtils.createDate(2007, Calendar.SEPTEMBER, 2)));
        ScheduledCalendar calendar = new ScheduledCalendar();
        StudyParticipantAssignment assignment = new StudyParticipantAssignment();
        calendar.setAssignment(assignment);
        calendar.addArm(new ScheduledArm());
        calendar.getScheduledArms().get(0).addEvent(scheduledActivity);
        scheduledActivity.changeState(new Canceled());
        assignment.setEndDateEpoch(DateUtils.createDate(2007, Calendar.SEPTEMBER, 1));
        
        scheduledActivity.changeState(new Occurred());
        assertEquals("Wrong states size", 2, scheduledActivity.getAllStates().size());
        assertEquals("Wrong event state", ScheduledEventMode.CANCELED, scheduledActivity.getCurrentState().getMode());
    }

    public void testScheduleConditional() throws Exception {
        scheduledActivity.changeState(new Conditional("New", DateUtils.createDate(2007, Calendar.SEPTEMBER, 2)));
        scheduledActivity.changeState(new Scheduled("Scheduled", DateUtils.createDate(2007, Calendar.SEPTEMBER, 2)));
        scheduledActivity.changeState(new Occurred("Occurred", DateUtils.createDate(2007, Calendar.SEPTEMBER, 2)));

        assertEquals("Conditional flag not set", true, scheduledActivity.isConditionalEvent());
        assertEquals("Wrong previous state size", 2, scheduledActivity.getPreviousStates().size());
    }

    public void testIsConditionalPos() throws Exception {
        Date date = DateUtils.createDate(2006, Calendar.AUGUST, 3);
        scheduledActivity.changeState(new Conditional("Conditional", date));
        scheduledActivity.changeState(new Scheduled("Conditional", date));
        assertTrue("Event should be conditional", scheduledActivity.isConditionalEvent());
    }

    public void testIsConditionalNeg() throws Exception {
        Date date = DateUtils.createDate(2006, Calendar.AUGUST, 3);
        scheduledActivity.changeState(new Scheduled("Conditional", date));
        assertFalse("Event should not be conditional", scheduledActivity.isConditionalEvent());
    }

    public void testIsValidStateChangePos() throws Exception {
        Date date = DateUtils.createDate(2006, Calendar.AUGUST, 3);
        scheduledActivity.changeState(new Conditional("Conditional", date));
        assertTrue("Should be valid new state", scheduledActivity.isValidNewState(Scheduled.class));
    }

    public void testIsValidStateChangeNeg() throws Exception {
        Date date = DateUtils.createDate(2006, Calendar.AUGUST, 3);
        scheduledActivity.changeState(new Conditional("Conditional", date));
        assertFalse("Should not be valid new state", scheduledActivity.isValidNewState(Canceled.class));
    }

    public void testUnscheduleScheduledEvent() throws Exception {
        scheduledActivity.changeState(new Scheduled());
        assertEquals("Test setup failure", 1, scheduledActivity.getAllStates().size());

        scheduledActivity.unscheduleIfOutstanding("Testing");
        assertEquals("State not changed", 2, scheduledActivity.getAllStates().size());
        assertEquals("State not changed", ScheduledEventMode.CANCELED,
            scheduledActivity.getCurrentState().getMode());
        assertEquals("New state has wrong reason", "Testing",
            scheduledActivity.getCurrentState().getReason());
    }

    public void testUnscheduleConditionalEvent() throws Exception {
        scheduledActivity.changeState(new Conditional());
        assertEquals("Test setup failure", 1, scheduledActivity.getAllStates().size());

        scheduledActivity.unscheduleIfOutstanding("Testing");
        assertEquals("State not changed", 2, scheduledActivity.getAllStates().size());
        assertEquals("State not changed", ScheduledEventMode.NOT_APPLICABLE,
            scheduledActivity.getCurrentState().getMode());
        assertEquals("New state has wrong reason", "Testing",
            scheduledActivity.getCurrentState().getReason());
    }

    public void testUnscheduleCanceledEvent() throws Exception {
        scheduledActivity.changeState(new Canceled());
        assertEquals("Test setup failure", 1, scheduledActivity.getAllStates().size());

        scheduledActivity.unscheduleIfOutstanding("Testing");
        assertEquals("State changed", 1, scheduledActivity.getAllStates().size());
        assertEquals("State changed", ScheduledEventMode.CANCELED,
            scheduledActivity.getCurrentState().getMode());
    }

    public void testUnscheduleOccurredEvent() throws Exception {
        scheduledActivity.changeState(new Occurred());
        assertEquals("Test setup failure", 1, scheduledActivity.getAllStates().size());

        scheduledActivity.unscheduleIfOutstanding("Testing");
        assertEquals("State changed", 1, scheduledActivity.getAllStates().size());
        assertEquals("State changed", ScheduledEventMode.OCCURRED,
            scheduledActivity.getCurrentState().getMode());
    }

    public void testUnscheduleNotApplicableEvent() throws Exception {
        scheduledActivity.changeState(new NotApplicable());
        assertEquals("Test setup failure", 1, scheduledActivity.getAllStates().size());

        scheduledActivity.unscheduleIfOutstanding("Testing");
        assertEquals("State changed", 1, scheduledActivity.getAllStates().size());
        assertEquals("State changed", ScheduledEventMode.NOT_APPLICABLE,
            scheduledActivity.getCurrentState().getMode());
    }
}
