/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class CollectionAddMutatorTest extends StudyCalendarTestCase {
    private static final int PERIOD_ID = 7;

    private PeriodDao periodDao;
    private StudySegment studySegment;
    private Period period;
    private Add add;
    private CollectionAddMutator adder;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studySegment = setId(1, createNamedInstance("A1", StudySegment.class));
        period = setId(PERIOD_ID, createPeriod("P1", 3, PERIOD_ID, 1));
        periodDao = registerMockFor(PeriodDao.class);

        add = new Add();
        add.setChildId(period.getId());

        adder = new CollectionAddMutator(add, periodDao);

        expect(periodDao.getById(PERIOD_ID)).andReturn(period).anyTimes();
    }

    public void testApplyFromDao() throws Exception {
        assertEquals("Test setup failure", 0, studySegment.getPeriods().size());
        replayMocks();
        adder.apply(studySegment);
        verifyMocks();
        assertEquals("child not added", 1, studySegment.getPeriods().size());
        assertSame("Wrong child added", period, studySegment.getPeriods().iterator().next());
    }
    
    public void testApplyFromEmbeddedNewChild() throws Exception {
        assertEquals("Test setup failure", 0, studySegment.getPeriods().size());
        add.setChild(setId(null, period));

        resetMocks(); // no DAO call expected
        replayMocks();
        adder.apply(studySegment);
        verifyMocks();

        assertEquals("child not added", 1, studySegment.getPeriods().size());
        assertSame("Wrong child added", period, studySegment.getPeriods().iterator().next());
    }

    public void testRevert() throws Exception {
        studySegment.getPeriods().add(period);

        replayMocks();
        adder.revert(studySegment);
        verifyMocks();
        assertEquals("child not removed", 0, studySegment.getPeriods().size());
    }
    
    public void testRevertBeforeSaved() throws Exception {
        period.setId(null);
        studySegment.getPeriods().add(period);
        add.setChild(period);

        replayMocks();
        adder.revert(studySegment);
        verifyMocks();
        assertEquals("child not removed", 0, studySegment.getPeriods().size());
    }

    public void testAddToTransientParentAddsTransientCopy() throws Exception {
        assertEquals("Test setup failure", 0, studySegment.getPeriods().size());
        studySegment.setMemoryOnly(true);

        replayMocks();
        adder.apply(studySegment);
        verifyMocks();
        assertEquals("child not added", 1, studySegment.getPeriods().size());
        Period actualAdded = studySegment.getPeriods().iterator().next();
        assertNotSame("Child added directly from DAO", period, actualAdded);
        assertEquals("Wrong child added", PERIOD_ID, (int) actualAdded.getId());
        assertTrue("New child is not marked transient", actualAdded.isMemoryOnly());
    }
}
