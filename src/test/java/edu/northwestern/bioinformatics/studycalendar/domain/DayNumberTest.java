package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Rhett Sutphin
 */
public class DayNumberTest extends StudyCalendarTestCase {
    public void testStringRepresentationInCycle() throws Exception {
        assertEquals("C1D1", DayNumber.create(1, 1).toString());
        assertEquals("C3D8", DayNumber.create(3, 8).toString());
        assertEquals("C2311D45", DayNumber.create(2311, 45).toString());
    }

    public void testNoCycleInformationForNegativeDays() throws Exception {
        assertEquals("-5", DayNumber.create(4, -5).toString());
    }
    
    public void testNoCycleInformationForDayZero() throws Exception {
        assertEquals("0", DayNumber.create(4, 0).toString());
    }
    
    public void testStringRepresentationOutsideOfCycle() throws Exception {
        assertEquals("1", DayNumber.create(1).toString());
        assertEquals("0", DayNumber.create(0).toString());
        assertEquals("-11", DayNumber.create(-11).toString());
    }
    
    public void testCreateFromDayAndCycleLengthInFirstCycle() throws Exception {
        assertEquals("C1D4", DayNumber.createCycleDayNumber(4, 21).toString());
    }

    public void testCreateFromDayAndCycleLengthInLaterCycle() throws Exception {
        assertEquals("C2D14", DayNumber.createCycleDayNumber(35, 21).toString());
    }

    public void testCycleIsEvenForCycle4() throws Exception {
        assertEquals("even", DayNumber.create(4, 3).getCycleEvenOrOdd());
    }

    public void testCycleIsOddForCycle13() throws Exception {
        assertEquals("odd", DayNumber.create(13, 3).getCycleEvenOrOdd());
    }
    
    public void testCycleIsNeitherOddOrEvenWithNoCycleInformation() throws Exception {
        assertNull(DayNumber.create(3).getCycleEvenOrOdd());
    }
}
