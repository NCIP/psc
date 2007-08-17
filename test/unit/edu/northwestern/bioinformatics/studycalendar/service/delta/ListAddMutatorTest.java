package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.ArmDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import org.easymock.EasyMock;

/**
 * @author Rhett Sutphin
 */
public class ListAddMutatorTest extends StudyCalendarTestCase {
    private static final int ARM_ID = 17;

    private ArmDao armDao;
    private Epoch epoch;
    private Arm arm;
    private Add add;
    private ListAddMutator adder;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        epoch = setId(1, createNamedInstance("E1", Epoch.class));
        epoch.addArm(setId(1, createNamedInstance("A1", Arm.class)));
        epoch.addArm(setId(2, createNamedInstance("A2", Arm.class)));
        arm = setId(ARM_ID, createNamedInstance("A1.5", Arm.class));
        armDao = registerMockFor(ArmDao.class);

        add = new Add();
        add.setNewChildId(arm.getId());
        add.setIndex(1);

        EasyMock.expect(armDao.getById(ARM_ID)).andReturn(arm).anyTimes();
        adder = new ListAddMutator(add, armDao);
    }

    public void testApply() throws Exception {
        assertEquals("Test setup failure", 2, epoch.getArms().size());
        replayMocks();
        adder.apply(epoch);
        verifyMocks();
        assertEquals("child not added", 3, epoch.getArms().size());
        assertSame("Wrong child added (or in wrong position): " + epoch.getArms(), arm, epoch.getArms().get(1));
    }

    public void testRevert() throws Exception {
        epoch.addChild(arm, 1);

        replayMocks();
        adder.revert(epoch);
        verifyMocks();
        assertEquals("child not removed", 2, epoch.getArms().size());
        assertEquals("Wrong child removed", "A1", epoch.getArms().get(0).getName());
        assertEquals("Wrong child removed", "A2", epoch.getArms().get(1).getName());
    }
}
