/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.DomainTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.assertNotEquals;

/**
 * @author Rhett Sutphin
 */
public class DeltaTest extends DomainTestCase {
    private static final Date NOW = DateTools.createDate(2005, Calendar.SEPTEMBER, 5);

    public void testDeltaForPlannedCalendar() throws Exception {
        assertDeltaFor(new PlannedCalendar(), PlannedCalendarDelta.class);
    }

    public void testDeltaForEpoch() throws Exception {
        assertDeltaFor(new Epoch(), EpochDelta.class);
    }

    public void testDeltaForStudySegment() throws Exception {
        assertDeltaFor(new StudySegment(), StudySegmentDelta.class);
    }

    public void testDeltaForPeriod() throws Exception {
        assertDeltaFor(new Period(), PeriodDelta.class);
    }

    public void testDeltaForPlannedActivity() throws Exception {
        assertDeltaFor(new PlannedActivity(), PlannedActivityDelta.class);
    }

    public void testDeltaForPlannedActivityLabel() throws Exception {
        assertDeltaFor(new PlannedActivityLabel(), PlannedActivityLabelDelta.class);
    }

    public void testDeltaForPopulation() throws Exception {
        assertDeltaFor(new Population(), PopulationDelta.class);
    }    

    public void testDeltaForStudy() throws Exception {
        assertDeltaFor(new Study(), StudyDelta.class);
    }
    
    public void testAddChange() throws Exception {
        Delta<?> delta = new EpochDelta();
        assertEquals("Test setup failure", 0, delta.getChanges().size());
        delta.addChange(PropertyChange.create("nil", "null", "{}"));
        assertEquals(1, delta.getChanges().size());
    }

    public void testRemoveChangeNotifiesSiblings() throws Exception {
        Change sib0 = registerMockFor(Change.class);
        Change sib1 = registerMockFor(Change.class);
        Change sib2 = registerMockFor(Change.class);

        Delta<?> delta = new EpochDelta();
        delta.getChangesInternal().addAll(Arrays.asList(sib0, sib1, sib2));

        sib0.siblingDeleted(delta, sib1, 1, 0, NOW);
        sib2.siblingDeleted(delta, sib1, 1, 2, NOW);
        sib1.setDelta(null);

        replayMocks();
        delta.removeChange(sib1, NOW);
        verifyMocks();
    }

    public void testRemoveChangeDoesNotNotifyIfTheRemovedChangeWasNotInTheDelta() throws Exception {
        Change sib0 = registerMockFor(Change.class);
        Change sib1 = registerMockFor(Change.class);
        Change sib2 = registerMockFor(Change.class);

        Delta<?> delta = new EpochDelta();
        delta.getChangesInternal().addAll(Arrays.asList(sib0, sib2));

        replayMocks();
        delta.removeChange(sib1, NOW);
        verifyMocks();
    }

    public void testGetChangesReturnsReadOnlyList() throws Exception {
        Delta<?> delta = new EpochDelta();
        try {
            delta.getChanges().add(PropertyChange.create("aleph", "i", "I"));
            fail("Exception not thrown");
        } catch (UnsupportedOperationException e) {
            // good
        }
    }

    public void testCloneDeepClonesChanges() throws Exception {
        Delta<Epoch> src = Delta.createDeltaFor(new Epoch());
        src.addChange(PropertyChange.create("name", "A", "Aprime"));

        Delta<Epoch> clone = src.clone();
        assertEquals("Wrong number of cloned changes", 1, clone.getChanges().size());
        assertNotSame("Changes not deep cloned", src.getChanges().get(0), clone.getChanges().get(0));
        assertEquals("Changes not cloned", src.getChanges().get(0), clone.getChanges().get(0));
    }

    public void testCloneClearsParentRef() throws Exception {
        Delta<Epoch> src = Delta.createDeltaFor(new Epoch());
        src.setRevision(new Amendment());

        Delta<Epoch> clone = src.clone();
        assertNull("Clone has stale parent ref", clone.getRevision());
    }

    public void testSetMemOnlyRecursiveToChanges() throws Exception {
        Delta<Epoch> delta = Delta.createDeltaFor(new Epoch());
        delta.addChange(PropertyChange.create("name", "A", "Aprime"));
        delta.setMemoryOnly(true);
        assertTrue(delta.getChanges().get(0).isMemoryOnly());
    }

    private static <T extends Changeable> void assertDeltaFor(T node, Class<?> expectedClass) {
        Delta<T> actual = Delta.createDeltaFor(node);
        assertNotNull(actual);
        assertEquals("Wrong class", expectedClass, actual.getClass());
    }

    public void testEqualsWhenDeltaNodeAreEquals() throws Exception {
        Delta<Epoch> delta1 = Delta.createDeltaFor(new Epoch());
        Delta<Epoch> delta2 = Delta.createDeltaFor(new Epoch());
        assertEquals("Deltas are not equals", delta1, delta2);
    }

    public void testEqualsWhenDeltaNodeAreNotEquals() throws Exception {
        Delta<Epoch> delta1 = Delta.createDeltaFor(new Epoch());
        Delta<Period> delta2 = Delta.createDeltaFor(new Period());
        assertNotEquals("Deltas are equals", delta1, delta2);
    }

    public void testDeepEqualsWhenChangesAreEquals() throws Exception {
        PropertyChange change = PropertyChange.create("name", "A", "Aprime");
        Delta<Epoch> delta1 = Delta.createDeltaFor(new Epoch(), change);
        Delta<Epoch> delta2 = Delta.createDeltaFor(new Epoch(), change);

        assertFalse(delta1.deepEquals(delta2).hasDifferences());
    }

    public void testDeepEqualsWhenAlignedChangesAreComparableButNotEqual() throws Exception {
        Add add1 = Add.create(setGridId("E1", Epoch.create("E1")));
        Add add2 = Add.create(setGridId("E1", Epoch.create("E2")));

        Delta<Epoch> delta1 = Delta.createDeltaFor(new Epoch(),
            PropertyChange.create("name", "A", "B"), Add.create(new StudySegment()), add1);
        Delta<Epoch> delta2 = Delta.createDeltaFor(new Epoch(),
            PropertyChange.create("name", "A", "B"), Add.create(new StudySegment()), add2);

        assertChildDifferences(delta1.deepEquals(delta2),
            new String[] { "add of epoch:E1", "child" },
            "name \"E1\" does not match \"E2\"");
    }

    public void testDeepEqualsWhenAlignedChangesAreNotComparable() throws Exception {
        Change c1 = Add.create(setGridId("E1", Epoch.create("E1")));
        Change c2 = Remove.create(setGridId("E1", Epoch.create("E2")));

        Delta<Epoch> delta1 = Delta.createDeltaFor(new Epoch(),
            PropertyChange.create("name", "A", "B"), Add.create(new StudySegment()), c1);
        Delta<Epoch> delta2 = Delta.createDeltaFor(new Epoch(),
            PropertyChange.create("name", "A", "B"), Add.create(new StudySegment()), c2);

        assertDifferences(delta1.deepEquals(delta2),
            "add of epoch:E1 replaced by remove of epoch:E1");
    }

    public void testDeepEqualsWhenMoreChangesOnLeft() throws Exception {
        Delta<Epoch> delta1 = Delta.createDeltaFor(new Epoch(),
            PropertyChange.create("name", "A", "B"), Add.create(new StudySegment()),
            Add.create(setGridId("E1", Epoch.create("E1"))));
        Delta<Epoch> delta2 = Delta.createDeltaFor(new Epoch(),
            PropertyChange.create("name", "A", "B"), Add.create(new StudySegment()));

        assertDifferences(delta1.deepEquals(delta2),
            "missing add of epoch:E1");
    }

    public void testDeepEqualsWhenMoreChangesOnRight() throws Exception {
        Delta<Epoch> delta1 = Delta.createDeltaFor(new Epoch(),
            PropertyChange.create("name", "A", "B"), Add.create(new StudySegment()));
        Delta<Epoch> delta2 = Delta.createDeltaFor(new Epoch(),
            PropertyChange.create("name", "A", "B"), Add.create(new StudySegment()),
            Add.create(setGridId("E1", Epoch.create("E1"))));

        assertDifferences(delta1.deepEquals(delta2),
            "extra add of epoch:E1");
    }

    public void testDeepEqualsWhenDeltaNodeAreNotEqual() throws Exception {
        Delta<Epoch> delta1 = Delta.createDeltaFor(setGridId("E1", new Epoch()));
        Delta<Epoch> delta2 = Delta.createDeltaFor(setGridId("E2", new Epoch()));

        assertDifferences(delta1.deepEquals(delta2), "for different node");
    }

    public void testGetBriefDescription() throws Exception {
        StudySegment e = setGridId("3342", new StudySegment());
        assertEquals("delta for study segment 3342", Delta.createDeltaFor(e).getBriefDescription());
    }

    public void testGetNodeTypeDescriptionForEpoch() throws Exception {
        assertEquals("epoch", Delta.createDeltaFor(new Epoch()).getNodeTypeDescription());
    }

    public void testGetNodeTypeDescriptionForPC() throws Exception {
        assertEquals("planned calendar",
            Delta.createDeltaFor(new PlannedCalendar()).getNodeTypeDescription());
    }

    public void testGetNodeTypeDescriptionForPlannedActivityLabel() throws Exception {
        assertEquals("planned activity label",
            Delta.createDeltaFor(new PlannedActivityLabel()).getNodeTypeDescription());
    }
}
