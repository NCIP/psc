/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import junit.framework.TestCase;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.*;

import java.util.Calendar;

/**
 * @author Nataliya Shurupova
 */

public class SpecificDateBlackoutTest extends TestCase {
    private SpecificDateBlackout recurringHoliday = new SpecificDateBlackout();
    private SpecificDateBlackout nonRecurringHoliday = new SpecificDateBlackout();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        recurringHoliday.setMonth(Calendar.JUNE);
        recurringHoliday.setDay(2);
        recurringHoliday.setDescription("holiday1");

        nonRecurringHoliday.setMonth(Calendar.JULY);
        nonRecurringHoliday.setDay(7);
        nonRecurringHoliday.setYear(2007);
        nonRecurringHoliday.setDescription("holiday2");
    }

    public void testDay() throws Exception {
        assertEquals("recurring days are not equal", 2, (int) recurringHoliday.getDay());
        assertEquals("non recurring days are not equal", 7, (int) nonRecurringHoliday.getDay());
    }


    public void testMonth() throws Exception {
        assertEquals("recurring months are not equal", Calendar.JUNE,
                (int) recurringHoliday.getMonth());
        assertEquals("non recurring months are not equal", Calendar.JULY,
                (int) nonRecurringHoliday.getMonth());
    }

    public void testYear() throws Exception {
        assertEquals("recurring years are not equal", null,
                recurringHoliday.getYear());
        assertEquals("non recurring years are not equal", 2007,
                (int) nonRecurringHoliday.getYear());
    }

    public void testStatus() throws Exception {
        assertEquals("recurring descriptions are not equal", "holiday1",
                recurringHoliday.getDescription());
        assertEquals("non recurring descriptions are not equal", "holiday2",
                nonRecurringHoliday.getDescription());
    }

    public void testGetDisplayName() throws Exception {
        String displayRecurringName = (recurringHoliday.getMonth()+1) +
                "/" + recurringHoliday.getDay();
        String displayNonRecurringName = (nonRecurringHoliday.getMonth()+1) +
                "/" + nonRecurringHoliday.getDay() +
                "/" + nonRecurringHoliday.getYear();

        assertEquals("display recurring names are not equal",
                recurringHoliday.getDisplayName(), displayRecurringName);
        assertEquals("display non recurring names are not equal",
                nonRecurringHoliday.getDisplayName(), displayNonRecurringName);
    }

    public void testEquals() throws Exception {
        SpecificDateBlackout h1 = new SpecificDateBlackout();
        h1.setMonth(Calendar.JUNE);
        h1.setDay(2);

        SpecificDateBlackout h2 = new SpecificDateBlackout();
        h2.setMonth(Calendar.JULY);
        h2.setDay(7);
        h2.setYear(2007);

        assertEquals("recurring holidays are not equal", true, h1.equals(recurringHoliday));
        assertEquals("non recurring holidays are not equal", true, h2.equals(nonRecurringHoliday));
        assertNotEquals("non recurring holidays are equal", true, h1.equals(nonRecurringHoliday));        
    }

}
