package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.domain.delta.DeltaAssertions.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;

/**
 * @author Rhett Sutphin
 */
public class ReorderTest extends StudyCalendarTestCase {
    private Reorder reorder;
    private Epoch epoch;
    private Arm aa, ab, ac;
    private Delta<?> delta;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        reorder = new Reorder();
        epoch = setId(5, Epoch.create("Treatment", "A", "B", "C"));
        aa = setId(1, epoch.getArms().get(0));
        ab = setId(2, epoch.getArms().get(1));
        ac = setId(3, epoch.getArms().get(2));
        epoch.getArms().remove(ac);

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

    public void testMergeIntoEmptyDelta() throws Exception {
        reorder.setChild(ab);
        reorder.setOldIndex(1);
        reorder.setNewIndex(2);
        
        reorder.mergeInto(delta);
        assertEquals("Change not added", 1, delta.getChanges().size());
        assertSame("Wrong change added", reorder, delta.getChanges().get(0));
    }
    
    public void testMergeIntoDeltaWithEarlierReorderForSame() throws Exception {
        delta.addChange(Fixtures.createReorderChange(ab, 1, 0));
        
        reorder.setChild(ab);
        reorder.setNewIndex(2);
        reorder.mergeInto(delta);
        
        assertEquals("Extra change added", 1, delta.getChanges().size());
        assertReorder("Reorder index not updated", ab, 1, 2, delta.getChanges().get(0));
    }
    
    public void testMergeIntoDeltaWithInterveningReorder() throws Exception {
        delta.addChanges(
            Fixtures.createReorderChange(ab, 1, 0),
            Fixtures.createReorderChange(aa, 1, 0));

        reorder.setChild(ab);
        reorder.setOldIndex(0);
        reorder.setNewIndex(2);
        reorder.mergeInto(delta);

        assertEquals("Wrong number of changes: " + delta.getChanges(), 3, delta.getChanges().size());
        assertReorder("Wrong change 0", ab, 1, 0, delta.getChanges().get(0));
        assertReorder("Wrong change 1", aa, 1, 0, delta.getChanges().get(1));
        assertReorder("Wrong change 2", ab, 0, 2, delta.getChanges().get(2));
    }

    public void testMergeIntoDeltaWithInterveningRemove() throws Exception {
        delta.addChanges(
            Fixtures.createReorderChange(ab, 1, 0),
            Remove.create(aa));

        reorder.setChild(ab);
        reorder.setOldIndex(0);
        reorder.setNewIndex(1);
        reorder.mergeInto(delta);

        assertEquals("Wrong number of changes", 3, delta.getChanges().size());
        assertReorder("Wrong change 0", ab, 1, 0, delta.getChanges().get(0));
        assertRemove("Wrong change 1",  aa, delta.getChanges().get(1));
        assertReorder("Wrong change 2", ab, 0, 1, delta.getChanges().get(2));
    }

    public void testMergeIntoDeltaWithInterveningAdd() throws Exception {
        delta.addChanges(
            Fixtures.createReorderChange(ab, 1, 0),
            Add.create(ac));

        reorder.setChild(ab);
        reorder.setOldIndex(0);
        reorder.setNewIndex(2);
        reorder.mergeInto(delta);

        assertEquals("Wrong number of changes", 3, delta.getChanges().size());
        assertReorder("Wrong change 0", ab, 1, 0, delta.getChanges().get(0));
        assertAdd("Wrong change 1",  ac, null, delta.getChanges().get(1));
        assertReorder("Wrong change 2", ab, 0, 2, delta.getChanges().get(2));
    }
}
