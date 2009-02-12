package edu.northwestern.bioinformatics.studycalendar.utils;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static gov.nih.nci.cabig.ctms.lang.DateTools.createDate;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class MutableRangeTest extends StudyCalendarTestCase {
    public void testAddWhenNonOverlapping() throws Exception {
        Date oneStart = createDate(2007, Calendar.MARCH, 5);
        Date oneStop = createDate(2007, Calendar.MARCH, 17);
        MutableRange<Date> one = new MutableRange<Date>(oneStart, oneStop);
        MutableRange<Date> two = new MutableRange<Date>(
            createDate(2007, Calendar.SEPTEMBER, 5), createDate(2007, Calendar.SEPTEMBER, 12));

        one.add(two);
        assertEquals("Wrong start", oneStart, one.getStart());
        assertEquals("Wrong stop", two.getStop(), one.getStop());
    }

    public void testAddWhenIncluded() throws Exception {
        Date oneStart = createDate(2007, Calendar.MARCH, 5);
        Date oneStop = createDate(2007, Calendar.DECEMBER, 17);
        MutableRange<Date> one = new MutableRange<Date>(oneStart, oneStop);
        MutableRange<Date> two = new MutableRange<Date>(
            createDate(2007, Calendar.SEPTEMBER, 5), createDate(2007, Calendar.SEPTEMBER, 12));

        one.add(two);
        assertEquals("Wrong start", oneStart, one.getStart());
        assertEquals("Wrong stop", oneStop, one.getStop());
    }

    public void testAddSingleValueBeforeStart() throws Exception {
        Date originalStart = createDate(2007, Calendar.MARCH, 5);
        Date originalStop = createDate(2007, Calendar.DECEMBER, 17);
        Date toAdd = createDate(2007, Calendar.FEBRUARY, 4);
        MutableRange<Date> range = new MutableRange<Date>(originalStart, originalStop);
        range.add(toAdd);

        assertEquals("Wrong start", toAdd, range.getStart());
        assertEquals("Wrong stop", originalStop, range.getStop());
    }

    public void testAddSingleValueInRange() throws Exception {
        Date originalStart = createDate(2007, Calendar.MARCH, 5);
        Date originalStop = createDate(2007, Calendar.DECEMBER, 17);
        Date toAdd = createDate(2007, Calendar.JUNE, 4);
        MutableRange<Date> range = new MutableRange<Date>(originalStart, originalStop);
        range.add(toAdd);

        assertEquals("Wrong start", originalStart, range.getStart());
        assertEquals("Wrong stop", originalStop, range.getStop());
    }

    public void testAddSingleValueAfterStop() throws Exception {
        Date originalStart = createDate(2007, Calendar.MARCH, 5);
        Date originalStop = createDate(2007, Calendar.DECEMBER, 17);
        Date toAdd = createDate(2008, Calendar.FEBRUARY, 4);
        MutableRange<Date> range = new MutableRange<Date>(originalStart, originalStop);
        range.add(toAdd);

        assertEquals("Wrong start", originalStart, range.getStart());
        assertEquals("Wrong stop", toAdd, range.getStop());
    }
}
