package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import static edu.northwestern.bioinformatics.studycalendar.test.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.test.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

public class PlannedActivityTest extends StudyCalendarTestCase {
    private PlannedActivity pa0, pa1;

    protected void setUp() throws Exception {
        super.setUp();
        Activity activity0 = createActivity("0", Fixtures.createActivityType("PROCEDURE"));
        Activity activity1 = createActivity("1", Fixtures.createActivityType("INTERVENTION"));

        pa0 = createPlannedActivity(activity0, 1);
        pa1 = createPlannedActivity(activity1, 2);
    }

    public void testNaturalOrderByDayFirst() throws Exception {
        assertNegative(pa0.compareTo(pa1));
        assertPositive(pa1.compareTo(pa0));
    }

    public void testNaturalOrderConsidersActivity() throws Exception {
        pa0.setDay(pa1.getDay());
        assertPositive(pa0.compareTo(pa1));
        assertNegative(pa1.compareTo(pa0));
    }

    public void testDaysInStudySegmentSimple() throws Exception {
        changePeriod(1, 7, 1);
        assertDaysInStudySegment(pa0, 1);
        assertDaysInStudySegment(pa1, 2);
    }

    public void testDaysInStudySegmentOffset() throws Exception {
        changePeriod(17, 7, 1);
        assertDaysInStudySegment(pa0, 17);
        assertDaysInStudySegment(pa1, 18);
    }

    public void testDaysInStudySegmentWithRepetitions() throws Exception {
        changePeriod(8, 4, 3);
        assertDaysInStudySegment(pa0, 8, 12, 16);
        assertDaysInStudySegment(pa1, 9, 13, 17);
    }

    public void testDayInStudySegmentNegative() throws Exception {
        changePeriod(-21, 7, 2);
        assertDaysInStudySegment(pa0, -21, -14);
        assertDaysInStudySegment(pa1, -20, -13);
    }

    public void testDaysInStudySegmentWithUnit() throws Exception {
        changePeriod(4, Duration.Unit.month, 2, 4);
        assertDaysInStudySegment(pa0, 4, 60, 116, 172);
        assertDaysInStudySegment(pa1, 5, 61, 117, 173);
    }

    private void changePeriod(int startDay, int dayCount, int repetitions) {
        changePeriod(startDay, Duration.Unit.day, dayCount, repetitions);
    }

    private void changePeriod(int startDay, Duration.Unit unit, int quantity, int repetitions) {
        Period p0 = createPeriod("P0", startDay, unit, quantity, repetitions);
        p0.addPlannedActivity(pa0);
        p0.addPlannedActivity(pa1);
    }

    private void assertDaysInStudySegment(PlannedActivity e, int... expectedDays) {
        assertEquals("Wrong number of days in study segment", expectedDays.length, e.getDaysInStudySegment().size());
        for (int i = 0; i < expectedDays.length; i++) {
            int expectedDay = expectedDays[i];
            int actualDay = e.getDaysInStudySegment().get(i);
            assertEquals("Days mismatched at index " + i, expectedDay, actualDay);
        }
    }

    public void testCloneDoesNotDeepCloneActivity() throws Exception {
        PlannedActivity clone = pa0.clone();
        assertSame("Activity is not same object", pa0.getActivity(), clone.getActivity());
    }

    public void testCloneDeepClonesLabels() throws Exception {
        labelPlannedActivity(pa0, "foo", "boom");
        PlannedActivity clone = pa0.clone();
        assertEquals("clone has different number of labels",
            pa0.getPlannedActivityLabels().size(), clone.getPlannedActivityLabels().size());
        assertNotSame("clone has same labels collection",
            pa0.getPlannedActivityLabels(), clone.getPlannedActivityLabels());
        assertNotSame("first label not cloned",
            pa0.getPlannedActivityLabels().first(), clone.getPlannedActivityLabels().first());
        assertEquals("first label not cloned",
            pa0.getPlannedActivityLabels().first().getLabel(), clone.getPlannedActivityLabels().first().getLabel());
        assertNotSame("last label not cloned",
            pa0.getPlannedActivityLabels().last(), clone.getPlannedActivityLabels().last());
        assertEquals("last label not cloned",
            pa0.getPlannedActivityLabels().last().getLabel(), clone.getPlannedActivityLabels().last().getLabel());
    }
    
    public void testClonedLabelParentIsClone() throws Exception {
        labelPlannedActivity(pa0, "foo", "boom");
        PlannedActivity clone = pa0.clone();
        assertSame(clone, clone.getPlannedActivityLabels().first().getParent());
    }

    public void testScheduledModeWhenConditional() throws Exception {
        pa0.setCondition("Only if you roll 2, 4, or 5");
        assertEquals(ScheduledActivityMode.CONDITIONAL, pa0.getInitialScheduledMode());
    }

    public void testScheduledModeWhenNotConditional() throws Exception {
        pa0.setCondition(" ");
        pa1.setCondition(null);
        assertEquals(ScheduledActivityMode.SCHEDULED, pa0.getInitialScheduledMode());
        assertEquals(ScheduledActivityMode.SCHEDULED, pa1.getInitialScheduledMode());
    }

    public void testAddFirstPlannedActivityLabel() throws Exception {
        PlannedActivityLabel expected = new PlannedActivityLabel();
        pa0.addPlannedActivityLabel(expected);
        assertEquals("Wrong number of labels", 1, pa0.getPlannedActivityLabels().size());
        assertSame("Wrong label added", expected, pa0.getPlannedActivityLabels().iterator().next());
        assertSame("Reverse relationship not preserved", pa0, expected.getPlannedActivity());
    }

    public void testGetLabelsForNoPlannedActivityLabels() throws Exception {
        assertEquals(0, pa0.getLabels().size());
    }

    public void testGetLabels() throws Exception {
        labelPlannedActivity(pa0, "foo");
        labelPlannedActivity(pa0, "bar");
        Set<String> actual = pa0.getLabels();
        assertEquals("Wrong number of labels returned", 2, actual.size());
        Iterator<String> it = actual.iterator();
        assertEquals("Wrong first label", "bar", it.next());
        assertEquals("Wrong second label", "foo", it.next());
    }

    public void testGetLabelsForRepetition() throws Exception {
        Period p = createPeriod(5, 5, 3);
        p.addPlannedActivity(pa0);

        labelPlannedActivity(pa0, "bar");
        labelPlannedActivity(pa0, 2, "foo", "baz");
        labelPlannedActivity(pa0, 0, "quux");

        List<SortedSet<String>> actual = pa0.getLabelsByRepetition();
        assertEquals("Wrong number of labels for rep 0", 2, actual.get(0).size());
        assertTrue("Wrong labels for rep 0", actual.get(0).containsAll(Arrays.asList("bar", "quux")));
        assertEquals("Wrong number of labels for rep 1", 1, actual.get(1).size());
        assertTrue("Wrong labels for rep 1: " + actual.get(1), actual.get(1).containsAll(Arrays.asList("bar")));
        assertEquals("Wrong number of labels for rep 2", 3, actual.get(2).size());
        assertTrue("Wrong labels for rep 2: " + actual.get(2), actual.get(2).containsAll(Arrays.asList("bar", "baz", "foo")));
    }

    public void testGetLabelsByRepetitionDoesNotWorkWhenDetached() throws Exception {
        pa0.setParent(null);
        labelPlannedActivity(pa0, "zem");
        try {
            pa0.getLabelsByRepetition();
        } catch (StudyCalendarSystemException scse) {
            assertEquals("This method does not work unless the planned activity is part of a period", scse.getMessage());
        }
    }

    public void testClearIdsClearsLabelIds() throws Exception {
        labelPlannedActivity(pa0, "zem", "quux");
        assignIds(pa0, 5);

        pa0.clearIds();

        for (PlannedActivityLabel label : pa0.getPlannedActivityLabels()) {
            assertNull("Should not have an id, but does: " + label, label.getId());
            assertNull("Should not have a grid id, but does: " + label, label.getGridId());
        }
    }
}
