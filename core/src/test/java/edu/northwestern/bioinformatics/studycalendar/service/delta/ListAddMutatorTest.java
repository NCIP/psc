/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import org.easymock.EasyMock;

/**
 * @author Rhett Sutphin
 */
public class ListAddMutatorTest extends StudyCalendarTestCase {
    private static final int STUDY_SEGMENT_ID = 17;

    private StudySegmentDao studySegmentDao;
    private Epoch epoch;
    private StudySegment studySegment;
    private Add add;
    private ListAddMutator adder;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        epoch = setId(1, createNamedInstance("E1", Epoch.class));
        epoch.addStudySegment(setId(1, createNamedInstance("A1", StudySegment.class)));
        epoch.addStudySegment(setId(2, createNamedInstance("A2", StudySegment.class)));
        studySegment = setId(STUDY_SEGMENT_ID, createNamedInstance("A1.5", StudySegment.class));
        studySegmentDao = registerMockFor(StudySegmentDao.class);

        add = new Add();
        add.setChildId(studySegment.getId());
        add.setIndex(1);

        EasyMock.expect(studySegmentDao.getById(STUDY_SEGMENT_ID)).andReturn(studySegment).anyTimes();
        adder = new ListAddMutator(add, studySegmentDao);
    }

    public void testApplyFromDao() throws Exception {
        assertEquals("Test setup failure", 2, epoch.getStudySegments().size());
        replayMocks();
        adder.apply(epoch);
        verifyMocks();
        assertEquals("child not added", 3, epoch.getStudySegments().size());
        assertSame("Wrong child added (or in wrong position): " + epoch.getStudySegments(), studySegment, epoch.getStudySegments().get(1));
    }
    
    public void testApplyFromEmbeddedNewChild() throws Exception {
        assertEquals("Test setup failure", 2, epoch.getStudySegments().size());
        studySegment.setId(null);
        add.setChild(studySegment);

        resetMocks();
        replayMocks();
        adder.apply(epoch);
        verifyMocks();
        assertEquals("child not added", 3, epoch.getStudySegments().size());
        assertSame("Wrong child added (or in wrong position): " + epoch.getStudySegments(), studySegment, epoch.getStudySegments().get(1));
    }

    public void testRevert() throws Exception {
        epoch.addChild(studySegment, 1);

        replayMocks();
        adder.revert(epoch);
        verifyMocks();
        assertEquals("child not removed", 2, epoch.getStudySegments().size());
        assertEquals("Wrong child removed", "A1", epoch.getStudySegments().get(0).getName());
        assertEquals("Wrong child removed", "A2", epoch.getStudySegments().get(1).getName());
    }
    
    public void testRevertBeforeSaved() throws Exception {
        epoch.addChild(studySegment, 1);
        studySegment.setId(null);
        add.setChild(studySegment);

        replayMocks();
        adder.revert(epoch);
        verifyMocks();
        assertEquals("child not removed", 2, epoch.getStudySegments().size());
        assertEquals("Wrong child removed", "A1", epoch.getStudySegments().get(0).getName());
        assertEquals("Wrong child removed", "A2", epoch.getStudySegments().get(1).getName());
    }

    public void testApplyToTransientAddsTransientCopy() throws Exception {
        assertEquals("Test setup failure", 2, epoch.getStudySegments().size());

        epoch.setMemoryOnly(true);
        replayMocks();
        adder.apply(epoch);
        verifyMocks();
        assertEquals("child not added", 3, epoch.getStudySegments().size());
        assertEquals("Wrong child added (or in wrong position)", STUDY_SEGMENT_ID, (int) epoch.getStudySegments().get(1).getId());
        assertNotSame("Child is direct from DAO", studySegment, epoch.getStudySegments().get(1));
        assertTrue("Child is not transient", epoch.getStudySegments().get(1).isMemoryOnly());
    }

}
