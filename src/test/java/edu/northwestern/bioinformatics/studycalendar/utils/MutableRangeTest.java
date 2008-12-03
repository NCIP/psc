package edu.northwestern.bioinformatics.studycalendar.utils;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class MutableRangeTest extends StudyCalendarTestCase {
    public void testAddWhenNonOverlapping() throws Exception {
        Date oneStart = DateTools.createDate(2007, Calendar.MARCH, 5);
        Date oneStop = DateTools.createDate(2007, Calendar.MARCH, 17);
        MutableRange<Date> one = new MutableRange<Date>(oneStart, oneStop);
        MutableRange<Date> two = new MutableRange<Date>(
            DateTools.createDate(2007, Calendar.SEPTEMBER, 5), DateTools.createDate(2007, Calendar.SEPTEMBER, 12));

        one.add(two);
        assertEquals("Wrong start", oneStart, one.getStart());
        assertEquals("Wrong stop", two.getStop(), one.getStop());
    }

    public void testAddWhenIncluded() throws Exception {
        Date oneStart = DateTools.createDate(2007, Calendar.MARCH, 5);
        Date oneStop = DateTools.createDate(2007, Calendar.DECEMBER, 17);
        MutableRange<Date> one = new MutableRange<Date>(oneStart, oneStop);
        MutableRange<Date> two = new MutableRange<Date>(
            DateTools.createDate(2007, Calendar.SEPTEMBER, 5), DateTools.createDate(2007, Calendar.SEPTEMBER, 12));

        one.add(two);
        assertEquals("Wrong start", oneStart, one.getStart());
        assertEquals("Wrong stop", oneStop, one.getStop());
    }
}
