package edu.northwestern.bioinformatics.studycalendar.utils;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import static gov.nih.nci.cabig.ctms.lang.DateTools.*;

import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class DateRangeTest extends StudyCalendarTestCase {
    public void testDayCountForSingleDay() throws Exception {
        assertEquals(1, new DateRange(
            DateTools.createDate(2008, Calendar.MAY, 3),
            DateTools.createDate(2008, Calendar.MAY, 3)
        ).getDayCount());
    }
    
    public void testDayCountForMultipleDaysIsInclusive() throws Exception {
        assertEquals(45, new DateRange(
            createDate(2008, Calendar.MAY, 4),
            createDate(2008, Calendar.JUNE, 17)
        ).getDayCount());
    }
}
