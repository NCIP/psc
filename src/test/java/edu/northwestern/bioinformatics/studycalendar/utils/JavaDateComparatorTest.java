package edu.northwestern.bioinformatics.studycalendar.utils;

import edu.nwu.bioinformatics.commons.testing.CoreTestCase;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 * @author rsutphin
 */
public class JavaDateComparatorTest extends CoreTestCase {
    private JavaDateComparator comparator = new JavaDateComparator();

    public void testCompareDifferentDates() {
        Date d1 = createDateForDay(1);
        Date d2 = createDateForDay(2);
        assertNegative("d1 not < d2", comparator.compare(d1, d2));
        assertPositive("d2 not > d1", comparator.compare(d2, d1));
    }

    public void testCompareEquivalentDates() {
        Date d1 = createDateForDay(4);
        Date d2 = createDateForDay(4);
        assertEquals("d1 != d2", 0, comparator.compare(d1, d2));
        assertEquals("d2 != d1", 0, comparator.compare(d2, d1));
    }

    public void testCompareDifferentDayTimestamps() {
        Date d1 = createTimestampForDay(1, 550233);
        Date d2 = createTimestampForDay(2, 3);
        assertNegative("d1 not < d2", comparator.compare(d1, d2));
        assertPositive("d2 not > d1", comparator.compare(d2, d1));
    }

    public void testCompareDifferentNanosTimestamps() {
        Date d1 = createTimestampForDay(1, 20003);
        Date d2 = createTimestampForDay(1, 23451);
        assertNegative("d1 not < d2", comparator.compare(d1, d2));
        assertPositive("d2 not > d1", comparator.compare(d2, d1));
    }

    public void testCompareEquivalentTimestamps() {
        Date d1 = createTimestampForDay(4, 88);
        Date d2 = createTimestampForDay(4, 88);
        assertEquals("d1 != d2", 0, comparator.compare(d1, d2));
        assertEquals("d2 != d1", 0, comparator.compare(d2, d1));
    }

    public void testCompareEquivalentDateAndTimestamp() {
        Date d1 = createDateForDay(4);
        Date d2 = createTimestampForDay(4, 0);
        assertEquals("d1 != d2", 0, comparator.compare(d1, d2));
        assertEquals("d2 != d1", 0, comparator.compare(d2, d1));
    }

    public void testCompareDifferentNanosDateAndTimestamp() {
        Date d1 = createDateForDay(1);
        Date d2 = createTimestampForDay(1, 23451);
        assertNegative("d1 not < d2", comparator.compare(d1, d2));
        assertPositive("d2 not > d1", comparator.compare(d2, d1));
    }

    public void testCompareDifferentDayTimestampAndDate() {
        Date d1 = createDateForDay(9);
        Date d2 = createTimestampForDay(15, 603);
        assertNegative("d1 not < d2", comparator.compare(d1, d2));
        assertPositive("d2 not > d1", comparator.compare(d2, d1));
    }

    private static Date createDateForDay(int day) {
        return edu.nwu.bioinformatics.commons.DateUtils.createDate(2005, Calendar.MARCH, day);
    }

    private static Timestamp createTimestampForDay(int day, int nanos) {
        Timestamp timestamp = edu.nwu.bioinformatics.commons.DateUtils.createTimestamp(2005, Calendar.MARCH, day);
        timestamp.setNanos(nanos);
        return timestamp;
    }
}
