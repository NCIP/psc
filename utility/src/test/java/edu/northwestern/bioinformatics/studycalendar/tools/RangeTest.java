/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools;

import gov.nih.nci.cabig.ctms.lang.DateTools;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.*;
import junit.framework.TestCase;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Moses Hohman
 */
public class RangeTest extends TestCase {
    public void testCompareBasedOnEnd() {
        Range<Date> r1 = createDateRange(5, 15);
        Range<Date> r2 = createDateRange(5, 16);
        assertNegative("r1 not < r2", r1.compareTo(r2));
        assertPositive("r2 not > r1", r2.compareTo(r1));
    }

    public void testCompareBasedOnStartDateIfStopDatesEqual() {
        Range<Date> r1 = createDateRange(5, 15);
        Range<Date> r2 = createDateRange(6, 15);
        assertNegative("r1 not < r2", r1.compareTo(r2));
        assertPositive("r2 not > r1", r2.compareTo(r1));
    }

    public void testCompareZeroWhenEqual() {
        Range<Date> r1 = createDateRange(5, 15);
        Range<Date> r2 = createDateRange(5, 15);
        assertEquals("r1 != r2", 0, r1.compareTo(r2));
        assertEquals("r2 != r1", 0, r2.compareTo(r1));
    }

    public void testCompareWithIntegerRange() throws Exception {
        Range<Integer> r1 = new Range<Integer>(0, 2);
        Range<Integer> r2 = new Range<Integer>(1, 2);
        assertNegative("r1 not < r2", r1.compareTo(r2));
        assertPositive("r2 not > r1", r2.compareTo(r1));
    }

    public void testCompareDateToTimestampWorks() {
        Range<Date> r1 = new Range<Date>(
            DateTools.createDate(2005, Calendar.JULY, 11), null);
        Range<Date> r2 = new Range<Date>(
            DateTools.createTimestamp(2005, Calendar.JULY, 12), null);
        assertNegative("r1 not < r2", r1.compareTo(r2));
        assertPositive("r2 not > r1", r2.compareTo(r1));
    }

    public void testStopDateNullIsHigh() {
        Range<Date> r1 = createDateRange(5, 15);
        Range<Date> r2 = createRangeWithNullStopDate(5);
        assertNegative("r1 not < r2", r1.compareTo(r2));
        assertPositive("r2 not > r1", r2.compareTo(r1));
        Range<Date> r3 = createRangeWithNullStopDate(5);
        Range<Date> r4 = createRangeWithNullStopDate(6);
        assertNegative("r3 not < r4", r3.compareTo(r4));
        assertPositive("r4 not > r3", r4.compareTo(r3));
        Range<Date> r5 = createRangeWithNullStopDate(5);
        Range<Date> r6 = createRangeWithNullStopDate(5);
        assertEquals("r5 != r6", 0, r5.compareTo(r6));
        assertEquals("r6 != r5", 0, r6.compareTo(r5));
    }

    private static Range<Date> createDateRange(int startDay, int stopDay) {
        return new Range<Date>(
                DateTools.createDate(2005, Calendar.JULY, startDay),
                DateTools.createDate(2005, Calendar.JULY, stopDay));
    }

    private static Range<Date> createRangeWithNullStopDate(int startDay) {
        return new Range<Date>(
                DateTools.createDate(2005, Calendar.JULY, startDay),
                null);
    }

    public void testIntersectsWhenSuperset() {
        Range<Date> superset = createDateRange(2, 22);
        Range<Date> subset = createDateRange(6, 14);
        Range<Date> indefinite = createRangeWithNullStopDate(1);

        assertTrue("Concrete superset intersects concrete subset", superset.intersects(subset));
        assertTrue("Concrete subset intersects concrete superset", subset.intersects(superset));
        assertTrue("Indefinite superset intersects concrete subset", indefinite.intersects(subset));
        assertTrue("concrete subset intersects indefinite superset", subset.intersects(indefinite));
    }

    public void testIntersectsWhenOverlapping() {
        Range<Date> left = createDateRange(1, 8);
        Range<Date> right = createDateRange(6, 15);
        Range<Date> indefinite = createRangeWithNullStopDate(12);

        assertTrue("Left intersects with right", left.intersects(right));
        assertTrue("Right intersects with left", right.intersects(left));
        assertTrue("Right intersects with indefinite", right.intersects(indefinite));
        assertTrue("Indefinite intersects with right", indefinite.intersects(right));
    }

    public void testIntersectsWhenTangent() {
        Range<Date> left = createDateRange(1, 8);
        Range<Date> right = createDateRange(8, 15);
        Range<Date> indefinite = createRangeWithNullStopDate(15);

        assertTrue("Left intersects with right", left.intersects(right));
        assertTrue("Right intersects with left", right.intersects(left));
        assertTrue("Right intersects with indefinite", right.intersects(indefinite));
        assertTrue("Indefinite intersects with right", indefinite.intersects(right));
    }

    public void testSelfIntersectsSelf() {
        Range<Date> concrete = createDateRange(5, 12);
        Range<Date> indefinite = createRangeWithNullStopDate(15);

        assertTrue("Concrete intersects self", concrete.intersects(concrete));
        assertTrue("Indefinite intersects self", indefinite.intersects(indefinite));
    }

    public void testIncludesRange() throws Exception {
        Range<Date> one = createDateRange(3, 15);
        Range<Date> two = createDateRange(6, 12);

        assertTrue(one.includes(two));
        assertFalse(two.includes(one));
    }

    public void testConcreteRangeIncludesSelf() throws Exception {
        Range<Date> concrete = createDateRange(5, 12);
        assertTrue(concrete.includes(concrete));
    }

    public void testIndefiniteRangeIncludesSelf() throws Exception {
        Range<Date> indefinite = createRangeWithNullStopDate(5);
        assertTrue(indefinite.includes(indefinite));
    }

    public void testHasBound() {
        Range<Integer> finite        = new Range<Integer>(3, 8);
        Range<Integer> infinite      = new Range<Integer>(null, null);
        Range<Integer> infiniteLeft  = new Range<Integer>(null, 8);
        Range<Integer> infiniteRight = new Range<Integer>(3, null);

        assertTrue(finite.hasBound());
        assertTrue(infiniteLeft.hasBound());
        assertTrue(infiniteRight.hasBound());
        assertFalse(infinite.hasBound());
    }
}
