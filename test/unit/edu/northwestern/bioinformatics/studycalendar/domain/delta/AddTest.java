package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
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

    public void testMergeIntoEmptyDeltaSimplyAdds() throws Exception {
        add.setChild(epoch);
        assertEquals("Test setup failure", 0, delta.getChanges().size());
        add.mergeInto(delta);
        assertEquals("No change added", 1, delta.getChanges().size());
        assertEquals("Wrong change added", add, delta.getChanges().get(0));
    }

    public void testMergeIntoDeltaWithSameAddIsNoop() throws Exception {
        Add existingAdd = createAddChange(epoch, null);
        delta.getChanges().add(existingAdd);

        add.setChild(epoch);
        add.mergeInto(delta);

        assertEquals("Nothing should have been added", 1, delta.getChanges().size());
        assertSame("Nothing should have changed", existingAdd, delta.getChanges().get(0));
    }
    
    public void testMergeIntoDeltaWithARemoveForTheSameChild() throws Exception {
        plannedCalendar.addChild(epoch);
        delta.getChanges().add(createRemoveChange(epoch));
        
        add.setChild(epoch);
        add.mergeInto(delta);
        
        assertEquals("Remove should have been canceled", 0, delta.getChanges().size());
    }
    
    public void testMergeIntoDeltaWithRemoveForTheSameChildAndAnIndex() throws Exception {
        plannedCalendar.addChild(epoch);
        delta.getChanges().add(createRemoveChange(epoch));

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
}
