package edu.northwestern.bioinformatics.studycalendar.domain;

import junit.framework.TestCase;

/**
 * @author Moses Hohman
 */
public class ArmTest extends TestCase {
    private Arm arm = new Arm();

    public void testAddPeriod() {
        Period period = new Period();
        arm.addPeriod(period);
        assertEquals("wrong number of periods", 1, arm.getPeriods().size());
        assertSame("wrong period present", period, arm.getPeriods().iterator().next());
        assertEquals("bidirectional relationship not maintained", arm, period.getArm());
    }

    public void testLengthSimple() throws Exception {
        Period single = Fixtures.createPeriod("", 3, Duration.Unit.day, 15, 3);
        arm.addPeriod(single);

        assertEquals(47, arm.getLengthInDays());
    }
    
    public void testLengthWhenOverlapping() throws Exception {
        Period zero = Fixtures.createPeriod("", 1, Duration.Unit.day, 30, 1);
        Period one = Fixtures.createPeriod("", 17, Duration.Unit.day, 15, 1);
        arm.addPeriod(zero);
        arm.addPeriod(one);

        assertEquals(31, arm.getLengthInDays());
    }
}
