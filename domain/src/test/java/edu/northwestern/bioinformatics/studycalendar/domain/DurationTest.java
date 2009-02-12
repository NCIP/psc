package edu.northwestern.bioinformatics.studycalendar.domain;

import static edu.northwestern.bioinformatics.studycalendar.domain.Duration.Unit.*;
import junit.framework.TestCase;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.*;

/**
 * @author Moses Hohman
 * @author Rhett Sutphin
 */
public class DurationTest extends TestCase {
    public void testToString() {
        assertEquals("null (null unit)s", new Duration().toString());
        assertEquals("5 days", new Duration(5, day).toString());
        assertEquals("1 week", new Duration(1, week).toString());
    }

    public void testInDays() {
        assertNull("not null when quantity null", new Duration(null, day).getDays());
        assertNull("not null when unit null", new Duration(1, null).getDays());
        assertEquals(new Integer(35), new Duration(5, week).getDays());
    }

    public void testQuantityCannotBeNegative() {
        try {
            new Duration(-1, day);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testNaturalOrderIsByLengthInDays() throws Exception {
        Duration d1 = new Duration(4, Duration.Unit.week);
        Duration d2 = new Duration(28, Duration.Unit.day);
        Duration d3 = new Duration(17, Duration.Unit.day);

        assertEquals(0, d1.compareTo(d2));
        assertEquals(0, d2.compareTo(d1));
        assertNegative(d3.compareTo(d1));
        assertNegative(d3.compareTo(d2));
        assertPositive(d2.compareTo(d3));
    }

    public void testEqualsTrueWhenEqual() {
        Duration sevenDays1 = new Duration(7, day);
        Duration sevenDays2 = new Duration(7, day);
        assertEquals(sevenDays1, sevenDays2);
    }

    public void testEqualsFalseWhenEquivalent() {
        Duration sevenDays = new Duration(7, day);
        Duration oneWeek = new Duration(1, week);
        assertFalse(sevenDays.equals(oneWeek));
    }

    public void testHashCodesEqualWhenEqual() {
        Duration sevenDays1 = new Duration(7, day);
        Duration sevenDays2 = new Duration(7, day);
        assertEquals(sevenDays1.hashCode(), sevenDays2.hashCode());
    }
}
