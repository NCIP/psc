package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;

/**
 * @author Rhett Sutphin
 */
public class AddTest extends StudyCalendarTestCase {
    private static final int EPOCH_ID = 5;
    private Delta<?> delta;
    private Add add;

    private PlannedCalendar plannedCalendar;
    private Epoch epoch;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        add = new Add();
        plannedCalendar = new PlannedCalendar();
        epoch = setId(EPOCH_ID, Epoch.create("E"));
        delta = new PlannedCalendarDelta(plannedCalendar);
    }

    public void testGetNewChildIdPassesThroughFromDomainObject() throws Exception {
        add.setChildId(17);
        add.setChild(epoch);
        assertEquals(EPOCH_ID, (int) add.getChildId());
    }
    
    public void testGetNewChildUsesDirectAttributeIfNoDomainObject() throws Exception {
        add.setChild(null);
        add.setChildId(EPOCH_ID);
        assertEquals(EPOCH_ID, (int) add.getChildId());
    }

    public void testSetNewChildIdClearsNewChildIfIdsDoNotMatch() throws Exception {
        add.setChild(setId(3, new Arm()));
        add.setChildId(15);
        assertNull("New child not cleared", add.getChild());
    }

    public void testSetNewChildIdKeepsNewChildIfIdsMatch() throws Exception {
        Arm expectedChild = setId(15, new Arm());
        add.setChild(expectedChild);
        add.setChildId(expectedChild.getId());
        assertSame("New child incorrectly cleared", expectedChild, add.getChild());
    }

    public void testSetToSameChildWithChildIdOnly() throws Exception {
        Add other = new Add();
        other.setChildId(5);
        add.setToSameChildAs(other);
        assertNull(add.getChild());
        assertEquals(5, (int) add.getChildId());
    }

    public void testSetToSameChildWithConcreteChild() throws Exception {
        Add other = new Add();
        other.setChild(epoch);
        add.setToSameChildAs(other);
        assertSame(epoch, add.getChild());
    }

    ////// mergeInto tests

    public void testMergeIntoEmptyDeltaSimplyAdds() throws Exception {
        add.setChild(epoch);
        assertEquals("Test setup failure", 0, delta.getChanges().size());
        add.mergeInto(delta);
        assertEquals("No change added", 1, delta.getChanges().size());
        assertEquals("Wrong change added", add, delta.getChanges().get(0));
    }

    public void testMergeIntoDeltaWithSameAddIsNoop() throws Exception {
        Add existingAdd = Add.create(epoch);
        delta.addChange(existingAdd);

        add.setChild(epoch);
        add.mergeInto(delta);

        assertEquals("Nothing should have been added", 1, delta.getChanges().size());
        assertSame("Nothing should have changed", existingAdd, delta.getChanges().get(0));
    }
    
    public void testMergeIntoDeltaWithARemoveForTheSameChild() throws Exception {
        plannedCalendar.addChild(epoch);
        delta.addChange(Remove.create(epoch));
        
        add.setChild(epoch);
        add.mergeInto(delta);
        
        assertEquals("Remove should have been canceled", 0, delta.getChanges().size());
    }
    
    public void testMergeIntoDeltaWithRemoveForTheSameChildAndAnIndex() throws Exception {
        plannedCalendar.addChild(epoch);
        delta.addChange(Remove.create(epoch));

        add.setChild(epoch);
        add.setIndex(4);
        add.mergeInto(delta);

        assertEquals("Net effect should be one change", 1, delta.getChanges().size());
        assertEquals("Change not transmuted into reorder", ChangeAction.REORDER,
            delta.getChanges().get(0).getAction());
        assertEquals("New reorder has wrong child", epoch,
            ((ChildrenChange) delta.getChanges().get(0)).getChild());
        assertEquals("New reorder has wrong index", 4,
            (int) ((Reorder) delta.getChanges().get(0)).getNewIndex());
    }

    public void testMergeIntoDeltaForNodeWhichAlreadyContainsChild() throws Exception {
        plannedCalendar.addChild(epoch);

        add.setChild(epoch);
        add.mergeInto(delta);

        assertEquals("No change should have been added", 0, delta.getChanges().size());
    }

    ////// deleteSibling tests

    public void testDeleteAddSibDecrementsIndexIfPresentAndLater() throws Exception {
        Add toRemove = Add.create(Epoch.create("target"), 1);
        Add after = Add.create(Epoch.create("great"), 2);

        after.siblingDeleted(delta, toRemove, 0, 1);
        assertEquals(1, (int) after.getIndex());
    }
    
    public void testDeleteAddSibDoesNotDecrementIndexIfEarlier() throws Exception {
        Add toRemove = Add.create(Epoch.create("target"), 4);
        Add after = Add.create(Epoch.create("great"), 3);

        after.siblingDeleted(delta, toRemove, 0, 1);
        assertEquals(3, (int) after.getIndex());
    }

    public void testDeleteAddSibDoesNotDecrementIndexIfBefore() throws Exception {
        Add before = Add.create(Epoch.create("great"), 5);
        Add toRemove = Add.create(Epoch.create("target"), 4);

        before.siblingDeleted(delta, toRemove, 1, 0);
        assertEquals(5, (int) before.getIndex());
    }

    public void testDeleteAddSibIgnoredIfUnindexed() throws Exception {
        Add toRemove = Add.create(Epoch.create("target"), 4);
        Add after = Add.create(Epoch.create("great"));

        after.siblingDeleted(delta, toRemove, 0, 1);
        assertNull(after.getIndex());
    }

    public void testDeleteAddSibDecrementsIfUnindexedDeletedAddComesBefore() throws Exception {
        Add toRemove = Add.create(Epoch.create("target"));
        Add after = Add.create(Epoch.create("great"), 7);

        after.siblingDeleted(delta, toRemove, 0, 1);
        assertEquals(6, (int) after.getIndex());
    }

    public void testDeleteAddSibDoesNotDecrementIfUnindexedDeletedAddComesAfter() throws Exception {
        Add toRemove = Add.create(Epoch.create("target"));
        Add before = Add.create(Epoch.create("great"), 7);

        before.siblingDeleted(delta, toRemove, 1, 0);
        assertEquals(7, (int) before.getIndex());
    }

    public void testDeleteRemoveSibIfIndexedAndAfterIncrements() throws Exception {
        plannedCalendar.addEpoch(epoch);
        Add after = Add.create(Epoch.create("history"), 5);
        Remove toDel = Remove.create(epoch);

        after.siblingDeleted(delta, toDel, 0, 1);
        assertEquals(6, (int) after.getIndex());
    }

    public void testDeleteRemoveSibIfIndexedAndEqualIncrements() throws Exception {
        plannedCalendar.addEpoch(epoch);
        Add after = Add.create(Epoch.create("history"), 0);
        Remove toDel = Remove.create(epoch);

        after.siblingDeleted(delta, toDel, 0, 1);
        assertEquals(1, (int) after.getIndex());
    }

    public void testDeleteRemoveSibIfIndexedAndBeforeDoesNotIncrement() throws Exception {
        plannedCalendar.addEpoch(Epoch.create("E1"));
        plannedCalendar.addEpoch(Epoch.create("E2"));
        plannedCalendar.addEpoch(epoch);
        Add after = Add.create(Epoch.create("history"), 1);
        Remove toDel = Remove.create(epoch);

        after.siblingDeleted(delta, toDel, 0, 1);
        assertEquals(1, (int) after.getIndex());
    }

    public void testDeleteRemoveSibIfNotIndexed() throws Exception {
        plannedCalendar.addEpoch(epoch);
        Add after = Add.create(Epoch.create("history"));
        Remove toDel = Remove.create(epoch);

        after.siblingDeleted(delta, toDel, 0, 1);
        // expect no exception
    }
    
    public void testDeleteRemoveSibWhenNodeChildrenUnordered() throws Exception {
        Arm arm = new Arm();
        Period existingPeriod = new Period();
        arm.addPeriod(existingPeriod);
        Remove toDel = Remove.create(existingPeriod);
        Add after = Add.create(createNamedInstance("New", Period.class), 1);
        Delta<?> armDelta = Delta.createDeltaFor(arm, toDel, after);

        after.siblingDeleted(armDelta, toDel, 0, 1);
        assertEquals(1, (int) after.getIndex());
    }

    public void testDeleteReorderSibWhenNotIndexed() throws Exception {
        Reorder toDel = Reorder.create(Epoch.create("target"), 1, 7);
        Add after = Add.create(Epoch.create("great"));

        after.siblingDeleted(delta, toDel, 0, 1);
        assertNull(after.getIndex()); // no change
    }

    public void testDeleteReorderUpSibWithIndexBetweenReorderIndexes() throws Exception {
        Reorder toDel = Reorder.create(Epoch.create("target"), 4, 1);
        Add after = Add.create(Epoch.create("great"), 2);

        after.siblingDeleted(delta, toDel, 0, 1);
        assertEquals(1, (int) after.getIndex());
    }
    
    public void testDeleteReorderUpSibWithIndexEqualsNew() throws Exception {
        Reorder toDel = Reorder.create(Epoch.create("target"), 4, 1);
        Add after = Add.create(Epoch.create("great"), 1);

        after.siblingDeleted(delta, toDel, 0, 1);
        assertEquals(1, (int) after.getIndex());
    }

    public void testDeleteReorderUpSibWithIndexEqualsOld() throws Exception {
        Reorder toDel = Reorder.create(Epoch.create("target"), 4, 1);
        Add after = Add.create(Epoch.create("great"), 4);

        after.siblingDeleted(delta, toDel, 0, 1);
        assertEquals(3, (int) after.getIndex());
    }

    public void testDeleteReorderUpSibWithIndexAfter() throws Exception {
        Reorder toDel = Reorder.create(Epoch.create("target"), 4, 1);
        Add after = Add.create(Epoch.create("great"), 5);

        after.siblingDeleted(delta, toDel, 0, 1);
        assertEquals(5, (int) after.getIndex());
    }

    public void testDeleteReorderUpSibWithIndexBefore() throws Exception {
        Reorder toDel = Reorder.create(Epoch.create("target"), 4, 1);
        Add after = Add.create(Epoch.create("great"), 0);

        after.siblingDeleted(delta, toDel, 0, 1);
        assertEquals(0, (int) after.getIndex());
    }

    public void testDeleteReorderDownSibWithIndexBetweenReorderIndexes() throws Exception {
        Reorder toDel = Reorder.create(Epoch.create("target"), 1, 4);
        Add after = Add.create(Epoch.create("great"), 2);

        after.siblingDeleted(delta, toDel, 0, 1);
        assertEquals(3, (int) after.getIndex());
    }

    public void testDeleteReorderDownSibWithIndexEqualsNew() throws Exception {
        Reorder toDel = Reorder.create(Epoch.create("target"), 1, 4);
        Add after = Add.create(Epoch.create("great"), 4);

        after.siblingDeleted(delta, toDel, 0, 1);
        assertEquals(5, (int) after.getIndex());
    }

    public void testDeleteReorderDownSibWithIndexEqualsOld() throws Exception {
        Reorder toDel = Reorder.create(Epoch.create("target"), 1, 4);
        Add after = Add.create(Epoch.create("great"), 1);

        after.siblingDeleted(delta, toDel, 0, 1);
        assertEquals(1, (int) after.getIndex());
    }

    public void testDeleteReorderDownSibWithIndexAfter() throws Exception {
        Reorder toDel = Reorder.create(Epoch.create("target"), 1, 4);
        Add after = Add.create(Epoch.create("great"), 5);

        after.siblingDeleted(delta, toDel, 0, 1);
        assertEquals(5, (int) after.getIndex());
    }
    
    public void testDeleteReorderDownSibWithIndexBefore() throws Exception {
        Reorder toDel = Reorder.create(Epoch.create("target"), 1, 4);
        Add after = Add.create(Epoch.create("great"), 0);

        after.siblingDeleted(delta, toDel, 0, 1);
        assertEquals(0, (int) after.getIndex());
    }

    public void testDeleteReorderNoopSibWithIndex() throws Exception {
        Reorder toDel = Reorder.create(Epoch.create("target"), 4, 4);
        Add after = Add.create(Epoch.create("great"), 2);

        after.siblingDeleted(delta, toDel, 0, 1);
        assertEquals(2, (int) after.getIndex());
    }

    public void testDeleteReorderSibWhenIndexedAndEarlier() throws Exception {
        Reorder toDel = Reorder.create(Epoch.create("target"), 1, 4);
        Add after = Add.create(Epoch.create("great"), 5);

        after.siblingDeleted(delta, toDel, 1, 0);
        assertEquals(5, (int) after.getIndex());
    }
}
