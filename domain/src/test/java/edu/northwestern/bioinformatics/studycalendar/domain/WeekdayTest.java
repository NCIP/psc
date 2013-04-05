/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import junit.framework.TestCase;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.*;

/**
 * @author Nataliya Shurupova
 */

public class WeekdayTest extends TestCase {
    WeekdayBlackout dayOfTheWeek = new WeekdayBlackout();

    protected void setUp() throws Exception {
        super.setUp();
        dayOfTheWeek.setDayOfTheWeek("Monday");
        dayOfTheWeek.setDescription("Office is Closed");
    }

    public void testDayOfTheWeek() throws Exception {
        assertEquals("dayOfTheWeek are not equals", "Monday",
                dayOfTheWeek.getDayOfTheWeek());
        assertNotEquals("dayOfTheWeek are equals", "Tuesday",
                dayOfTheWeek.getDayOfTheWeek());
    }

    public void testDescription() throws Exception {
        assertEquals("descriptions are the same", "Office is Closed",
                dayOfTheWeek.getDescription());
    }

    public void testMapDayStringToInt() throws Exception {
        WeekdayBlackout dayOfTheWeekTwo = new WeekdayBlackout();
        dayOfTheWeekTwo.setDayOfTheWeek("Monday");
        assertEquals("descriptions are the same", 2,
                dayOfTheWeekTwo.getDayOfTheWeekInteger());
        dayOfTheWeekTwo.setDayOfTheWeek("Tuesday");
        assertEquals("descriptions are the same", 3,
                dayOfTheWeekTwo.getDayOfTheWeekInteger());
        dayOfTheWeekTwo.setDayOfTheWeek("Wednesday");
        assertEquals("descriptions are the same", 4,
                dayOfTheWeekTwo.getDayOfTheWeekInteger());
        dayOfTheWeekTwo.setDayOfTheWeek("Thursday");
        assertEquals("descriptions are the same", 5,
                dayOfTheWeekTwo.getDayOfTheWeekInteger());
        dayOfTheWeekTwo.setDayOfTheWeek("Friday");
        assertEquals("descriptions are the same", 6,
                dayOfTheWeekTwo.getDayOfTheWeekInteger());
        dayOfTheWeekTwo.setDayOfTheWeek("Saturday");
        assertEquals("descriptions are the same", 7,
                dayOfTheWeekTwo.getDayOfTheWeekInteger());
        dayOfTheWeekTwo.setDayOfTheWeek("Sunday");
        assertEquals("descriptions are the same", 1,
                dayOfTheWeekTwo.getDayOfTheWeekInteger());
    }

    public void testGetDisplayName() throws Exception {
        assertEquals("display names are not equal", "Monday",
                dayOfTheWeek.getDayOfTheWeek());
    }

    public void testEquals() throws Exception {
        WeekdayBlackout d1 = new WeekdayBlackout();
        d1.setDayOfTheWeek("Monday");
        d1.setDescription("Office is Closed");

        WeekdayBlackout d2 = new WeekdayBlackout();
        d2.setDayOfTheWeek("Tuesday");
        d2.setDescription("Office is Closed");

        WeekdayBlackout d3 = new WeekdayBlackout();
        d3.setDayOfTheWeek("Monday");
        d3.setDescription("Something");

        assertEquals("days of the week are not equal", true, d1.equals(dayOfTheWeek));
        assertNotEquals("days of the week are equals", true, d2.equals(dayOfTheWeek));
        assertEquals("days of the week are equals", true, d3.equals(dayOfTheWeek));
    }
}
