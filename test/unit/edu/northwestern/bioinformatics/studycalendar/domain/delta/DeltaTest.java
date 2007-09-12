package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;

/**
 * @author Rhett Sutphin
 */
public class DeltaTest extends StudyCalendarTestCase {
    public void testDeltaForPlannedCalendar() throws Exception {
        assertDeltaFor(new PlannedCalendar(), PlannedCalendarDelta.class);
    }

    public void testDeltaForEpoch() throws Exception {
        assertDeltaFor(new Epoch(), EpochDelta.class);
    }

    public void testDeltaForArm() throws Exception {
        assertDeltaFor(new Arm(), ArmDelta.class);
    }

    public void testDeltaForPeriod() throws Exception {
        assertDeltaFor(new Period(), PeriodDelta.class);
    }

    public void testDeltaForPlannedEvent() throws Exception {
        assertDeltaFor(new PlannedEvent(), PlannedEventDelta.class);
    }

    private static void assertDeltaFor(PlanTreeNode<?> node, Class<?> expectedClass) {
        Delta<?> actual = Delta.createDeltaFor(node);
        assertNotNull(actual);
        assertEquals("Wrong class", expectedClass, actual.getClass());
    }
}
