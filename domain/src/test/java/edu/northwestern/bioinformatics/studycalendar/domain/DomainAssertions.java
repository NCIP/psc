package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.tools.DayRange;
import static junit.framework.Assert.*;


/**
 * @author Rhett Sutphin
 */
public class DomainAssertions {
    public static void assertDayRange(Integer expectedStart, Integer expectedEnd, DayRange actual) {
        assertEquals("Wrong start day", expectedStart, actual.getStartDay());
        assertEquals("Wrong end day", expectedEnd, actual.getEndDay());
    }
}
