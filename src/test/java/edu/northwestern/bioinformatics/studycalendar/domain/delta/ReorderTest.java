package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import static edu.northwestern.bioinformatics.studycalendar.domain.delta.DeltaAssertions.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;

/**
 * @author Rhett Sutphin
 */
public class ReorderTest extends StudyCalendarTestCase {
    private Reorder reorder;
    private Epoch epoch;
    private StudySegment ssa, ssb, ssc;
    private Delta<?> delta;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        reorder = new Reorder();
        epoch = setId(5, Epoch.create("Treatment", "A", "B", "C"));
        ssa = setId(1, epoch.getStudySegments().get(0));
        ssb = setId(2, epoch.getStudySegments().get(1));
        ssc = setId(3, epoch.getStudySegments().get(2));
        epoch.getStudySegments().remove(ssc);

        delta = Delta.createDeltaFor(epoch);
    }
    
    public void testIsNoop() throws Exception {
        assertTrue(Reorder.create(epoch, 4, 4).isNoop());
        assertFalse(Reorder.create(epoch, 2, 7).isNoop());
        assertFalse(Reorder.create(epoch, 1, 0).isNoop());
    }

    public void testIsMoveUp() throws Exception {
        assertFalse(Reorder.create(epoch, 4, 4).isMoveUp());
        assertFalse(Reorder.create(epoch, 2, 7).isMoveUp());
        assertTrue(Reorder.create(epoch, 1, 0).isMoveUp());
    }
    
    public void testIsMoveDown() throws Exception {
        assertFalse(Reorder.create(epoch, 4, 4).isMoveDown());
        assertTrue(Reorder.create(epoch, 2, 7).isMoveDown());
        assertFalse(Reorder.create(epoch, 1, 0).isMoveDown());
    }

    ////// MERGE TESTS

    public void testMergeIntoEmptyDelta() throws Exception {
        setReorderProperties(ssb, 1, 2);
        
        reorder.mergeInto(delta);
        assertEquals("Change not added", 1, delta.getChanges().size());
        assertSame("Wrong change added", reorder, delta.getChanges().get(0));
    }
    
    public void testMergeIntoDeltaWithEarlierReorderForSame() throws Exception {
        delta.addChange(Reorder.create(ssb, 1, 0));
        
        reorder.setChild(ssb);
        reorder.setNewIndex(2);
        reorder.mergeInto(delta);
        
        assertEquals("Extra change added", 1, delta.getChanges().size());
        assertReorder("Reorder index not updated", ssb, 1, 2, delta.getChanges().get(0));
    }
    
    public void testMergeIntoDeltaWithInterveningReorder() throws Exception {
        delta.addChanges(
            Reorder.create(ssb, 1, 0),
            Reorder.create(ssa, 1, 0));

        setReorderProperties(ssb, 0, 2);
        reorder.mergeInto(delta);

        assertEquals("Wrong number of changes: " + delta.getChanges(), 3, delta.getChanges().size());
        assertReorder("Wrong change 0", ssb, 1, 0, delta.getChanges().get(0));
        assertReorder("Wrong change 1", ssa, 1, 0, delta.getChanges().get(1));
        assertReorder("Wrong change 2", ssb, 0, 2, delta.getChanges().get(2));
    }

    public void testMergeIntoDeltaWithInterveningRemove() throws Exception {
        delta.addChanges(
            Reorder.create(ssb, 1, 0),
            Remove.create(ssa));

        setReorderProperties(ssb, 0, 1);
        reorder.mergeInto(delta);

        assertEquals("Wrong number of changes", 3, delta.getChanges().size());
        assertReorder("Wrong change 0", ssb, 1, 0, delta.getChanges().get(0));
        assertRemove("Wrong change 1",  ssa, delta.getChanges().get(1));
        assertReorder("Wrong change 2", ssb, 0, 1, delta.getChanges().get(2));
    }

    public void testMergeIntoDeltaWithInterveningAdd() throws Exception {
        delta.addChanges(
            Reorder.create(ssb, 1, 0),
            Add.create(ssc));

        setReorderProperties(ssb, 0, 2);
        reorder.mergeInto(delta);

        assertEquals("Wrong number of changes", 3, delta.getChanges().size());
        assertReorder("Wrong change 0", ssb, 1, 0, delta.getChanges().get(0));
        assertAdd("Wrong change 1",  ssc, null, delta.getChanges().get(1));
        assertReorder("Wrong change 2", ssb, 0, 2, delta.getChanges().get(2));
    }

    ////// SIBLING REMOVAL TESTS

    // sibling add

    public void testSiblingIndexedAddDeletedWhenMoveUp() throws Exception {
        Add add = Add.create(ssc, 0);
        setReorderProperties(ssb, 2, 1);

        reorder.siblingDeleted(delta, add, 0, 1);
        assertEquals("Old not decremented", 1, (int) reorder.getOldIndex());
        assertEquals("New not decremented", 0, (int) reorder.getNewIndex());
    }

    public void testSiblingIndexedAddDeletedWhenMoveDown() throws Exception {
        Add add = Add.create(ssc, 0);
        setReorderProperties(ssa, 1, 2);

        reorder.siblingDeleted(delta, add, 0, 1);
        assertEquals("Old not decremented", 0, (int) reorder.getOldIndex());
        assertEquals("New not decremented", 1, (int) reorder.getNewIndex());
    }

    public void testSiblingIndexedInterveningAddDeletedWhenMoveUp() throws Exception {
        Add add = Add.create(ssc, 1);
        setReorderProperties(ssb, 2, 0);

        reorder.siblingDeleted(delta, add, 0, 1);
        assertEquals("Old not decremented", 1, (int) reorder.getOldIndex());
        assertEquals("New decremented", 0, (int) reorder.getNewIndex());
    }

    public void testSiblingIndexedInterveningAddDeletedWhenMoveDown() throws Exception {
        Add add = Add.create(ssc, 1);
        setReorderProperties(ssa, 0, 2);

        reorder.siblingDeleted(delta, add, 0, 1);
        assertEquals("Old decremented", 0, (int) reorder.getOldIndex());
        assertEquals("New not decremented", 1, (int) reorder.getNewIndex());
    }

    public void testSiblingIndexedAddAfterDeletedWhenMoveUp() throws Exception {
        Add add = Add.create(ssc, 2);
        setReorderProperties(ssb, 1, 0);

        reorder.siblingDeleted(delta, add, 0, 1);
        assertEquals("Old decremented", 1, (int) reorder.getOldIndex());
        assertEquals("New decremented", 0, (int) reorder.getNewIndex());
    }

    public void testSiblingIndexedAddAfterDeletedWhenMoveDown() throws Exception {
        Add add = Add.create(ssc, 2);
        setReorderProperties(ssa, 0, 1);

        reorder.siblingDeleted(delta, add, 0, 1);
        assertEquals("Old decremented", 0, (int) reorder.getOldIndex());
        assertEquals("New decremented", 1, (int) reorder.getNewIndex());
    }

    public void testSiblingUnindexedAddDeleted() throws Exception {
        Add add = Add.create(ssc);
        setReorderProperties(ssb, 1, 2);

        reorder.siblingDeleted(delta, add, 0, 1);
        assertEquals("Old decremented", 1, (int) reorder.getOldIndex());
        assertEquals("New decremented", 2, (int) reorder.getNewIndex());
    }

    // sibling remove

    public void testSiblingRemoveOfEarlyChildDeletedWhenMoveUp() throws Exception {
        epoch.addStudySegment(ssc);
        Remove remove = Remove.create(ssa);
        setReorderProperties(ssc, 1, 0);

        reorder.siblingDeleted(delta, remove, 0, 1);
        assertEquals("Old not incremented", 2, (int) reorder.getOldIndex());
        assertEquals("New not incremented", 1, (int) reorder.getNewIndex());
    }

    public void testSiblingRemoveOfEarlyChildDeletedWhenMoveDown() throws Exception {
        epoch.addStudySegment(ssc);
        Remove remove = Remove.create(ssa);
        setReorderProperties(ssb, 0, 1);

        reorder.siblingDeleted(delta, remove, 0, 1);
        assertEquals("Old not incremented", 1, (int) reorder.getOldIndex());
        assertEquals("New not incremented", 2, (int) reorder.getNewIndex());
    }

    public void testSiblingRemoveOfInterveningChildDeletedWhenMoveUp() throws Exception {
        epoch.addStudySegment(ssc);
        Remove remove = Remove.create(ssb);
        setReorderProperties(ssc, 1, 0);

        reorder.siblingDeleted(delta, remove, 0, 1);
        assertEquals("Old not incremented", 2, (int) reorder.getOldIndex());
        assertEquals("New incremented", 0, (int) reorder.getNewIndex());
    }

    public void testSiblingRemoveOfInterveningChildDeletedWhenMoveDown() throws Exception {
        epoch.addStudySegment(ssc);
        Remove remove = Remove.create(ssb);
        setReorderProperties(ssa, 0, 1);

        reorder.siblingDeleted(delta, remove, 0, 1);
        assertEquals("Old incremented", 0, (int) reorder.getOldIndex());
        assertEquals("New not incremented", 2, (int) reorder.getNewIndex());
    }

    public void testSiblingRemoveOfLaterChildDeletedWhenMoveUp() throws Exception {
        epoch.addStudySegment(ssc);
        Remove remove = Remove.create(ssc);
        setReorderProperties(ssb, 1, 0);

        reorder.siblingDeleted(delta, remove, 0, 1);
        assertEquals("Old incremented", 1, (int) reorder.getOldIndex());
        assertEquals("New incremented", 0, (int) reorder.getNewIndex());
    }

    public void testSiblingRemoveOfLaterChildDeletedWhenMoveDown() throws Exception {
        epoch.addStudySegment(ssc);
        Remove remove = Remove.create(ssc);
        setReorderProperties(ssb, 0, 1);

        reorder.siblingDeleted(delta, remove, 0, 1);
        assertEquals("Old incremented", 0, (int) reorder.getOldIndex());
        assertEquals("New incremented", 1, (int) reorder.getNewIndex());
    }

    // sibling reorder
    
    public void testSiblingReorderUpDeletedWhenOldInSiblingRange() throws Exception {
        epoch.addStudySegment(ssc);
        Reorder first = Reorder.create(ssc, 2, 0);
        setReorderProperties(ssa, 1, 2);

        reorder.siblingDeleted(delta, first, 0, 1);
        assertEquals("Old not decremented", 0, (int) reorder.getOldIndex());
        assertEquals("New changed", 2, (int) reorder.getNewIndex());
    }
    
    public void testSiblingReorderUpDeletedWhenOldAtSiblingMax() throws Exception {
        epoch.addStudySegment(ssc);
        Reorder first = Reorder.create(ssc, 2, 0);
        setReorderProperties(ssb, 2, 0);

        reorder.siblingDeleted(delta, first, 0, 1);
        assertEquals("Old not decremented", 1, (int) reorder.getOldIndex());
        assertEquals("New changed", 0, (int) reorder.getNewIndex());
    }

    public void testSiblingReorderUpDeletedWhenOldOutOfSiblingRange() throws Exception {
        epoch.addStudySegment(ssc);
        Reorder first = Reorder.create(ssc, 2, 1);
        setReorderProperties(ssa, 0, 1);

        reorder.siblingDeleted(delta, first, 0, 1);
        assertEquals("Old decremented", 0, (int) reorder.getOldIndex());
        assertEquals("New changed", 1, (int) reorder.getNewIndex());
    }

    public void testSiblingReorderDownDeletedWhenOldInSiblingRange() throws Exception {
        epoch.addStudySegment(ssc);
        Reorder first = Reorder.create(ssa, 0, 2);
        setReorderProperties(ssc, 1, 2);

        reorder.siblingDeleted(delta, first, 0, 1);
        assertEquals("Old not incremented", 2, (int) reorder.getOldIndex());
        assertEquals("New changed", 2, (int) reorder.getNewIndex());
    }
    
    // testSiblingReorderDownDeletedWhenOldAtSiblingMax --> not possible

    public void testSiblingReorderDownDeletedWhenOldOutOfSiblingRange() throws Exception {
        epoch.addStudySegment(ssc);
        Reorder first = Reorder.create(ssc, 2, 1);
        setReorderProperties(ssa, 0, 1);

        reorder.siblingDeleted(delta, first, 0, 1);
        assertEquals("Old incremented", 0, (int) reorder.getOldIndex());
        assertEquals("New changed", 1, (int) reorder.getNewIndex());
    }

    private void setReorderProperties(StudySegment child, int old, int newI) {
        reorder.setChild(child);
        reorder.setOldIndex(old);
        reorder.setNewIndex(newI);
    }
}
