package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import static edu.northwestern.bioinformatics.studycalendar.domain.delta.DeltaAssertions.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;

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

    public void testMergeIntoEmptyDelta() throws Exception {
        change.setPropertyName("startDay");
        change.setOldValue("5");
        change.setNewValue("8");

        change.mergeInto(delta);
        assertEquals("Wrong number of changes", 1, delta.getChanges().size());
        assertSame("Wrong change", change, delta.getChanges().get(0));
    }

    public void testMergeWithExistingChangeForProperty() throws Exception {
        delta.getChanges().add(createPropertyChange("startDay", "2", "5"));

        change.setPropertyName("startDay");
        change.setOldValue("5");
        change.setNewValue("8");
        change.mergeInto(delta);

        assertEquals("Change not merged", 1, delta.getChanges().size());
        assertPropertyChange("Result values not merged", "startDay", "2", "8", delta.getChanges().get(0));
    }

    public void testMergeWithExistingChangeForADifferentProperty() throws Exception {
        delta.getChanges().add(createPropertyChange("name", "Second", "First"));

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
}
