package edu.northwestern.bioinformatics.studycalendar.domain;

import junit.framework.TestCase;

/**
 * @author Moses Hohman
 */
public class ArmTest extends TestCase {
    private Arm arm = new Arm();

    public void testAddEpoch() {
        Period period = new Period();
        arm.addPeriod(period);
        assertEquals("wrong number of epochs", 1, arm.getPeriods().size());
        assertSame("wrong epoch present", period, arm.getPeriods().iterator().next());
        assertEquals("bidirectional relationship not maintained", arm, period.getArm());
    }
}
