/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createPeriod;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class RemoveMutatorTest extends StudyCalendarTestCase {
    private static final int PERIOD_ID = 7;

    private PeriodDao periodDao;
    private StudySegment studySegment;
    private Period period;
    private Remove remove;
    private RemoveMutator remover;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        periodDao = registerMockFor(PeriodDao.class);

        studySegment = setId(1, createNamedInstance("A1", StudySegment.class));
        period = setId(PERIOD_ID, createPeriod("P1", 3, PERIOD_ID, 1));
        studySegment.addPeriod(period);

        remove = new Remove();
        remove.setChildId(period.getId());

        remover = new RemoveMutator(remove, periodDao);

        expect(periodDao.getById(PERIOD_ID)).andReturn(period).anyTimes();
    }

    public void testRevertFromId() throws Exception {
        studySegment.getPeriods().remove(period);
        assertEquals("Test setup failure", 0, studySegment.getPeriods().size());
        replayMocks();
        remover.revert(studySegment);
        verifyMocks();
        assertEquals("child not added back", 1, studySegment.getPeriods().size());
        assertSame("Wrong child added back", period, studySegment.getPeriods().iterator().next());
    }

    public void testRevertFromEmbeddedNewChild() throws Exception {
        studySegment.getPeriods().remove(period);
        assertEquals("Test setup failure", 0, studySegment.getPeriods().size());
        remove.setChild(setId(null, period));

        resetMocks(); // no DAO call expected
        replayMocks();
        remover.revert(studySegment);
        verifyMocks();

        assertEquals("child not added", 1, studySegment.getPeriods().size());
        assertSame("Wrong child added", period, studySegment.getPeriods().iterator().next());
    }

    public void testApply() throws Exception {
        assertNotNull("Test setup failure; period has no initial parent", period.getParent());
        replayMocks();
        remover.apply(studySegment);
        verifyMocks();
        assertEquals("child not removed", 0, studySegment.getPeriods().size());
        assertNull("child's parent ref not cleared", period.getParent());
    }

    public void testApplyBeforeSaved() throws Exception {
        period.setId(null);
        studySegment.getPeriods().add(period);
        remove.setChild(period);

        replayMocks();
        remover.apply(studySegment);
        verifyMocks();
        assertEquals("child not removed", 0, studySegment.getPeriods().size());
    }
}
