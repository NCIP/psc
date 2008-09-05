package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
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

    private static <T extends PlanTreeNode<?>> void assertDeltaFor(T node, Class<?> expectedClass) {
        Delta<T> actual = Delta.createDeltaFor(node);
        assertNotNull(actual);
        assertEquals("Wrong class", expectedClass, actual.getClass());
    }
}
