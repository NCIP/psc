/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Reorder;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;

/**
 * @author Rhett Sutphin
 */
public class ReorderMutatorTest extends StudyCalendarTestCase {
    private Reorder reorder;
    private Epoch epoch;
    private ReorderMutator reorderer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        epoch = Epoch.create("Treatment", "A", "B", "C");
        reorder = new Reorder();

        reorderer = new ReorderMutator(reorder);
    }

    public void testApplyMoveDown() throws Exception {
        reorder.setChild(epoch.getStudySegments().get(0));
        reorder.setNewIndex(1);
        reorderer.apply(epoch);

        assertEquals("Not reordered", "B", epoch.getStudySegments().get(0).getName());
        assertEquals("Not reordered", "A", epoch.getStudySegments().get(1).getName());
        assertEquals("Not reordered", "C", epoch.getStudySegments().get(2).getName());
    }

    public void testApplyMoveUp() throws Exception {
        reorder.setChild(epoch.getStudySegments().get(2));
        reorder.setNewIndex(0);
        reorderer.apply(epoch);

        assertEquals("Not reordered", "C", epoch.getStudySegments().get(0).getName());
        assertEquals("Not reordered", "A", epoch.getStudySegments().get(1).getName());
        assertEquals("Not reordered", "B", epoch.getStudySegments().get(2).getName());
    }

    public void testRevertMoveDown() throws Exception {
        reorder.setChild(epoch.getStudySegments().get(0));
        reorder.setOldIndex(1);
        reorderer.revert(epoch);

        assertEquals("Not reordered", "B", epoch.getStudySegments().get(0).getName());
        assertEquals("Not reordered", "A", epoch.getStudySegments().get(1).getName());
        assertEquals("Not reordered", "C", epoch.getStudySegments().get(2).getName());
    }

    public void testRevertMoveUp() throws Exception {
        reorder.setChild(epoch.getStudySegments().get(2));
        reorder.setOldIndex(0);
        reorderer.revert(epoch);

        assertEquals("Not reordered", "C", epoch.getStudySegments().get(0).getName());
        assertEquals("Not reordered", "A", epoch.getStudySegments().get(1).getName());
        assertEquals("Not reordered", "B", epoch.getStudySegments().get(2).getName());
    }
}
