package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Canceled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Conditional;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.NotApplicable;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Occurred;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import junit.framework.TestCase;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class ScheduledActivityTest extends TestCase {
    private ScheduledActivity scheduledActivity = new ScheduledActivity();
    private PlannedActivity plannedActivity = new PlannedActivity();

    public void testGetActualDateCurrentDate() throws Exception {
        Date expected = DateTools.createDate(2006, Calendar.AUGUST, 3);
        scheduledActivity.setIdealDate(DateTools.createDate(2006, Calendar.AUGUST, 1));
        scheduledActivity.changeState(new Scheduled(null, expected));
        assertEquals(expected, scheduledActivity.getActualDate());
    }

    public void testGetActualDateIsPreviousDate() throws Exception {
        Date expected = DateTools.createDate(2006, Calendar.AUGUST, 3);
        scheduledActivity.setIdealDate(DateTools.createDate(2006, Calendar.AUGUST, 1));
        scheduledActivity.changeState(new Scheduled(null, DateTools.createDate(2006, Calendar.AUGUST, 4)));
        scheduledActivity.changeState(new Scheduled(null, expected));
        scheduledActivity.changeState(new Canceled(null, expected));
        assertEquals(expected, scheduledActivity.getActualDate());
    }

    public void testGetActualDateIsIdealDate() throws Exception {
        Date expected = DateTools.createDate(2006, Calendar.AUGUST, 3);
        scheduledActivity.setIdealDate(expected);
        assertEquals(expected, scheduledActivity.getActualDate());
    }

    public void testGetDayNumberWithRepetition() throws Exception {
        plannedActivity.setDay(1);
        scheduledActivity.setPlannedActivity(plannedActivity);
        scheduledActivity.setRepetitionNumber(null);
        assertEquals(null,scheduledActivity.getDayNumber());
    }
    
    public void testGetDayNumberWithPeriod() throws Exception {
        scheduledActivity.setPlannedActivity(null);
        scheduledActivity.setRepetitionNumber(3);
        assertEquals(null,scheduledActivity.getDayNumber());
    }

    public void testChangeStateWithNoEvents() throws Exception {
        scheduledActivity.changeState(new Canceled("A",DateTools.createDate(2006, Calendar.AUGUST, 3)));
        assertEquals("Wrong number of states in history", 1, scheduledActivity.getAllStates().size());
        assertTrue("Wrong curent state", scheduledActivity.getCurrentState() instanceof Canceled);
        assertEquals("Wrong curent state", "A", scheduledActivity.getCurrentState().getReason());
        assertEquals(0, scheduledActivity.getPreviousStates().size());
    }

    public void testChangeStateWithCurrentEventOnly() throws Exception {
        scheduledActivity.changeState(new Canceled("A",DateTools.createDate(2006, Calendar.AUGUST, 3)));
        scheduledActivity.changeState(new Canceled("B",DateTools.createDate(2006, Calendar.AUGUST, 3)));
        assertEquals("Wrong number of states in history", 2, scheduledActivity.getAllStates().size());
        assertEquals("Wrong current state", "B", scheduledActivity.getCurrentState().getReason());
        assertEquals("Wrong number of previous states", 1, scheduledActivity.getPreviousStates().size());
        assertEquals("Wrong previous state", "A", scheduledActivity.getPreviousStates().get(0).getReason());
    }
    
    public void testChangeStateWithExistingHistory() throws Exception {
        scheduledActivity.changeState(new Canceled("A",DateTools.createDate(2006, Calendar.AUGUST, 3)));
        scheduledActivity.changeState(new Canceled("B",DateTools.createDate(2006, Calendar.AUGUST, 3)));
        scheduledActivity.changeState(new Canceled("C",DateTools.createDate(2006, Calendar.AUGUST, 3)));
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
        scheduledActivity.changeState(new Canceled("A",DateTools.createDate(2006, Calendar.AUGUST, 3)));
        List<ScheduledActivityState> all = scheduledActivity.getAllStates();
        assertEquals(1, all.size());
        assertEquals("A", all.get(0).getReason());
    }
    
    public void testGetAllWithHistory() throws Exception {
        scheduledActivity.changeState(new Canceled("A",DateTools.createDate(2006, Calendar.AUGUST, 3)));
        scheduledActivity.changeState(new Canceled("B",DateTools.createDate(2006, Calendar.AUGUST, 3)));
        scheduledActivity.changeState(new Canceled("C",DateTools.createDate(2006, Calendar.AUGUST, 3)));

        List<ScheduledActivityState> all = scheduledActivity.getAllStates();
        assertEquals(3, all.size());
        assertEquals("A", all.get(0).getReason());
        assertEquals("B", all.get(1).getReason());
        assertEquals("C", all.get(2).getReason());
    }

    public void testChangeStateCanceledToOccurredWithOffStudy() throws Exception {
        scheduledActivity.changeState(new Scheduled("New", DateTools.createDate(2007, Calendar.SEPTEMBER, 2)));
        ScheduledCalendar calendar = new ScheduledCalendar();
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        calendar.setAssignment(assignment);
        calendar.addStudySegment(new ScheduledStudySegment());
        calendar.getScheduledStudySegments().get(0).addEvent(scheduledActivity);
        scheduledActivity.changeState(new Canceled("Canceled",DateTools.createDate(2007, Calendar.SEPTEMBER, 2)));
        assignment.setEndDateEpoch(DateTools.createDate(2007, Calendar.SEPTEMBER, 1));
        
        //scheduledActivity.changeState(new Occurred());
        assertEquals("Wrong states size", 2, scheduledActivity.getAllStates().size());
        assertEquals("Wrong event state", ScheduledActivityMode.CANCELED, scheduledActivity.getCurrentState().getMode());
    }

    public void testScheduleConditional() throws Exception {
        scheduledActivity.changeState(new Conditional("New", DateTools.createDate(2007, Calendar.SEPTEMBER, 2)));
        scheduledActivity.changeState(new Scheduled("Scheduled", DateTools.createDate(2007, Calendar.SEPTEMBER, 2)));
        scheduledActivity.changeState(new Occurred("Occurred", DateTools.createDate(2007, Calendar.SEPTEMBER, 2)));

        assertEquals("Conditional flag not set", true, scheduledActivity.isConditionalEvent());
        assertEquals("Wrong previous state size", 2, scheduledActivity.getPreviousStates().size());
    }

    public void testIsConditionalPos() throws Exception {
        Date date = DateTools.createDate(2006, Calendar.AUGUST, 3);
        scheduledActivity.changeState(new Conditional("Conditional", date));
        scheduledActivity.changeState(new Scheduled("Conditional", date));
        assertTrue("Event should be conditional", scheduledActivity.isConditionalEvent());
    }

    public void testIsConditionalNeg() throws Exception {
        Date date = DateTools.createDate(2006, Calendar.AUGUST, 3);
        scheduledActivity.changeState(new Scheduled("Conditional", date));
        assertFalse("Event should not be conditional", scheduledActivity.isConditionalEvent());
    }

    public void testIsValidStateChangePos() throws Exception {
        Date date = DateTools.createDate(2006, Calendar.AUGUST, 3);
        scheduledActivity.changeState(new Conditional("Conditional", date));
        assertTrue("Should be valid new state", scheduledActivity.isValidNewState(Scheduled.class));
    }

    public void testIsValidStateChangeNeg() throws Exception {
        Date date = DateTools.createDate(2006, Calendar.AUGUST, 3);
        scheduledActivity.changeState(new Conditional("Conditional", date));
        assertFalse("Should not be valid new state", scheduledActivity.isValidNewState(Canceled.class));
    }

    public void testUnscheduleScheduledActivity() throws Exception {
        scheduledActivity.changeState(new Scheduled());
        assertEquals("Test setup failure", 1, scheduledActivity.getAllStates().size());

        scheduledActivity.unscheduleIfOutstanding("Testing");
        assertEquals("State not changed", 2, scheduledActivity.getAllStates().size());
        assertEquals("State not changed", ScheduledActivityMode.CANCELED,
            scheduledActivity.getCurrentState().getMode());
        assertEquals("New state has wrong reason", "Testing",
            scheduledActivity.getCurrentState().getReason());
    }

    public void testUnscheduleConditionalEvent() throws Exception {
        scheduledActivity.changeState(new Conditional());
        assertEquals("Test setup failure", 1, scheduledActivity.getAllStates().size());

        scheduledActivity.unscheduleIfOutstanding("Testing");
        assertEquals("State not changed", 2, scheduledActivity.getAllStates().size());
        assertEquals("State not changed", ScheduledActivityMode.NOT_APPLICABLE,
            scheduledActivity.getCurrentState().getMode());
        assertEquals("New state has wrong reason", "Testing",
            scheduledActivity.getCurrentState().getReason());
    }

    public void testUnscheduleCanceledEvent() throws Exception {
        scheduledActivity.changeState(new Canceled());
        assertEquals("Test setup failure", 1, scheduledActivity.getAllStates().size());

        scheduledActivity.unscheduleIfOutstanding("Testing");
        assertEquals("State changed", 1, scheduledActivity.getAllStates().size());
        assertEquals("State changed", ScheduledActivityMode.CANCELED,
            scheduledActivity.getCurrentState().getMode());
    }

    public void testUnscheduleOccurredEvent() throws Exception {
        scheduledActivity.changeState(new Occurred());
        assertEquals("Test setup failure", 1, scheduledActivity.getAllStates().size());

        scheduledActivity.unscheduleIfOutstanding("Testing");
        assertEquals("State changed", 1, scheduledActivity.getAllStates().size());
        assertEquals("State changed", ScheduledActivityMode.OCCURRED,
            scheduledActivity.getCurrentState().getMode());
    }

    public void testUnscheduleNotApplicableEvent() throws Exception {
        scheduledActivity.changeState(new NotApplicable());
        assertEquals("Test setup failure", 1, scheduledActivity.getAllStates().size());

        scheduledActivity.unscheduleIfOutstanding("Testing");
        assertEquals("State changed", 1, scheduledActivity.getAllStates().size());
        assertEquals("State changed", ScheduledActivityMode.NOT_APPLICABLE,
            scheduledActivity.getCurrentState().getMode());
    }

    public void testIsOutstandingWhenOutstanding() throws Exception {
        scheduledActivity.changeState(new Conditional());
        assertTrue(scheduledActivity.isOutstanding());
    }

    public void testIsOutstandingWhenCompleted() throws Exception {
        scheduledActivity.changeState(new Canceled());
        assertFalse(scheduledActivity.isOutstanding());
    }
}
