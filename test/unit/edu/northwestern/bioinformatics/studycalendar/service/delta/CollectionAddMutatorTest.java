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
        add.setNewChildId(period.getId());

        adder = new CollectionAddMutator(add, periodDao);

        expect(periodDao.getById(PERIOD_ID)).andReturn(period).anyTimes();
    }

    public void testApply() throws Exception {
        assertEquals("Test setup failure", 0, arm.getPeriods().size());
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
}
