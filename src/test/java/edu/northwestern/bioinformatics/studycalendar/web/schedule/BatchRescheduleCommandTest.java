package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.*;
import static edu.northwestern.bioinformatics.studycalendar.test.Fixtures.*;

import java.util.*;

/**
 * @author Rhett Sutphin
 */
public class BatchRescheduleCommandTest extends StudyCalendarTestCase {
    private BatchRescheduleCommand command;
    private ScheduledCalendarDao scheduledCalendarDao;
    private ScheduledCalendar calendar;
    private ScheduledActivity e1, e2, e3, e4, e5, e6, e7, e8;

    protected void setUp() throws Exception {
        super.setUp();
        scheduledCalendarDao = registerDaoMockFor(ScheduledCalendarDao.class);
        command = new BatchRescheduleCommand(scheduledCalendarDao);
        calendar = setId(6, new ScheduledCalendar());
        calendar.addStudySegment(new ScheduledStudySegment());
        calendar.addStudySegment(new ScheduledStudySegment());

        e1 =  createScheduledActivity("C", 2005, Calendar.APRIL, 1, new Canceled());
        e2 =  createScheduledActivity("O", 2005, Calendar.APRIL, 3, new Occurred());
        e3 =  createScheduledActivity("S", 2005, Calendar.APRIL, 4);
        e4 =  createConditionalEvent("X", 2005, Calendar.APRIL, 5);

        addEvents(
            calendar.getScheduledStudySegments().get(0),
            e1 ,
            e2,
            e3,
            e4
        );


        e5 = createScheduledActivity("C", 2005, Calendar.APRIL, 10, new Canceled());
        e6 = createScheduledActivity("O", 2005, Calendar.APRIL, 13, new Occurred());
        e7 = createScheduledActivity("S", 2005, Calendar.APRIL, 14);
        e8 = createConditionalEvent("X", 2005, Calendar.APRIL, 15);

        addEvents(
            calendar.getScheduledStudySegments().get(1),
                e5,
                e6,
                e7,
                e8
        );

        command.setScheduledCalendar(calendar);
    }

    public void testApplyCancel() throws Exception {
        command.setNewMode(ScheduledActivityMode.CANCELED);
        command.setNewReason("Died");
        Set events = new HashSet();
        Collections.addAll(events, e3, e7);
        command.setEvents(events);

        doApply();

        ScheduledActivity prevSched0 = calendar.getScheduledStudySegments().get(0).getActivities().get(2);
        assertEquals("New state not added to scheduled event in studySegment 0", 2, prevSched0.getAllStates().size());
        assertEquals("Wrong new state for previously scheduled event", ScheduledActivityMode.CANCELED, prevSched0.getCurrentState().getMode());
        assertEquals("Wrong new state for previously scheduled event", "Batch change: Died", prevSched0.getCurrentState().getReason());

        ScheduledActivity prevSched1 = calendar.getScheduledStudySegments().get(1).getActivities().get(2);
        assertEquals("New state not added to scheduled event in studySegment 1", 2, prevSched1.getAllStates().size());
        assertEquals("Wrong new state for previously scheduled event", ScheduledActivityMode.CANCELED, prevSched1.getCurrentState().getMode());
        assertEquals("Wrong new state for previously scheduled event", "Batch change: Died", prevSched1.getCurrentState().getReason());
    }

    public void testApplyReschedule() throws Exception {
        command.setNewMode(ScheduledActivityMode.SCHEDULED);
        command.setNewReason("Vacation");
        command.setDateOffset(7);
        Set events = new HashSet();
        Collections.addAll(events, e3, e7);
        command.setEvents(events);

        doApply();

        ScheduledActivity prevSched0 = calendar.getScheduledStudySegments().get(0).getActivities().get(2);
        assertEquals("New state not added to scheduled event in studySegment 0", 2, prevSched0.getAllStates().size());
        assertEquals("Wrong new state for previously scheduled event", ScheduledActivityMode.SCHEDULED, prevSched0.getCurrentState().getMode());
        assertEquals("Wrong new state for previously scheduled event", "Batch change: Vacation", prevSched0.getCurrentState().getReason());
        assertDayOfDate("Wrong new date for previously scheduled event", 2005, Calendar.APRIL, 11,
            ( prevSched0.getCurrentState()).getDate());

        ScheduledActivity prevSched1 = calendar.getScheduledStudySegments().get(1).getActivities().get(2);
        assertEquals("New state not added to scheduled event in studySegment 1", 2, prevSched1.getAllStates().size());
        assertEquals("Wrong new state for previously scheduled event", ScheduledActivityMode.SCHEDULED, prevSched1.getCurrentState().getMode());
        assertEquals("Wrong new state for previously scheduled event", "Batch change: Vacation", prevSched1.getCurrentState().getReason());
        assertDayOfDate("Wrong new date for previously scheduled event", 2005, Calendar.APRIL, 21,
            ( prevSched1.getCurrentState()).getDate());
    }

    public void testApplyOccured() throws Exception {
        command.setNewMode(ScheduledActivityMode.OCCURRED);
        command.setDateOffset(7);
        command.setNewReason("Vacation");
        Set events = new HashSet();
        Collections.addAll(events, e3, e5);
        command.setEvents(events);

        doApply();

        ScheduledActivity prevSched0 = calendar.getScheduledStudySegments().get(0).getActivities().get(2);
        assertEquals("New state not added to scheduled event in studySegment 0", 2, prevSched0.getAllStates().size());
        assertEquals("Wrong new state for previously scheduled event", ScheduledActivityMode.OCCURRED, prevSched0.getCurrentState().getMode());
        assertDayOfDate("Wrong new date for previously scheduled event", 2005, Calendar.APRIL, 4,
            (prevSched0.getCurrentState()).getDate());
        ScheduledActivity prevSched1 = calendar.getScheduledStudySegments().get(1).getActivities().get(0);
        assertEquals("New state added to scheduled event in studySegment 1", 2, prevSched1.getAllStates().size());
        assertEquals("Wrong new state for previously scheduled event", ScheduledActivityMode.CANCELED, prevSched1.getCurrentState().getMode());
    }

    public void testReasonWhenNewReasonBlank() throws Exception {
        command.setNewMode(ScheduledActivityMode.CANCELED);
        command.setNewReason(null);
        Set events = new HashSet();
        Collections.addAll(events, e3, e7);
        command.setEvents(events);

        doApply();

        ScheduledActivity prevSched0 = calendar.getScheduledStudySegments().get(0).getActivities().get(2);
        assertEquals("New state not added to scheduled event in studySegment 0", 2, prevSched0.getAllStates().size());
        assertEquals("Wrong new state for previously scheduled event", "Batch change", prevSched0.getCurrentState().getReason());
    }
    
    public void testDoesNothingWithNoMode() throws Exception {
        command.setNewMode(null);
        Set events = new HashSet();
        Collections.addAll(events, e4, e8);
        command.setEvents(events);
        command.setDateOffset(0);
        doApply(true);
        ScheduledActivity prevSched0 = calendar.getScheduledStudySegments().get(0).getActivities().get(2);
        assertEquals("New state added to scheduled event in studySegment 0", 1, prevSched0.getAllStates().size());
    }

    public void testScheduleFromConditional() throws Exception {
        command.setNewMode(ScheduledActivityMode.SCHEDULED);
        command.setDateOffset(0);
        command.setNewReason("Condition Applies");
        Set events = new HashSet();
        Collections.addAll(events, e4, e8);
        command.setEvents(events);

        doApply();

        ScheduledActivity prevCond0 = calendar.getScheduledStudySegments().get(0).getActivities().get(3);
        assertEquals("New state not added to conditional event in studySegment 0", 3, prevCond0.getAllStates().size());
        assertEquals("Wrong new state for previously conditional event", ScheduledActivityMode.SCHEDULED, prevCond0.getCurrentState().getMode());

        ScheduledActivity prevCond1 = calendar.getScheduledStudySegments().get(1).getActivities().get(3);
        assertEquals("New state not added to conditional event in studySegment 1", 3, prevCond1.getAllStates().size());
        assertEquals("Wrong new state for previously conditional event", ScheduledActivityMode.SCHEDULED, prevCond1.getCurrentState().getMode());

    }

    private void assertUnbatchableEventsNotChanged() {
        List<ScheduledActivity> studySegment0Events = calendar.getScheduledStudySegments().get(0).getActivities();
        assertEquals("New state added to canceled event in studySegment 0", 2, studySegment0Events.get(0).getAllStates().size());
        assertEquals("New state added to occurred event in studySegment 0", 2, studySegment0Events.get(1).getAllStates().size());
        List<ScheduledActivity> studySegment1Events = calendar.getScheduledStudySegments().get(0).getActivities();
        assertEquals("New state added to canceled event in studySegment 1", 2, studySegment1Events.get(0).getAllStates().size());
        assertEquals("New state added to occurred event in studySegment 1", 2, studySegment1Events.get(1).getAllStates().size());
    }

    private void doApply() {
        doApply(true);
    }

    private void doApply(boolean expectSave) {
        if (expectSave) scheduledCalendarDao.save(calendar);
        replayMocks();

        command.apply();
        verifyMocks();
        
        assertUnbatchableEventsNotChanged();
    }
}
