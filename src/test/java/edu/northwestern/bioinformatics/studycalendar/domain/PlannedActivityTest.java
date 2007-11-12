package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * Created by IntelliJ IDEA.
 * User: nshurupova
 * Date: Nov 9, 2007
 * Time: 1:44:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlannedActivityTest extends StudyCalendarTestCase {
    private PlannedActivity e0, e1;
    private Activity activity0, activity1;

    protected void setUp() throws Exception {
        super.setUp();
        activity0 = new Activity();
        activity0.setType(ActivityType.PROCEDURE);
        activity1 = new Activity();
        activity1.setType(ActivityType.INTERVENTION);

        e0 = new PlannedActivity();
        e0.setDay(1);
        e0.setActivity(activity0);
        e1 = new PlannedActivity();
        e1.setDay(2);
        e1.setActivity(activity1);
    }

    public void testNaturalOrderByDayFirst() throws Exception {
        assertNegative(e0.compareTo(e1));
        assertPositive(e1.compareTo(e0));
    }

    public void testNaturalOrderConsidersActivity() throws Exception {
        e0.setDay(e1.getDay());
        assertPositive(e0.compareTo(e1));
        assertNegative(e1.compareTo(e0));
    }

    public void testDaysInArmSimple() throws Exception {
        changePeriod(1, 7, 1);
        assertDaysInArm(e0, 1);
        assertDaysInArm(e1, 2);
    }

    private void changePeriod(int startDay, int dayCount, int repetitions) {
        Period p0 = Fixtures.createPeriod("P0", startDay, dayCount, repetitions);
        p0.addPlannedActivity(e0);
        p0.addPlannedActivity(e1);
    }

    public void testDaysInArmOffset() throws Exception {
        changePeriod(17, 7, 1);
        assertDaysInArm(e0, 17);
        assertDaysInArm(e1, 18);
    }

    public void testDaysInArmWithRepetitions() throws Exception {
        changePeriod(8, 4, 3);
        assertDaysInArm(e0, 8, 12, 16);
        assertDaysInArm(e1, 9, 13, 17);
    }

    public void testDayInArmNegative() throws Exception {
        changePeriod(-21, 7, 2);
        assertDaysInArm(e0, -21, -14);
        assertDaysInArm(e1, -20, -13);
    }

    private void assertDaysInArm(PlannedActivity e, int... expectedDays) {
        assertEquals("Wrong number of days in arm", expectedDays.length, e.getDaysInArm().size());
        for (int i = 0; i < expectedDays.length; i++) {
            int expectedDay = expectedDays[i];
            int actualDay = e.getDaysInArm().get(i);
            assertEquals("Days mismatched at index " + i, expectedDay, actualDay);
        }
    }

    public void testCloneDoesNotDeepCloneActivity() throws Exception {
        PlannedActivity clone = (PlannedActivity) e0.clone();
        assertSame("Activity is not same object", e0.getActivity(), clone.getActivity());
    }

    public void testScheduledModeWhenConditional() throws Exception {
        e0.setCondition("Only if you roll 2, 4, or 5");
        assertEquals(ScheduledActivityMode.CONDITIONAL, e0.getInitialScheduledMode());
    }

    public void testScheduledModeWhenNotConditional() throws Exception {
        e0.setCondition(" ");
        e1.setCondition(null);
        assertEquals(ScheduledActivityMode.SCHEDULED, e0.getInitialScheduledMode());
        assertEquals(ScheduledActivityMode.SCHEDULED, e1.getInitialScheduledMode());
    }
}
