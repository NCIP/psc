package edu.northwestern.bioinformatics.studycalendar.utils;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Rhett Sutphin
 */
public class EmptyDayRangeTest extends StudyCalendarTestCase {
    public void testContains() throws Exception {
        assertFalse("Should contain nothing", EmptyDayRange.INSTANCE.containsDay(0));
        assertFalse("Should contain nothing", EmptyDayRange.INSTANCE.containsDay(5));
        assertFalse("Should contain nothing", EmptyDayRange.INSTANCE.containsDay(-2));
    }

    public void testBoundsAreZero() throws Exception {
        assertEquals(0, (int) EmptyDayRange.INSTANCE.getStartDay());
        assertEquals(0, (int) EmptyDayRange.INSTANCE.getEndDay());
    }

    public void testNoDays() throws Exception {
        assertEquals(0, EmptyDayRange.INSTANCE.getDays().size());
        assertEquals(0, EmptyDayRange.INSTANCE.getDayCount());
    }
}
