package edu.northwestern.bioinformatics.studycalendar.utils;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class DayRangeTest extends StudyCalendarTestCase {
    public void testDayCount() throws Exception {
        assertEquals(12, new DayRange(1, 12).getDayCount());
        assertEquals(25, new DayRange(-12, 12).getDayCount());
        assertEquals(12, new DayRange(-12, -1).getDayCount());
        assertEquals(1, new DayRange(1, 1).getDayCount());
    }

    public void testAddSubset() throws Exception {
        DayRange r1 = new DayRange(3, 34);
        r1.add(new DayRange(6, 10));

        assertDayRange(3, 34, r1);
    }

    public void testAddIntersectingLow() throws Exception {
        DayRange r1 = new DayRange(17, 39);
        r1.add(new DayRange(13, 25));

        assertDayRange(13, 39, r1);
    }

    public void testAddIntersectingHigh() throws Exception {
        DayRange r1 = new DayRange(13, 25);
        r1.add(new DayRange(17, 39));

        assertDayRange(13, 39, r1);
    }

    public void testAddNotIntersecting() throws Exception {
        DayRange r1 = new DayRange(10, 20);
        r1.add(new DayRange(30, 40));

        assertDayRange(10, 40, r1);
    }

    public void testContains() throws Exception {
        DayRange r1 = new DayRange(-12, 3);
        assertTrue("Not contained when contained", r1.containsDay(-6));
        assertTrue("Not contained when start", r1.containsDay(-12));
        assertTrue("Not contained when end", r1.containsDay(3));

        assertFalse("Contained when outside low", r1.containsDay(-13));
        assertFalse("Contained when outside high", r1.containsDay(4));
    }
    
    public void testGetDays() throws Exception {
        assertEquals(Arrays.asList(1, 2, 3, 4), new DayRange(1, 4).getDays());
        assertEquals(Arrays.asList(-3, -2, -1, 0, 1, 2, 3, 4), new DayRange(-3, 4).getDays());
        assertEquals(Arrays.asList(11), new DayRange(11, 11).getDays());
    }
}
