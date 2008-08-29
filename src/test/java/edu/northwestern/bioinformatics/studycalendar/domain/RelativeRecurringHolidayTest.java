package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.Calendar;

/**
 * @author Nataliya Shurupova
 */

public class RelativeRecurringHolidayTest extends StudyCalendarTestCase {
    RelativeRecurringBlackoutDate holiday = new RelativeRecurringBlackoutDate();

    protected void setUp() throws Exception {
        super.setUp();
        holiday.setWeekNumber(3);
        holiday.setDayOfTheWeek("Wednesday");
        holiday.setMonth(Calendar.JANUARY);
        holiday.setDescription("Some day Off");
    }

    public void testGetNumberOfTheWeek() {
        assertEquals("Number of the week is not the same ", 3, (int) holiday.getWeekNumber());
        assertEquals("Number of the week is the same ", false, (holiday.getWeekNumber() != 3));
    }

    public void testGetDayOfTheWeek() throws Exception {
        assertEquals("Day of the week is not equals", "Wednesday", holiday.getDayOfTheWeek());
    }

    public void testGetMonth() throws Exception {
        assertEquals("Month is not the same ", Calendar.JANUARY, (int) holiday.getMonth());
    }


    public void testDisplayName() throws Exception {
        String displayHolidayName = (holiday.numberOfTheWeekString() + " " +
                holiday.getDayOfTheWeek() + " " + "of " + holiday.monthString());

        assertEquals("display recurring names are not equal",
                holiday.getDisplayName(), displayHolidayName);
    }

    public void testEquals() throws Exception {
        RelativeRecurringBlackoutDate h1 = new RelativeRecurringBlackoutDate();
        h1.setWeekNumber(3);
        h1.setDayOfTheWeek("Wednesday");
        h1.setMonth(Calendar.JANUARY);

        RelativeRecurringBlackoutDate h2 = new RelativeRecurringBlackoutDate();
        h2.setWeekNumber(4);
        h2.setDayOfTheWeek("Wednesday");
        h2.setMonth(Calendar.JANUARY);

        assertEquals("holiday are not equal ", true, h1.equals(holiday));
        assertEquals("holiday are not equal ", true, holiday.equals(h1));
        assertNotEquals("non recurring holidays are equal", true, h2.equals(holiday));
        assertNotEquals("non recurring holidays are equal", true, holiday.equals(h2));
    }
}
