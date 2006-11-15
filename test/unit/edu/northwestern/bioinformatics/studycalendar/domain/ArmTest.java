package edu.northwestern.bioinformatics.studycalendar.domain;

import junit.framework.TestCase;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Moses Hohman
 */
public class ArmTest extends StudyCalendarTestCase {
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

        assertDayRange(3, 47, arm.getDayRange());
        assertEquals(45, arm.getLengthInDays());
    }
    
    public void testLengthWhenOverlapping() throws Exception {
        Period zero = createPeriod("", 1, Duration.Unit.day, 30, 1);
        Period one = createPeriod("", 17, Duration.Unit.day, 15, 1);
        arm.addPeriod(zero);
        arm.addPeriod(one);

        assertDayRange(1, 31, arm.getDayRange());
        assertEquals(31, arm.getLengthInDays());
    }

    public void testLengthNegative() throws Exception {
        Period single = createPeriod("", -28, 15, 1);
        arm.addPeriod(single);

        assertDayRange(-28, -14, arm.getDayRange());
        assertEquals(15, arm.getLengthInDays());
    }

    public void testLengthNegativeAndPositiveWithGap() throws Exception {
        arm.addPeriod(createPeriod("dc", -28, 14, 1));
        arm.addPeriod(createPeriod("dc", 10, 8, 2));

        assertDayRange(-28, 25, arm.getDayRange());
        assertEquals(54, arm.getLengthInDays());
    }

    public void testQualifiedNameZeroArmEpoch() throws Exception {
        assertEquals("Epoch", Epoch.create("Epoch").getArms().get(0).getQualifiedName());
    }
    
    public void testQualifiedName() throws Exception {
        Epoch epoch = Epoch.create("Epoch", "A", "B");
        assertEquals("Epoch: A", epoch.getArms().get(0).getQualifiedName());
        assertEquals("Epoch: B", epoch.getArms().get(1).getQualifiedName());
    }

    public void testDayRangeWithNoPeriods() throws Exception {
        assertEquals(0, new Arm().getDayRange().getDayCount());
    }
}
