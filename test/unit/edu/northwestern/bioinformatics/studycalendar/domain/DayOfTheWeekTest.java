package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Nataliya Shurupova
 */

public class DayOfTheWeekTest extends StudyCalendarTestCase {
    DayOfTheWeek dayOfTheWeek = new DayOfTheWeek();

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
        DayOfTheWeek dayOfTheWeekTwo = new DayOfTheWeek();
        dayOfTheWeekTwo.setDayOfTheWeek("Monday");
        assertEquals("descriptions are the same", 2,
                dayOfTheWeekTwo.mapDayStringToInt(dayOfTheWeekTwo.getDayOfTheWeek()));
        dayOfTheWeekTwo.setDayOfTheWeek("Tuesday");
        assertEquals("descriptions are the same", 3,
                dayOfTheWeekTwo.mapDayStringToInt(dayOfTheWeekTwo.getDayOfTheWeek()));
        dayOfTheWeekTwo.setDayOfTheWeek("Wednesday");
        assertEquals("descriptions are the same", 4,
                dayOfTheWeekTwo.mapDayStringToInt(dayOfTheWeekTwo.getDayOfTheWeek()));
        dayOfTheWeekTwo.setDayOfTheWeek("Thursday");
        assertEquals("descriptions are the same", 5,
                dayOfTheWeekTwo.mapDayStringToInt(dayOfTheWeekTwo.getDayOfTheWeek()));
        dayOfTheWeekTwo.setDayOfTheWeek("Friday");
        assertEquals("descriptions are the same", 6,
                dayOfTheWeekTwo.mapDayStringToInt(dayOfTheWeekTwo.getDayOfTheWeek()));
        dayOfTheWeekTwo.setDayOfTheWeek("Saturday");
        assertEquals("descriptions are the same", 7,
                dayOfTheWeekTwo.mapDayStringToInt(dayOfTheWeekTwo.getDayOfTheWeek()));
        dayOfTheWeekTwo.setDayOfTheWeek("Sunday");
        assertEquals("descriptions are the same", 1,
                dayOfTheWeekTwo.mapDayStringToInt(dayOfTheWeekTwo.getDayOfTheWeek()));
    }

    public void testGetDisplayName() throws Exception {
        assertEquals("display names are not equal", "Monday",
                dayOfTheWeek.getDayOfTheWeek());
    }

    public void testEquals() throws Exception {
        DayOfTheWeek d1 = new DayOfTheWeek();
        d1.setDayOfTheWeek("Monday");
        d1.setDescription("Office is Closed");

        DayOfTheWeek d2 = new DayOfTheWeek();
        d2.setDayOfTheWeek("Tuesday");
        d2.setDescription("Office is Closed");

        DayOfTheWeek d3 = new DayOfTheWeek();
        d3.setDayOfTheWeek("Monday");
        d3.setDescription("Something");

        assertEquals("days of the week are not equal", true, d1.equals(dayOfTheWeek));
        assertNotEquals("days of the week are equals", true, d2.equals(dayOfTheWeek));
        assertEquals("days of the week are equals", true, d3.equals(dayOfTheWeek));
    }
    

}
