package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import static edu.northwestern.bioinformatics.studycalendar.domain.delta.DeltaAssertions.*;

/**
 * @author Rhett Sutphin
 */
public class PropertyChangeTest extends StudyCalendarTestCase {
    private PropertyChange change;
    private Period period;
    private Delta<?> delta;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        change = new PropertyChange();
        period = new Period();
        delta = Delta.createDeltaFor(period);
    }

    public void testIsNoop() throws Exception {
        assertTrue("null => null should be noop", PropertyChange.create("any", null, null).isNoop());
        assertTrue("equal old and new should be noop", PropertyChange.create("any", "aleph", "aleph").isNoop());
        assertFalse("null => not-null should be not noop", PropertyChange.create("any", null, "aleph").isNoop());
        assertFalse("not-null => null should be not noop", PropertyChange.create("any", "aleph", null).isNoop());
        assertFalse("unequal old and new should be not noop", PropertyChange.create("any", "aleph", "one").isNoop());
    }

    public void testMergeIntoEmptyDelta() throws Exception {
        change.setPropertyName("startDay");
        change.setOldValue("5");
        change.setNewValue("8");

        change.mergeInto(delta);
        assertEquals("Wrong number of changes", 1, delta.getChanges().size());
        assertSame("Wrong change", change, delta.getChanges().get(0));
    }

    public void testMergeWithExistingChangeForProperty() throws Exception {
        delta.addChange(PropertyChange.create("startDay", "2", "5"));

        change.setPropertyName("startDay");
        change.setOldValue("5");
        change.setNewValue("8");
        change.mergeInto(delta);

        assertEquals("Change not merged", 1, delta.getChanges().size());
        assertPropertyChange("Result values not merged", "startDay", "2", "8", delta.getChanges().get(0));
    }

    public void testMergeWithNoop() throws Exception {
        change.setPropertyName("startDay");
        change.setOldValue("5");
        change.setNewValue("5");
        change.mergeInto(delta);

        assertEquals("Change should not have been merged", 0, delta.getChanges().size());
    }

    public void testMergeWithExistingChangeForADifferentProperty() throws Exception {
        delta.addChange(PropertyChange.create("name", "Second", "First"));

        change.setPropertyName("duration.quantity");
        change.setOldValue("3");
        change.setNewValue("6");
        change.mergeInto(delta);

        assertEquals("Changes incorrectly merged", 2, delta.getChanges().size());
        assertPropertyChange("Wrong change 0", "name", "Second", "First",
            delta.getChanges().get(0));
        assertPropertyChange("Wrong change 1", "duration.quantity", "3", "6",
            delta.getChanges().get(1));
    }

    public void testCreateWithNullOldValue() throws Exception {
        PropertyChange actual = PropertyChange.create("f", null, "oo");
        assertNull(actual.getOldValue());
        assertEquals("oo", actual.getNewValue());
    }

    public void testCreateWithNullNewValue() throws Exception {
        PropertyChange actual = PropertyChange.create("f", "oo", null);
        assertEquals("oo", actual.getOldValue());
        assertNull(actual.getNewValue());
    }

    public void testCreateUsesToString() throws Exception {
        PropertyChange actual = PropertyChange.create("b", 6, 7);
        assertEquals("6", actual.getOldValue());
        assertEquals("7", actual.getNewValue());
    }
}
