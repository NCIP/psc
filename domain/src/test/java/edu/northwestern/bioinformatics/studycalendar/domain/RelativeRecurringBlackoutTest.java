/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import junit.framework.TestCase;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.*;

import java.util.Calendar;

/**
 * @author Nataliya Shurupova
 */

public class RelativeRecurringBlackoutTest extends TestCase {
    RelativeRecurringBlackout holiday = new RelativeRecurringBlackout();

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
        RelativeRecurringBlackout h1 = new RelativeRecurringBlackout();
        h1.setWeekNumber(3);
        h1.setDayOfTheWeek("Wednesday");
        h1.setMonth(Calendar.JANUARY);

        RelativeRecurringBlackout h2 = new RelativeRecurringBlackout();
        h2.setWeekNumber(4);
        h2.setDayOfTheWeek("Wednesday");
        h2.setMonth(Calendar.JANUARY);

        assertEquals("holiday are not equal ", true, h1.equals(holiday));
        assertEquals("holiday are not equal ", true, holiday.equals(h1));
        assertNotEquals("non recurring holidays are equal", true, h2.equals(holiday));
        assertNotEquals("non recurring holidays are equal", true, holiday.equals(h2));
    }
}
