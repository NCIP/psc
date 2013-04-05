/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.importer;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.service.importer.TemplateInternalReferenceIndex.Entry;
import edu.northwestern.bioinformatics.studycalendar.service.importer.TemplateInternalReferenceIndex.Key;
import junit.framework.TestCase;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;

/**
 * @author Jalpa Patel
 */
public class TemplateInternalReferenceIndexTest  extends TestCase {
    TemplateInternalReferenceIndex referenceIndex;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        referenceIndex =  new TemplateInternalReferenceIndex();
    }

    public void testGet() throws Exception {
        PlannedCalendar calendar = new PlannedCalendar();
        calendar.setGridId("CAL-GRID");
        Key key = new Key(calendar.getClass(), calendar.getGridId());
        Entry entry = referenceIndex.get(key);
        assertNotNull(entry);
        assertEquals("Key is not found", key, entry.getKey());
    }

    public void testAddPlanTreeNode() throws Exception {
        PlannedCalendar calendar = new PlannedCalendar();
        calendar.setGridId("CAL-GRID");
        referenceIndex.addChangeable(calendar);
        assertEquals("Wrong size of index map", 1, referenceIndex.getIndex().size());
        assertSame("Wrong PlanTreeNode present", calendar,
            referenceIndex.get(new Key(calendar.getClass(), calendar.getGridId())).getOriginal());
    }

    public void testAddPlanTreeNodeWithChildrenAddsChildrenRecursively() throws Exception {
        PlannedCalendar calendar = new PlannedCalendar();
        calendar.setGridId("CAL-GRID");
        calendar.getEpochs().add(setGridId("E4", new Epoch()));
        referenceIndex.addChangeable(calendar);
        assertEquals("Wrong size of index map", 2, referenceIndex.getIndex().size());
        assertNotNull("Root not added",
            referenceIndex.get(new Key(PlannedCalendar.class, "CAL-GRID")));
        assertNotNull("Child not added",
            referenceIndex.get(new Key(Epoch.class, "E4")));
    }

    public void testAddPlanTreeNodeThatCannotHaveChildrenWorksCorrectly() throws Exception {
        referenceIndex.addChangeable(setGridId("PAL-4", new PlannedActivityLabel()));
        assertEquals("Wrong size of index map", 1, referenceIndex.getIndex().size());
        assertNotNull("Wrong node added",
                referenceIndex.get(new Key(PlannedActivityLabel.class, "PAL-4")));
    }

    public void testAddPlanTreeNodeForNullGridId() throws Exception {
        PlannedCalendar calendar = new PlannedCalendar();
        referenceIndex.addChangeable(calendar);
        assertEquals("Wrong size of index map", 0, referenceIndex.getIndex().size());
    }

    public void testAddDelta() throws Exception {
        Epoch epoch = Epoch.create("Treatment", "A", "B", "C");
        epoch.setGridId("EPOCH-GRID");
        Delta<Epoch> delta = Delta.createDeltaFor(epoch);
        delta.setGridId("EPOCH-DELTA-GRID");
        referenceIndex.addDelta(delta);
        assertEquals("Wrong size of index map", 2, referenceIndex.getIndex().size());
        assertSame("Wrong Delta Object present", delta,
                referenceIndex.get(new Key(delta.getClass(), delta.getGridId())).getOriginal());
        assertSame("Wrong Referring Delta present", delta,
                referenceIndex.get(new Key(epoch.getClass(), epoch.getGridId())).getReferringDeltas().get(0));
    }

    public void testAddDeltaForNullGridId() throws Exception {
        Epoch epoch = Epoch.create("Treatment", "A", "B", "C");
        Delta<Epoch> delta = Delta.createDeltaFor(epoch);
        referenceIndex.addDelta(delta);
        assertEquals("Wrong size of index map", 0, referenceIndex.getIndex().size());
    }

    public void testAddDeltaForNullNodeGridId() throws Exception {
        Epoch epoch = Epoch.create("Treatment", "A", "B", "C");
        Delta<Epoch> delta = Delta.createDeltaFor(epoch);
        delta.setGridId("EPOCH-DELTA-GRID");
        referenceIndex.addDelta(delta);
        assertEquals("Wrong size of index map", 1, referenceIndex.getIndex().size());
    }

    public void testAddChildrenChange() throws Exception {
        Add change =  new Add();
        change.setGridId("ADD-GRID");
        Epoch epoch = Epoch.create("Treatment", "A", "B", "C");
        epoch.setGridId("EPOCH-GRID");
        change.setChild(epoch);
        referenceIndex.addChildrenChange(change);
        assertEquals("Wrong size of index map", 2, referenceIndex.getIndex().size());
        assertSame("Wrong ChildrenChange Object present", change,
                referenceIndex.get(new Key(change.getClass(), change.getGridId())).getOriginal());
        assertSame("Wrong Referring ChildrenChange present", change,
                referenceIndex.get(new Key(epoch.getClass(), epoch.getGridId())).getReferringChanges().get(0));
    }

    public void testAddChildrenChangeForNullGridId() throws Exception {
        Add change =  new Add();
        Epoch epoch = Epoch.create("Treatment", "A", "B", "C");
        change.setChild(epoch);
        referenceIndex.addChildrenChange(change);
        assertEquals("Wrong size of index map", 0, referenceIndex.getIndex().size());
    }

    public void testAddChildrenChangeForNullChildGridId() throws Exception {
        Add change =  new Add();
        change.setGridId("ADD-GRID");
        Epoch epoch = Epoch.create("Treatment", "A", "B", "C");
        change.setChild(epoch);
        referenceIndex.addChildrenChange(change);
        assertEquals("Wrong size of index map", 1, referenceIndex.getIndex().size());
    }
}
