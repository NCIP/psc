package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import org.easymock.EasyMock;

/**
 * @author Rhett Sutphin
 */
public class EpochTest extends StudyCalendarTestCase {
    private Epoch epoch;

    protected void setUp() throws Exception {
        super.setUp();
        epoch = new Epoch();
    }

    public void testAddArm() throws Exception {
        Arm arm = new Arm();
        epoch.addArm(arm);
        assertEquals("Wrong number of arms", 1, epoch.getArms().size());
        assertSame("Wrong arm present", arm, epoch.getArms().get(0));
        assertSame("Bidirectional relationship not maintained", epoch, arm.getEpoch());
    }

    public void testLength() throws Exception {
        Arm a1 = registerMockFor(Arm.class);
        EasyMock.expect(a1.getLengthInDays()).andReturn(45);
        a1.setEpoch(epoch);

        Arm a2 = registerMockFor(Arm.class);
        EasyMock.expect(a2.getLengthInDays()).andReturn(13);
        a2.setEpoch(epoch);

        replayMocks();

        epoch.addArm(a1);
        epoch.addArm(a2);
        assertEquals(45, epoch.getLengthInDays());
        verifyMocks();
    }
    
    public void testMultipleArms() throws Exception {
        assertFalse(new Epoch().isMultipleArms());
        assertFalse(Fixtures.createEpoch("Holocene").isMultipleArms());
        assertTrue(Fixtures.createEpoch("Holocene", "A", "B").isMultipleArms());
    }
}
