package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.lang.DateTools;
import junit.framework.TestCase;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.*;

/**
 * @author Rhett Sutphin
 */
public class ScheduledActivityTest extends TestCase {
    private ScheduledActivity scheduledActivity, sa0, sa1;
    private PlannedActivity plannedActivity;
    private Activity activityA = createActivity("A");
    private Activity activityB = createActivity("B");

    public void setUp() throws Exception {
        super.setUp();
        plannedActivity = new PlannedActivity();
        scheduledActivity = new ScheduledActivity();
        sa0 = new ScheduledActivity(); sa1 = new ScheduledActivity();
        sa0.setActivity(activityA);
        sa0.changeState(ScheduledActivityMode.SCHEDULED.createStateInstance(DateTools.createDate(2008, Calendar.MARCH, 17), null));
        sa1.setActivity(activityB);
        sa1.changeState(ScheduledActivityMode.SCHEDULED.createStateInstance(DateTools.createDate(2008, Calendar.MARCH, 17), null));
    }

    public void testGetActualDateCurrentDate() throws Exception {
        Date expected = DateTools.createDate(2006, Calendar.AUGUST, 3);
        scheduledActivity.setIdealDate(DateTools.createDate(2006, Calendar.AUGUST, 1));
        scheduledActivity.changeState(ScheduledActivityMode.SCHEDULED.createStateInstance(expected, null));
        assertEquals(expected, scheduledActivity.getActualDate());
    }

    public void testGetActualDateIsPreviousDateIfCurrentNotSet() throws Exception {
        Date expected = DateTools.createDate(2006, Calendar.AUGUST, 3);
        scheduledActivity.setIdealDate(DateTools.createDate(2006, Calendar.AUGUST, 1));
        scheduledActivity.changeState(ScheduledActivityMode.SCHEDULED.createStateInstance(expected, null));
        scheduledActivity.changeState(ScheduledActivityMode.CANCELED.createStateInstance(new Date(), null));
        // this shouldn't happen in new data, but there could still be some out there
        scheduledActivity.getCurrentState().setDate(null);
        assertEquals(expected, scheduledActivity.getActualDate());
    }

    public void testGetActualDateIsIdealDateWhenNothingElseAvailable() throws Exception {
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
        scheduledActivity.changeState(ScheduledActivityMode.CANCELED.createStateInstance(DateTools.createDate(2006, Calendar.AUGUST, 3), "A"));
        assertEquals("Wrong number of states in history", 1, scheduledActivity.getAllStates().size());
        assertEquals("Wrong current state", ScheduledActivityMode.CANCELED, scheduledActivity.getCurrentState().getMode());
        assertEquals("Wrong current state", "A", scheduledActivity.getCurrentState().getReason());
        assertEquals(0, scheduledActivity.getPreviousStates().size());
    }

    public void testChangeStateWithCurrentEventOnly() throws Exception {
        scheduledActivity.changeState(ScheduledActivityMode.CANCELED.createStateInstance(DateTools.createDate(2006, Calendar.AUGUST, 3), "A"));
        scheduledActivity.changeState(ScheduledActivityMode.CANCELED.createStateInstance(DateTools.createDate(2006, Calendar.AUGUST, 3), "B"));
        assertEquals("Wrong number of states in history", 2, scheduledActivity.getAllStates().size());
        assertEquals("Wrong current state", "B", scheduledActivity.getCurrentState().getReason());
        assertEquals("Wrong number of previous states", 1, scheduledActivity.getPreviousStates().size());
        assertEquals("Wrong previous state", "A", scheduledActivity.getPreviousStates().get(0).getReason());
    }
    
    public void testChangeStateWithExistingHistory() throws Exception {
        scheduledActivity.changeState(ScheduledActivityMode.CANCELED.createStateInstance(DateTools.createDate(2006, Calendar.AUGUST, 3), "A"));
        scheduledActivity.changeState(ScheduledActivityMode.CANCELED.createStateInstance(DateTools.createDate(2006, Calendar.AUGUST, 3), "B"));
        scheduledActivity.changeState(ScheduledActivityMode.CANCELED.createStateInstance(DateTools.createDate(2006, Calendar.AUGUST, 3), "C"));
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
        scheduledActivity.changeState(ScheduledActivityMode.CANCELED.createStateInstance(DateTools.createDate(2006, Calendar.AUGUST, 3), "A"));
        List<ScheduledActivityState> all = scheduledActivity.getAllStates();
        assertEquals(1, all.size());
        assertEquals("A", all.get(0).getReason());
    }
    
    public void testGetAllWithHistory() throws Exception {
        scheduledActivity.changeState(ScheduledActivityMode.CANCELED.createStateInstance(DateTools.createDate(2006, Calendar.AUGUST, 3), "A"));
        scheduledActivity.changeState(ScheduledActivityMode.CANCELED.createStateInstance(DateTools.createDate(2006, Calendar.AUGUST, 3), "B"));
        scheduledActivity.changeState(ScheduledActivityMode.CANCELED.createStateInstance(DateTools.createDate(2006, Calendar.AUGUST, 3), "C"));

        List<ScheduledActivityState> all = scheduledActivity.getAllStates();
        assertEquals(3, all.size());
        assertEquals("A", all.get(0).getReason());
        assertEquals("B", all.get(1).getReason());
        assertEquals("C", all.get(2).getReason());
    }

    public void testChangeStateCanceledToOccurredWithOffStudy() throws Exception {
        scheduledActivity.changeState(ScheduledActivityMode.SCHEDULED.createStateInstance(DateTools.createDate(2007, Calendar.SEPTEMBER, 2), "New"));
        ScheduledCalendar calendar = new ScheduledCalendar();
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        calendar.setAssignment(assignment);
        calendar.addStudySegment(new ScheduledStudySegment());
        calendar.getScheduledStudySegments().get(0).addEvent(scheduledActivity);
        scheduledActivity.changeState(ScheduledActivityMode.CANCELED.createStateInstance(DateTools.createDate(2007, Calendar.SEPTEMBER, 2), "Canceled"));
        assignment.setEndDate(DateTools.createDate(2007, Calendar.SEPTEMBER, 1));
        
        //scheduledActivity.changeState(new Occurred());
        assertEquals("Wrong states size", 2, scheduledActivity.getAllStates().size());
        assertEquals("Wrong event state", ScheduledActivityMode.CANCELED, scheduledActivity.getCurrentState().getMode());
    }

    public void testScheduleConditional() throws Exception {
        scheduledActivity.changeState(ScheduledActivityMode.CONDITIONAL.createStateInstance(DateTools.createDate(2007, Calendar.SEPTEMBER, 2), "New"));
        scheduledActivity.changeState(ScheduledActivityMode.SCHEDULED.createStateInstance(DateTools.createDate(2007, Calendar.SEPTEMBER, 2), "Scheduled"));
        scheduledActivity.changeState(ScheduledActivityMode.OCCURRED.createStateInstance(DateTools.createDate(2007, Calendar.SEPTEMBER, 2), "Occurred"));

        assertEquals("Conditional flag not set", true, scheduledActivity.isConditionalEvent());
        assertEquals("Wrong previous state size", 2, scheduledActivity.getPreviousStates().size());
    }

    public void testIsConditionalPos() throws Exception {
        Date date = DateTools.createDate(2006, Calendar.AUGUST, 3);
        scheduledActivity.changeState(ScheduledActivityMode.CONDITIONAL.createStateInstance(date, "Conditional"));
        scheduledActivity.changeState(ScheduledActivityMode.SCHEDULED.createStateInstance(date, "Conditional"));
        assertTrue("Event should be conditional", scheduledActivity.isConditionalEvent());
    }

    public void testIsConditionalNeg() throws Exception {
        Date date = DateTools.createDate(2006, Calendar.AUGUST, 3);
        scheduledActivity.changeState(ScheduledActivityMode.SCHEDULED.createStateInstance(date, "Conditional"));
        assertFalse("Event should not be conditional", scheduledActivity.isConditionalEvent());
    }

    public void testUnscheduleScheduledActivity() throws Exception {
        Date scheduledOn = DateTools.createDate(2005, Calendar.MAY, 5);
        scheduledActivity.changeState(ScheduledActivityMode.SCHEDULED.createStateInstance(scheduledOn, null));
        assertEquals("Test setup failure", 1, scheduledActivity.getAllStates().size());

        scheduledActivity.unscheduleIfOutstanding("Testing");
        assertEquals("State not changed", 2, scheduledActivity.getAllStates().size());
        assertEquals("State not changed", ScheduledActivityMode.CANCELED,
            scheduledActivity.getCurrentState().getMode());
        assertEquals("New state has wrong reason", "Testing",
            scheduledActivity.getCurrentState().getReason());
        assertEquals("New state does not preserve previous date", scheduledOn,
            scheduledActivity.getCurrentState().getDate());
    }

    public void testUnscheduleConditionalActivity() throws Exception {
        Date conditionalFor = DateTools.createDate(2005, Calendar.MAY, 9);
        scheduledActivity.changeState(ScheduledActivityMode.CONDITIONAL.createStateInstance(conditionalFor, null));
        assertEquals("Test setup failure", 1, scheduledActivity.getAllStates().size());

        scheduledActivity.unscheduleIfOutstanding("Testing");
        assertEquals("State not changed", 2, scheduledActivity.getAllStates().size());
        assertEquals("State not changed", ScheduledActivityMode.NOT_APPLICABLE,
            scheduledActivity.getCurrentState().getMode());
        assertEquals("New state has wrong reason", "Testing",
            scheduledActivity.getCurrentState().getReason());
        assertEquals("New state does not preserve previous date", conditionalFor,
            scheduledActivity.getCurrentState().getDate());
    }

    public void testUnscheduleCanceledEvent() throws Exception {
        scheduledActivity.changeState(ScheduledActivityMode.CANCELED.createStateInstance());
        assertEquals("Test setup failure", 1, scheduledActivity.getAllStates().size());

        scheduledActivity.unscheduleIfOutstanding("Testing");
        assertEquals("State changed", 1, scheduledActivity.getAllStates().size());
        assertEquals("State changed", ScheduledActivityMode.CANCELED,
            scheduledActivity.getCurrentState().getMode());
    }

    public void testUnscheduleOccurredEvent() throws Exception {
        scheduledActivity.changeState(ScheduledActivityMode.OCCURRED.createStateInstance());
        assertEquals("Test setup failure", 1, scheduledActivity.getAllStates().size());

        scheduledActivity.unscheduleIfOutstanding("Testing");
        assertEquals("State changed", 1, scheduledActivity.getAllStates().size());
        assertEquals("State changed", ScheduledActivityMode.OCCURRED,
            scheduledActivity.getCurrentState().getMode());
    }

    public void testUnscheduleNotApplicableEvent() throws Exception {
        scheduledActivity.changeState(ScheduledActivityMode.NOT_APPLICABLE.createStateInstance());
        assertEquals("Test setup failure", 1, scheduledActivity.getAllStates().size());

        scheduledActivity.unscheduleIfOutstanding("Testing");
        assertEquals("State changed", 1, scheduledActivity.getAllStates().size());
        assertEquals("State changed", ScheduledActivityMode.NOT_APPLICABLE,
            scheduledActivity.getCurrentState().getMode());
    }

    public void testIsOutstandingWhenOutstanding() throws Exception {
        scheduledActivity.changeState(ScheduledActivityMode.CONDITIONAL.createStateInstance());
        assertTrue(scheduledActivity.isOutstanding());
    }

    public void testIsOutstandingWhenCompleted() throws Exception {
        scheduledActivity.changeState(ScheduledActivityMode.CANCELED.createStateInstance());
        assertFalse(scheduledActivity.isOutstanding());
    }

    public void testNaturalOrderConsidersStudy() throws Exception {
        sa0.setPlannedActivity(plannedActivityFromStudy("Y"));
        sa1.setPlannedActivity(plannedActivityFromStudy("X"));

        assertPositive(sa0.compareTo(sa1));
        assertNegative(sa1.compareTo(sa0));
    }
    
    public void testNaturalOrderConsidersPAWeight() throws Exception {
        PlannedActivity pa0 = plannedActivityFromStudy("X");
        PlannedActivity pa1 = plannedActivityFromStudy("X");
        pa1.setWeight(4);
        sa0.setPlannedActivity(pa0);
        sa1.setPlannedActivity(pa1);

        assertPositive(sa0.compareTo(sa1));
        assertNegative(sa1.compareTo(sa0));
    }

    public void testCompareWithNoPlannedActivityDoesNotErrorOut() throws Exception {
        sa0.setPlannedActivity(null); sa1.setPlannedActivity(null);
        assertNegative(sa0.compareTo(sa1));
    }

    public void testCompareWithDisassociatedPlannedActivityDoesNotErrorOut() throws Exception {
        sa0.setPlannedActivity(new PlannedActivity());
        sa1.setPlannedActivity(new PlannedActivity());

        assertNegative(sa0.compareTo(sa1));
    }

    public void testNaturalOrderConsidersState() throws Exception {
        PlannedActivity pa = plannedActivityFromStudy("X");
        sa0.setPlannedActivity(pa);
        sa1.setPlannedActivity(pa);
        sa1.setActivity(sa0.getActivity());
        sa0.changeState(ScheduledActivityMode.CANCELED.createStateInstance(sa0.getCurrentState().getDate(), null));
        sa1.changeState(ScheduledActivityMode.MISSED.createStateInstance(sa1.getCurrentState().getDate(), null));

        assertNegative(sa0.compareTo(sa1));
        assertPositive(sa1.compareTo(sa0));
    }

    public void testNaturalOrderSortsByAssignmentIdWhenOtherwiseEqual() throws Exception {
        PlannedActivity pa = plannedActivityFromStudy("X");
        sa0.setPlannedActivity(pa);

        ScheduledActivity sa0prime = new ScheduledActivity();
        sa0prime.setActivity(sa0.getActivity());
        sa0prime.setPlannedActivity(pa);

        setId(2, putActivityInAssignment(sa0));
        setId(1, putActivityInAssignment(sa0prime));

        assertPositive(sa0.compareTo(sa0prime));
        assertNegative(sa0prime.compareTo(sa0));
    }

    public void testNaturalOrderDiscriminatesBetweenTwoIdenticalActivitiesFromTheSameAssignment() throws Exception {
        PlannedActivity pa = plannedActivityFromStudy("X");
        sa0.setPlannedActivity(pa);

        ScheduledActivity sa0prime = new ScheduledActivity();
        sa0prime.setActivity(sa0.getActivity());
        sa0prime.setPlannedActivity(pa);

        setId(7, putActivityInAssignment(sa0));
        setId(1, sa0prime);
        sa0prime.setScheduledStudySegment(sa0.getScheduledStudySegment());

        assertNegative(sa0.compareTo(sa0prime));
        assertPositive(sa0prime.compareTo(sa0));
    }

    public void testGetAssignmentReturnsAssignmentWhenAttached() throws Exception {
        StudySubjectAssignment expected = putActivityInAssignment(sa0);
        assertSame(expected, sa0.getStudySubjectAssignment());
    }

    public void testGetAssignmentWithoutSegmentReturnsNull() throws Exception {
        assertNull(sa0.getStudySubjectAssignment());
    }

    public void testGetAssignmentWithoutCalendarReturnsNull() throws Exception {
        putActivityInAssignment(sa0);
        sa0.getScheduledStudySegment().setScheduledCalendar(null);
        assertNull(sa0.getStudySubjectAssignment());
    }

    public void testGetAssignmentWithoutAssignmentReturnsNull() throws Exception {
        putActivityInAssignment(sa0);
        sa0.getScheduledStudySegment().getScheduledCalendar().setAssignment(null);
        assertNull(sa0.getStudySubjectAssignment());
    }

    private StudySubjectAssignment putActivityInAssignment(ScheduledActivity sa) {
        StudySubjectAssignment ssa0 = new StudySubjectAssignment();
        ssa0.setScheduledCalendar(new ScheduledCalendar());
        ssa0.getScheduledCalendar().addStudySegment(new ScheduledStudySegment());
        ssa0.getScheduledCalendar().getScheduledStudySegments().get(0).addEvent(sa);
        return ssa0;
    }

    private PlannedActivity plannedActivityFromStudy(String ident) {
        PlannedActivity pa = createPlannedActivity(activityA, 2);
        Period p = createPeriod(1, 7, 1); p.addPlannedActivity(pa);
        Study s = createSingleEpochStudy(ident, "E");
        s.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0).addPeriod(p);
        return pa;
    }
}
