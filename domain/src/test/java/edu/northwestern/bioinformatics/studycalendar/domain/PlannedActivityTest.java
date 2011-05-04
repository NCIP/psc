package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.*;

public class PlannedActivityTest extends DomainTestCase {
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

    public void testNaturalOrderConsidersWeight() throws Exception {
        pa0.setDay(pa1.getDay());
        pa0.setActivity(pa1.getActivity());
        pa0.setWeight(9); pa1.setWeight(4);
        assertNegative(pa0.compareTo(pa1));
        assertPositive(pa1.compareTo(pa0));
    }

    public void testNaturalOrderWorksWithOneNullWeight() throws Exception {
        pa0.setDay(pa1.getDay());
        pa0.setActivity(pa1.getActivity());
        pa0.setWeight(9); pa1.setWeight(null);
        assertNegative(pa0.compareTo(pa1));
        assertPositive(pa1.compareTo(pa0));
    }

    public void testCompareWeightToIsInverseNumericOrder() throws Exception {
        pa0.setWeight(9); pa1.setWeight(4);
        assertNegative(pa0.compareWeightTo(pa1));
        assertPositive(pa1.compareWeightTo(pa0));
    }

    public void testCompareWeightToNullSafe() throws Exception {
        pa0.setWeight(null); pa1.setWeight(4);
        assertNegative(pa1.compareWeightTo(pa0));
        assertPositive(pa0.compareWeightTo(pa1));
    }
    
    public void testCompareWeightForDefault() throws Exception {
        assertEquals(0, pa1.compareWeightTo(pa0));
        assertEquals(0, pa0.compareWeightTo(pa1));
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

    public void testCopy() throws Exception {
        pa0.setId(1);
        pa0.setGridId("grid 0");
        PlannedActivity copiedPlannedActivity = pa0.copy();

        assertNotNull(copiedPlannedActivity);

        assertNotSame("copied node and source node must be different", copiedPlannedActivity, pa0);
        assertNull(copiedPlannedActivity.getId());
        assertNull(copiedPlannedActivity.getGridId());

        assertEquals("must copy activity", copiedPlannedActivity.getActivity(), pa0.getActivity());
        assertSame("activity must be same", copiedPlannedActivity.getActivity(), pa0.getActivity());
        assertEquals("must copy condition", copiedPlannedActivity.getCondition(), pa0.getCondition());
        assertEquals("must copy day", copiedPlannedActivity.getDay(), pa0.getDay());
        assertEquals("must copy details", copiedPlannedActivity.getDetails(), pa0.getDetails());
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

    public void testPlannedActivitiesSortingByWeight() throws Exception {
        PlannedActivity pa0, pa1, pa2, pa3;
        pa0 = createPlannedActivity(createActivity("0", Fixtures.createActivityType("PROCEDURE")), 1, 5);
        pa1 = createPlannedActivity(createActivity("1", Fixtures.createActivityType("INTERVENTION")), 2, 0);
        pa2 = createPlannedActivity(createActivity("2", Fixtures.createActivityType("DESEASE")), 3, -1);
        pa3 = createPlannedActivity(createActivity("3", Fixtures.createActivityType("OTHER")), 4, 10);

        List<PlannedActivity> listOfPAs = new ArrayList<PlannedActivity>();
        listOfPAs.add(pa0);
        listOfPAs.add(pa1);
        listOfPAs.add(pa2);
        listOfPAs.add(pa3);

        assertTrue("First element has wrong weight ", listOfPAs.get(0).getWeight()==5);
        assertTrue("Second element has wrong weight ", listOfPAs.get(1).getWeight()==0);
        assertTrue("Third element has wrong weight ", listOfPAs.get(2).getWeight()==-1);
        assertTrue("Forth element has wrong weight ", listOfPAs.get(3).getWeight()==10);
        Collections.sort(listOfPAs);
        assertTrue("First element after sorting has wrong weight ", listOfPAs.get(0).getWeight()==10);
        assertTrue("Second element after sorting has wrong weight ", listOfPAs.get(1).getWeight()==5);
        assertTrue("Third element after sorting has wrong weight ", listOfPAs.get(2).getWeight()==0);
        assertTrue("Forth element after sorting has wrong weight ", listOfPAs.get(3).getWeight()==-1);
    }

    public void testPlanDayWithoutPeriod() throws Exception {
        assertNull(new PlannedActivity().getPlanDay());
    }
    
    public void testPlanDayForDayOne() throws Exception {
        assertEquals("1", createPlannedActivityInPeriod(1, 1).getPlanDay());
    }

    public void testPlanDayForDayTwoWithPeriodAtOne() throws Exception {
        assertEquals("2", createPlannedActivityInPeriod(1, 2).getPlanDay());
    }

    public void testPlanDayForDayOneWithPeriodAtTwo() throws Exception {
        assertEquals("2", createPlannedActivityInPeriod(2, 1).getPlanDay());
    }

    public void testPlanDayForDayOneWithPeriodStartingNegative() throws Exception {
        assertEquals("-9", createPlannedActivityInPeriod(-9, 1).getPlanDay());
    }
    
    public void testEffectiveWeightWithExplicitWeightIsExplicitWeight() throws Exception {
        pa0.setWeight(8);
        assertEquals(8, pa0.getEffectiveWeight());
    }

    public void testEffectiveWeightWithNoWeightIsZero() throws Exception {
        pa0.setWeight(null);
        assertEquals(0, pa0.getEffectiveWeight());
    }

    public void testDeepEqualsForDifferentActivity() throws Exception {
        PlannedActivity a = createPlannedActivity(createActivity("a1", "A"), 1);
        PlannedActivity b = createPlannedActivity(createActivity("a2", "A"), 1);

        assertChildDifferences(a.deepEquals(b), "activity", "name \"a1\" does not match \"a2\"");
    }

    public void testDeepEqualsForDifferentDay() throws Exception {
        PlannedActivity a = createPlannedActivity("A", 1);
        PlannedActivity b = createPlannedActivity("A", 2);

        assertDifferences(a.deepEquals(b), "day does not match: 1 != 2");
    }

    public void testDeepEqualsForDifferentWeight() throws Exception {
        PlannedActivity a = createPlannedActivity("A", 1, 8);
        PlannedActivity b = createPlannedActivity("A", 1, 10);

        assertDifferences(a.deepEquals(b), "weight does not match: 8 != 10");
    }

    public void testDeepEqualsForDifferentDetailsAndCondition() throws Exception {
        PlannedActivity a = createPlannedActivity("A", 1, "Every time", null);
        PlannedActivity b = createPlannedActivity("A", 1, "Sometimes", "When the coin flip is tails");

        assertDifferences(a.deepEquals(b),
            "details \"Every time\" does not match \"Sometimes\"",
            "condition <null> does not match \"When the coin flip is tails\"");
    }

    public void testDeepEqualsForDifferentLabels() throws Exception {
        PlannedActivity a = createPlannedActivity("A", 1);
        PlannedActivity b = createPlannedActivity("A", 1);
        Fixtures.addPlannedActivityLabel(a, "rb", 5);
        Fixtures.addPlannedActivityLabel(b, "bit", null);

        Differences differences = a.deepEquals(b);
        assertDifferences(differences,
            "missing label rb on 5",
            "extra label bit on all");
    }

    public void testDeepEqualsForDifferentPopulation() throws Exception {
        PlannedActivity a = createPlannedActivity("A", 1);
        PlannedActivity b = createPlannedActivity("A", 1);
        Population p1 = Fixtures.createPopulation("N1", "name");
        a.setPopulation(p1);

        assertDifferences(a.deepEquals(b), "population N1 does not match <null>");
    }

    private PlannedActivity createPlannedActivityInPeriod(int periodStartDay, int paDay) {
        Period p = createPeriod(periodStartDay, 21, 1);
        PlannedActivity pa = createPlannedActivity("A", paDay);
        p.addPlannedActivity(pa);
        return pa;
    }
}
