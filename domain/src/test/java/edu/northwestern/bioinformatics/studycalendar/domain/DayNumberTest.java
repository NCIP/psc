/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import junit.framework.TestCase;

/**
 * @author Rhett Sutphin
 */
public class DayNumberTest extends TestCase {
    public void testStringRepresentationInCycle() throws Exception {
        assertEquals("C1D1", DayNumber.createCycleDayNumber(1, 1).toString());
        assertEquals("C3D8", DayNumber.createCycleDayNumber(28, 10).toString());
        assertEquals("C2311D45", DayNumber.createCycleDayNumber(115545, 50).toString());
    }

    public void testNoCycleInformationForNegativeDays() throws Exception {
        assertEquals("-5", DayNumber.createCycleDayNumber(-5, 3).toString());
    }
    
    public void testNoCycleInformationForDayZero() throws Exception {
        assertEquals("0", DayNumber.createCycleDayNumber(0, 4).toString());
    }
    
    public void testStringRepresentationOutsideOfCycle() throws Exception {
        assertEquals("1", DayNumber.createAbsoluteDayNumber(1).toString());
        assertEquals("0", DayNumber.createAbsoluteDayNumber(0).toString());
        assertEquals("-11", DayNumber.createAbsoluteDayNumber(-11).toString());
    }
    
    public void testCreateFromDayAndCycleLengthInFirstCycle() throws Exception {
        assertEquals("C1D4", DayNumber.createCycleDayNumber(4, 21).toString());
    }

    public void testCreateFromDayAndCycleLengthInLaterCycle() throws Exception {
        assertEquals("C2D14", DayNumber.createCycleDayNumber(35, 21).toString());
    }

    public void testCycleIsEvenForCycle4() throws Exception {
        assertEquals("even", DayNumber.createCycleDayNumber(37, 7).getCycleEvenOrOdd());
    }

    public void testCycleIsOddForCycle13() throws Exception {
        assertEquals("odd", DayNumber.createCycleDayNumber(17, 7).getCycleEvenOrOdd());
    }
    
    public void testCycleIsNeitherOddOrEvenWithNoCycleInformation() throws Exception {
        assertNull(DayNumber.createAbsoluteDayNumber(3).getCycleEvenOrOdd());
    }

    public void testAbsoluteDayNumberInCycle() throws Exception {
        assertEquals(50, DayNumber.createCycleDayNumber(50, 14).getAbsoluteDayNumber());
    }

    public void testAbsoluteDayNumberFromAbsolute() throws Exception {
        assertEquals(34, DayNumber.createAbsoluteDayNumber(34).getAbsoluteDayNumber());
    }
}
