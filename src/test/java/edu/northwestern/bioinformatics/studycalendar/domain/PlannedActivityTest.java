package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;

import java.util.List;

public class PlannedActivityTest extends StudyCalendarTestCase {
    private PlannedActivity pa0, pa1;

    protected void setUp() throws Exception {
        super.setUp();
        Activity activity0 = createActivity("0", ActivityType.PROCEDURE);
        Activity activity1 = createActivity("1", ActivityType.INTERVENTION);

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

    private void changePeriod(int startDay, int dayCount, int repetitions) {
        Period p0 = createPeriod("P0", startDay, dayCount, repetitions);
        p0.addPlannedActivity(pa0);
        p0.addPlannedActivity(pa1);
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
        assertSame("Wrong label added", expected, pa0.getPlannedActivityLabels().get(0));
        assertSame("Reverse relationship not preserved", pa0, expected.getPlannedActivity());
    }

    public void testGetLabelsForNoPlannedActivityLabels() throws Exception {
        assertEquals(0, pa0.getLabels().size());
    }

    public void testGetLabels() throws Exception {
        labelPlannedActivity(pa0, "foo");
        labelPlannedActivity(pa0, "bar");
        List<Label> actual = pa0.getLabels();
        assertEquals("Wrong number of labels returned", 2, actual.size());
        assertEquals("Wrong first label", "foo", actual.get(0).getName());
        assertEquals("Wrong second label", "bar", actual.get(1).getName());
    }

    public void testGetLabelsText() throws Exception {
        labelPlannedActivity(pa0, "foo");
        labelPlannedActivity(pa0, "bar");
        List<String> actual = pa0.getLabelNames();
        assertEquals("Wrong number of labels returned", 2, actual.size());
        assertEquals("Wrong first label", "foo", actual.get(0));
        assertEquals("Wrong second label", "bar", actual.get(1));
    }
}
