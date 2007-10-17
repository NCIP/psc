package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;

/**
 * @author Rhett Sutphin
 */
public class RemoveTest extends StudyCalendarTestCase {
    private Remove remove;
    private Epoch epoch;
    private Arm aa, ab, ac;
    private Delta<Epoch> delta;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        remove = new Remove();
        epoch = setId(5, Epoch.create("Treatment", "A", "B", "C"));
        aa = setId(1, epoch.getArms().get(0));
        ab = setId(2, epoch.getArms().get(1));
        ac = setId(3, epoch.getArms().get(2));
        epoch.getArms().remove(ac);

        delta = Delta.createDeltaFor(epoch);
    }

    public void testGetChildIdPassesThroughFromDomainObject() throws Exception {
        Epoch child = setId(4, new Epoch());
        remove.setChildId(17);
        remove.setChild(child);
        assertEquals(4, (int) remove.getChildId());
    }

    public void testGetChildUsesDirectAttributeIfNoDomainObject() throws Exception {
        remove.setChild(null);
        remove.setChildId(5);
        assertEquals(5, (int) remove.getChildId());
    }

    public void testSetChildIdClearsChildIfIdsDoNotMatch() throws Exception {
        remove.setChild(setId(3, new Arm()));
        remove.setChildId(15);
        assertNull("New child not cleared", remove.getChild());
    }

    public void testSetChildIdKeepsChildIfIdsMatch() throws Exception {
        Arm expectedChild = setId(15, new Arm());
        remove.setChild(expectedChild);
        remove.setChildId(expectedChild.getId());
        assertSame("New child incorrectly cleared", expectedChild, remove.getChild());
    }

    public void testMergeWithNoOtherChanges() throws Exception {
        remove.setChild(aa);
        remove.mergeInto(delta);

        assertEquals("Remove should have been added", 1, delta.getChanges().size());
        assertSame(remove, delta.getChanges().get(0));
    }

    public void testMergeWithEquivalentAddPresent() throws Exception {
        delta.addChange(Add.create(ac, 2));

        remove.setChild(ac);
        remove.mergeInto(delta);

        assertEquals("Remove should have canceled add", 0, delta.getChanges().size());
    }

    public void testMergeWithEquivalentRemovePresent() throws Exception {
        Remove expectedRemove = Remove.create(ab);
        delta.addChange(expectedRemove);

        remove.setChild(ab);
        remove.mergeInto(delta);

        assertEquals("Duplicate remove should not have been added", 1, delta.getChanges().size());
        assertEquals(expectedRemove, delta.getChanges().get(0));
    }

    public void testMergeWithChildNotInNode() throws Exception {
        remove.setChild(ac);
        remove.mergeInto(delta);

        assertEquals("Remove should not have been added", 0, delta.getChanges().size());
    }

    public void testMergeTwoIntoDeltaWithIndexesThenRemoveOne() throws Exception {
        Arm retained0 = setId(17, new Arm());
        Arm retained1 = setId(18, new Arm());
        Arm retained2 = setId(19, new Arm());
        delta.addChange(Add.create(retained0, 2));
        delta.addChange(Add.create(ac, 3));
        delta.addChange(Add.create(retained1, 4));
        delta.addChange(Add.create(retained2, 5));

        remove.setChild(ac);
        remove.mergeInto(delta);
        assertEquals("Wrong number of changes in delta", 3, delta.getChanges().size());
        Add add0 = (Add) delta.getChanges().get(0);
        Add add1 = (Add) delta.getChanges().get(1);
        Add add2 = (Add) delta.getChanges().get(2);
        DeltaAssertions.assertAdd("Index for earlier retained add updated",   retained0, 2, add0);
        DeltaAssertions.assertAdd("Index for later retained add not updated", retained1, 3, add1);
        DeltaAssertions.assertAdd("Index for later retained add not updated", retained2, 4, add2);
    }
}
