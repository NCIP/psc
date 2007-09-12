package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class CollectionAddMutatorTest extends StudyCalendarTestCase {
    private static final int PERIOD_ID = 7;

    private PeriodDao periodDao;
    private Arm arm;
    private Period period;
    private Add add;
    private CollectionAddMutator adder;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        arm = setId(1, createNamedInstance("A1", Arm.class));
        period = setId(PERIOD_ID, createPeriod("P1", 3, PERIOD_ID, 1));
        periodDao = registerMockFor(PeriodDao.class);

        add = new Add();
        add.setChildId(period.getId());

        adder = new CollectionAddMutator(add, periodDao);

        expect(periodDao.getById(PERIOD_ID)).andReturn(period).anyTimes();
    }

    public void testApplyFromDao() throws Exception {
        assertEquals("Test setup failure", 0, arm.getPeriods().size());
        replayMocks();
        adder.apply(arm);
        verifyMocks();
        assertEquals("child not added", 1, arm.getPeriods().size());
        assertSame("Wrong child added", period, arm.getPeriods().iterator().next());
    }
    
    public void testApplyFromEmbeddedNewChild() throws Exception {
        assertEquals("Test setup failure", 0, arm.getPeriods().size());
        add.setChild(setId(null, period));

        resetMocks(); // no DAO call expected
        replayMocks();
        adder.apply(arm);
        verifyMocks();

        assertEquals("child not added", 1, arm.getPeriods().size());
        assertSame("Wrong child added", period, arm.getPeriods().iterator().next());
    }

    public void testRevert() throws Exception {
        arm.getPeriods().add(period);

        replayMocks();
        adder.revert(arm);
        verifyMocks();
        assertEquals("child not removed", 0, arm.getPeriods().size());
    }
    
    public void testRevertBeforeSaved() throws Exception {
        period.setId(null);
        arm.getPeriods().add(period);
        add.setChild(period);

        replayMocks();
        adder.revert(arm);
        verifyMocks();
        assertEquals("child not removed", 0, arm.getPeriods().size());
    }

    public void testAddToTransientParentAddsTransientCopy() throws Exception {
        assertEquals("Test setup failure", 0, arm.getPeriods().size());
        arm.setMemoryOnly(true);

        replayMocks();
        adder.apply(arm);
        verifyMocks();
        assertEquals("child not added", 1, arm.getPeriods().size());
        Period actualAdded = arm.getPeriods().iterator().next();
        assertNotSame("Child added directly from DAO", period, actualAdded);
        assertEquals("Wrong child added", PERIOD_ID, (int) actualAdded.getId());
        assertTrue("New child is not marked transient", actualAdded.isMemoryOnly());
    }
}
