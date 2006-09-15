package edu.northwestern.bioinformatics.studycalendar.domain;

import static edu.northwestern.bioinformatics.studycalendar.domain.Duration.Unit.week;
import static edu.northwestern.bioinformatics.studycalendar.domain.Duration.Unit.day;
import junit.framework.TestCase;

/**
 * @author Moses Hohman
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
