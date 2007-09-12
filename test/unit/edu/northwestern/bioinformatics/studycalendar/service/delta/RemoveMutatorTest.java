package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createPeriod;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class RemoveMutatorTest extends StudyCalendarTestCase {
    private static final int PERIOD_ID = 7;

    private PeriodDao periodDao;
    private Arm arm;
    private Period period;
    private Remove remove;
    private RemoveMutator remover;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        periodDao = registerMockFor(PeriodDao.class);

        arm = setId(1, createNamedInstance("A1", Arm.class));
        period = setId(PERIOD_ID, createPeriod("P1", 3, PERIOD_ID, 1));
        arm.addPeriod(period);

        remove = new Remove();
        remove.setChildId(period.getId());

        remover = new RemoveMutator(remove, periodDao);

        expect(periodDao.getById(PERIOD_ID)).andReturn(period).anyTimes();
    }

    public void testRevertFromId() throws Exception {
        arm.getPeriods().remove(period);
        assertEquals("Test setup failure", 0, arm.getPeriods().size());
        replayMocks();
        remover.revert(arm);
        verifyMocks();
        assertEquals("child not added back", 1, arm.getPeriods().size());
        assertSame("Wrong child added back", period, arm.getPeriods().iterator().next());
    }

    public void testRevertFromEmbeddedNewChild() throws Exception {
        arm.getPeriods().remove(period);
        assertEquals("Test setup failure", 0, arm.getPeriods().size());
        remove.setChild(setId(null, period));

        resetMocks(); // no DAO call expected
        replayMocks();
        remover.revert(arm);
        verifyMocks();

        assertEquals("child not added", 1, arm.getPeriods().size());
        assertSame("Wrong child added", period, arm.getPeriods().iterator().next());
    }

    public void testApply() throws Exception {
        replayMocks();
        remover.apply(arm);
        verifyMocks();
        assertEquals("child not removed", 0, arm.getPeriods().size());
    }

    public void testApplyBeforeSaved() throws Exception {
        period.setId(null);
        arm.getPeriods().add(period);
        remove.setChild(period);

        replayMocks();
        remover.apply(arm);
        verifyMocks();
        assertEquals("child not removed", 0, arm.getPeriods().size());
    }
}
