package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.DomainTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import java.util.Calendar;
import java.util.Date;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.delta.DeltaAssertions.*;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.assertNotEquals;

/**
 * @author Rhett Sutphin
 */
public class RemoveTest extends DomainTestCase {
    private static final Date NOW = DateTools.createDate(2010, Calendar.JANUARY, 1);

    private Remove remove;
    private Epoch epoch;
    private StudySegment ssa, ssb, ssc;
    private Delta<Epoch> delta;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        remove = new Remove();
        epoch = setId(5, Epoch.create("Treatment", "A", "B", "C"));
        ssa = setId(1, epoch.getStudySegments().get(0));
        ssb = setId(2, epoch.getStudySegments().get(1));
        ssc = setId(3, epoch.getStudySegments().get(2));
        epoch.getStudySegments().remove(ssc);

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
        remove.setChild(setId(3, new StudySegment()));
        remove.setChildId(15);
        assertNull("New child not cleared", remove.getChild());
    }

    public void testSetChildIdKeepsChildIfIdsMatch() throws Exception {
        StudySegment expectedChild = setId(15, new StudySegment());
        remove.setChild(expectedChild);
        remove.setChildId(expectedChild.getId());
        assertSame("New child incorrectly cleared", expectedChild, remove.getChild());
    }

    public void testMergeWithNoOtherChanges() throws Exception {
        remove.setChild(ssa);
        remove.mergeInto(delta, NOW);

        assertEquals("Remove should have been added", 1, delta.getChanges().size());
        assertSame(remove, delta.getChanges().get(0));
        assertEquals("Updated date not set", NOW, remove.getUpdatedDate());
    }

    public void testMergeWithEquivalentAddPresent() throws Exception {
        delta.addChange(Add.create(ssc, 2));

        remove.setChild(ssc);
        remove.mergeInto(delta, NOW);

        assertEquals("Remove should have canceled add", 0, delta.getChanges().size());
    }

    public void testMergeWithEquivalentRemovePresent() throws Exception {
        Remove expectedRemove = Remove.create(ssb);
        delta.addChange(expectedRemove);

        remove.setChild(ssb);
        remove.mergeInto(delta, NOW);

        assertEquals("Duplicate remove should not have been added", 1, delta.getChanges().size());
        assertEquals(expectedRemove, delta.getChanges().get(0));
    }

    public void testMergeWithChildNotInNode() throws Exception {
        remove.setChild(ssc);
        remove.mergeInto(delta, NOW);

        assertEquals("Remove should not have been added", 0, delta.getChanges().size());
    }

    public void testMergeTwoIntoDeltaWithIndexesThenRemoveOne() throws Exception {
        StudySegment retained0 = setId(17, new StudySegment());
        StudySegment retained1 = setId(18, new StudySegment());
        StudySegment retained2 = setId(19, new StudySegment());
        delta.addChange(Add.create(retained0, 2));
        delta.addChange(Add.create(ssc, 3));
        delta.addChange(Add.create(retained1, 4));
        delta.addChange(Add.create(retained2, 5));

        remove.setChild(ssc);
        remove.mergeInto(delta, NOW);
        assertEquals("Wrong number of changes in delta", 3, delta.getChanges().size());
        Add add0 = (Add) delta.getChanges().get(0);
        Add add1 = (Add) delta.getChanges().get(1);
        Add add2 = (Add) delta.getChanges().get(2);
        assertAdd("Index for earlier retained add updated",   retained0, 2, add0);
        assertChangeTime("Unchanged add time incorrectly updated", null, add0);
        assertAdd("Index for later retained add not updated", retained1, 3, add1);
        assertChangeTime("Updated add time not updated", NOW, add1);
        assertAdd("Index for later retained add not updated", retained2, 4, add2);
        assertChangeTime("Updated add time not updated", NOW, add2);
    }
    
    public void testMergeWithAddFollowedByReorderCancelsBoth() throws Exception {
        delta.addChange(Add.create(ssc, 2));
        Reorder.create(ssc, 2, 1).mergeInto(delta, NOW);

        remove.setChild(ssc);
        remove.mergeInto(delta, NOW);

        assertEquals("Remove should have canceled add and reorder", 0, delta.getChanges().size());
    }
    
    public void testMergeWithReorderAloneDoesNotCancelReorder() throws Exception {
        delta.addChange(Reorder.create(ssa, 0, 1));

        remove.setChild(ssa);
        remove.mergeInto(delta, NOW);

        assertEquals("Remove should not have canceled anything", 2, delta.getChanges().size());
    }

    public void testEqualsWhenRemoveHasSameChild() throws Exception {
        Remove remove1 = Remove.create(createNamedInstance("Segment1", StudySegment.class));
        Remove remove2 = Remove.create(createNamedInstance("Segment1", StudySegment.class));
        assertEquals("Removes are not equals", remove1, remove2);
    }

    public void testEqualsWhenRemoveHasDifferentChildName() throws Exception {
        Remove remove1 = Remove.create(createNamedInstance("Segment1", StudySegment.class));
        Remove remove2 = Remove.create(createNamedInstance("Segment2", StudySegment.class));
        assertNotEquals("Removes are equals", remove1, remove2);
    }

    public void testEqualsWhenRemoveHasDifferentChild() throws Exception {
        Remove remove1 = Remove.create(createNamedInstance("Epoch1", Epoch.class));
        Remove remove2 = Remove.create(createNamedInstance("Segment2", StudySegment.class));
        assertNotEquals("Removes are equals", remove1, remove2);
    }

    public void testDeepEqualsWhenRemoveHasDifferentChild() throws Exception {
        Remove remove1 = Remove.create(setGridId("e1", Epoch.create("E1")));
        Remove remove2 = Remove.create(setGridId("e2", Epoch.create("E2")));

        assertDifferences(remove1.deepEquals(remove2), "for different child");
    }
}
