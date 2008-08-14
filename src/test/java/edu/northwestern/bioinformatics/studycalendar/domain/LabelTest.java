package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Rhett Sutphin
 */
public class LabelTest extends StudyCalendarTestCase {
    public void testLabelsAreSortedCaseInsensitively() throws Exception {
        Label l1 = Fixtures.createNamedInstance("Foo", Label.class);
        Label l2 = Fixtures.createNamedInstance("bar", Label.class);

        assertPositive(l1.compareTo(l2));
        assertNegative(l2.compareTo(l1));
    }
    
    public void testLabelsAreSortedByCaseIfOtherwiseIdentical() throws Exception {
        Label l1 = Fixtures.createNamedInstance("Foo", Label.class);
        Label l2 = Fixtures.createNamedInstance("foo", Label.class);

        assertNegative(l1.compareTo(l2));
        assertPositive(l2.compareTo(l1));
    }

    public void testLabelsAreEqualWithSameName() throws Exception {
        Label l1 = Fixtures.createNamedInstance("foo", Label.class);
        Label l2 = Fixtures.createNamedInstance("foo", Label.class);
        assertEquals(l1, l2);
    }

    public void testLabelsAreNotEqualWithDifferentNames() throws Exception {
        Label l1 = Fixtures.createNamedInstance("foo", Label.class);
        Label l2 = Fixtures.createNamedInstance("bar", Label.class);
        assertNotEquals(l1, l2);
    }

    public void testLabelsHashCodesAreTheSameForSameName() throws Exception {
        Label l1 = Fixtures.createNamedInstance("foo", Label.class);
        Label l2 = Fixtures.createNamedInstance("foo", Label.class);
        assertEquals(l1.hashCode(), l2.hashCode());
    }

    public void testToString() throws Exception {
        assertEquals("Label[foo]", Fixtures.createNamedInstance("foo", Label.class).toString());
    }
}
