/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.DomainTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.NaturallyKeyed;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.UniquelyKeyed;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import java.util.Calendar;
import java.util.Date;

import static edu.northwestern.bioinformatics.studycalendar.domain.delta.DeltaAssertions.*;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.assertNotEquals;

/**
 * @author Rhett Sutphin
 */
public class PropertyChangeTest extends DomainTestCase {
    private static final Date NOW = DateTools.createDate(2025, Calendar.MAY, 9);

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

        change.mergeInto(delta, NOW);
        assertEquals("Wrong number of changes", 1, delta.getChanges().size());
        assertSame("Wrong change", change, delta.getChanges().get(0));
        assertChangeTime("Time not updated", NOW, change);
    }

    public void testMergeWithExistingChangeForProperty() throws Exception {
        delta.addChange(PropertyChange.create("startDay", "2", "5"));

        change.setPropertyName("startDay");
        change.setOldValue("5");
        change.setNewValue("8");
        change.mergeInto(delta, NOW);

        assertEquals("Change not merged", 1, delta.getChanges().size());
        assertPropertyChange("Result values not merged", "startDay", "2", "8", delta.getChanges().get(0));
        assertChangeTime("Merged change not timestamped", NOW, delta.getChanges().get(0));
    }

    public void testMergeWithNoop() throws Exception {
        change.setPropertyName("startDay");
        change.setOldValue("5");
        change.setNewValue("5");
        change.mergeInto(delta, NOW);

        assertEquals("Change should not have been merged", 0, delta.getChanges().size());
    }

    public void testMergeWithExistingChangeForADifferentProperty() throws Exception {
        delta.addChange(PropertyChange.create("name", "Second", "First"));

        change.setPropertyName("duration.quantity");
        change.setOldValue("3");
        change.setNewValue("6");
        change.mergeInto(delta, NOW);

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

    public void testCreateUsesNaturalKeyIfPresent() throws Exception {
        Object newValue = new NaturallyKeyed() {
            public String getNaturalKey() {
                return "natty";
            }
        };
        assertEquals("natty", PropertyChange.create("dc", null, newValue).getNewValue());
    }
    
    public void testCreatePrefersUniqueKeyToNaturalKey() throws Exception {
        Object newValue = Fixtures.createActivity("codec");
        assertTrue("Test assumption failure", newValue instanceof UniquelyKeyed);
        assertTrue("Test assumption failure", newValue instanceof NaturallyKeyed);
        assertEquals("Fixtures Source|codec",
            PropertyChange.create("dc", null, newValue).getNewValue());
    }

    public void testEqualsWhenPropertyChangeHasSameAttributes() throws Exception {
        PropertyChange change1 = PropertyChange.create("startDay", "3", "6");
        PropertyChange change2 = PropertyChange.create("startDay", "3", "6");
        assertEquals("PropertyChanges are not equals", change1, change2);
    }

    public void testEqualsWhenPropertyChangeHasDifferentPropertyName() throws Exception {
        PropertyChange change1 = PropertyChange.create("duration.quantity", "3", "6");
        PropertyChange change2 = PropertyChange.create("startDay", "3", "6");
        assertNotEquals("PropertyChanges are equals", change1, change2);
    }

    public void testEqualsWhenPropertyChangeHasDifferentOldValue() throws Exception {
        PropertyChange change1 = PropertyChange.create("startDay", "3", "6");
        PropertyChange change2 = PropertyChange.create("startDay", "4", "6");
        assertNotEquals("PropertyChanges are equals", change1, change2);
    }

    public void testEqualsWhenPropertyChangeHasDifferentNewValue() throws Exception {
        PropertyChange change1 = PropertyChange.create("startDay", "3", "6");
        PropertyChange change2 = PropertyChange.create("startDay", "3", "7");
        assertNotEquals("PropertyChanges are equals", change1, change2);
    }

    public void testDeepEqualsWithDifferentPropertyName() throws Exception {
        PropertyChange change1 = PropertyChange.create("duration.quantity", "3", "6");
        PropertyChange change2 = PropertyChange.create("startDay", "3", "6");
        assertDifferences(change1.deepEquals(change2),
            "property \"duration.quantity\" does not match \"startDay\"");
    }

    public void testDeepEqualsWithDifferentOldValue() throws Exception {
        PropertyChange change1 = PropertyChange.create("startDay", "2", "6");
        PropertyChange change2 = PropertyChange.create("startDay", "3", "6");
        assertDifferences(change1.deepEquals(change2),
            "old value \"2\" does not match \"3\"");
    }

    public void testDeepEqualsWithDifferentNewValue() throws Exception {
        PropertyChange change1 = PropertyChange.create("startDay", "3", "9");
        PropertyChange change2 = PropertyChange.create("startDay", "3", "6");
        assertDifferences(change1.deepEquals(change2),
            "new value \"9\" does not match \"6\"");
    }

    public void testNaturalKey() throws Exception {
        assertEquals("property change for startDay",
            PropertyChange.create("startDay", "0", "4").getNaturalKey());
    }
}
