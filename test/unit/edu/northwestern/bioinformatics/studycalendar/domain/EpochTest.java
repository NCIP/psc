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
        assertFalse(Epoch.create("Holocene").isMultipleArms());
        assertTrue(Epoch.create("Holocene", "A", "B").isMultipleArms());
    }

    public void testCreateNoArms() throws Exception {
        Epoch noArms = Epoch.create("Holocene");
        assertEquals("Holocene", noArms.getName());
        assertEquals("Wrong number of arms", 1, noArms.getArms().size());
        assertEquals("Wrong name for sole arm", "Holocene", noArms.getArms().get(0).getName());
    }
    
    public void testCreateMultipleArms() throws Exception {
        Epoch armed = Epoch.create("Holocene", "H", "I", "J");
        assertEquals("Holocene", armed.getName());
        assertEquals("Wrong number of arms", 3, armed.getArms().size());
        assertEquals("Wrong name for arm 0", "H", armed.getArms().get(0).getName());
        assertEquals("Wrong name for arm 1", "I", armed.getArms().get(1).getName());
        assertEquals("Wrong name for arm 2", "J", armed.getArms().get(2).getName());
    }
}
