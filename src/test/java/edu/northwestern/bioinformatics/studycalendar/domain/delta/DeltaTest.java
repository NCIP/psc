package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class DeltaTest extends StudyCalendarTestCase {
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
}
