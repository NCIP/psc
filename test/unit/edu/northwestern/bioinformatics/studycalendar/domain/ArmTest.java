package edu.northwestern.bioinformatics.studycalendar.domain;

import junit.framework.TestCase;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;

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
        Period single = createPeriod("", 3, Duration.Unit.day, 15, 3);
        arm.addPeriod(single);

        assertEquals(47, arm.getLengthInDays());
    }
    
    public void testLengthWhenOverlapping() throws Exception {
        Period zero = createPeriod("", 1, Duration.Unit.day, 30, 1);
        Period one = createPeriod("", 17, Duration.Unit.day, 15, 1);
        arm.addPeriod(zero);
        arm.addPeriod(one);

        assertEquals(31, arm.getLengthInDays());
    }

    public void testQualifiedNameZeroArmEpoch() throws Exception {
        assertEquals("Epoch", createEpoch("Epoch").getArms().get(0).getQualifiedName());
    }
    
    public void testQualifiedName() throws Exception {
        Epoch epoch = createEpoch("Epoch", "A", "B");
        assertEquals("Epoch: A", epoch.getArms().get(0).getQualifiedName());
        assertEquals("Epoch: B", epoch.getArms().get(1).getQualifiedName());
    }
}
