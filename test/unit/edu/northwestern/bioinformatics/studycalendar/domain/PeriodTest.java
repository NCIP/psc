package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;

/**
 * @author Moses Hohman
 * @author Rhett Sutphin
 */
public class PeriodTest extends StudyCalendarTestCase {
    private Period period = new Period();

    public void testDurationNeverNull() {
        Period p = new Period();
        p.setDuration(null);
        assertNotNull(p.getDuration());
    }

    public void testEndDay() {
        assertNull("not null when startDay null", period.getEndDay());
        period.setStartDay(1);
        assertNull("not null when duration blank", period.getEndDay());
        period.setRepetitions(2);
        period.setDuration(new Duration(2, Duration.Unit.week));
        assertEquals(new Integer(28), period.getEndDay());
    }
    
    public void testEndDayWithNegativeStart() throws Exception {
        period.setStartDay(-7);
        period.setDuration(new Duration(4, Duration.Unit.day));
        period.setRepetitions(1);

        assertEquals(-4, (int) period.getEndDay());
        period.setRepetitions(2);
        assertEquals(0, (int) period.getEndDay());
        period.setRepetitions(3);
        assertEquals(4, (int) period.getEndDay());
    }

    public void testAddPlannedEventMaintainsBidirectionality() throws Exception {
        PlannedEvent e = createPlannedEvent("Any", 5);
        assertNull(e.getPeriod());
        period.addPlannedEvent(e);
        assertSame(period, e.getPeriod());
    }

    public void testAddPlannedEventAdds() throws Exception {
        assertEquals(0, period.getPlannedEvents().size());
        PlannedEvent e = createPlannedEvent("Any", 5);
        period.addPlannedEvent(e);

        assertEquals(1, period.getPlannedEvents().size());
        assertSame(e, period.getPlannedEvents().iterator().next());
    }

    public void testSortAscendingByStartDayFirst() throws Exception {
        Period p1 = createPeriod("DC1", 1, 7, 1);
        Period p2 = createPeriod("DC2", 2, 6, 1);
        assertNegative(p1.compareTo(p2));
        assertPositive(p2.compareTo(p1));
    }

    public void testSortAscendingByTotalDaysSecond() throws Exception {
        Period p1 = createPeriod("DC1", 2, 7, 1);
        Period p2 = createPeriod("DC2", 2, 3, 2);
        assertNegative(p2.compareTo(p1));
        assertPositive(p1.compareTo(p2));
    }

    public void testSortDescendingByRepetitionsThird() throws Exception {
        Period p1 = createPeriod("DC1", 1, 21, 1);
        Period p2 = createPeriod("DC2", 1,  7, 3);
        assertNegative(p2.compareTo(p1));
        assertPositive(p1.compareTo(p2));
    }

    public void testSortByNameLast() throws Exception {
        Period p1 = createPeriod("B", 1, 7, 1);
        Period p2 = createPeriod("A", 1, 7, 1);
        assertNegative(p2.compareTo(p1));
        assertPositive(p1.compareTo(p2));
    }

    public void testIsFirstDayOfRep() throws Exception {
        Period p1 = createPeriod("A", 1, 7, 8);
        assertTrue(p1.isFirstDayOfRepetition(1));
        assertTrue(p1.isFirstDayOfRepetition(8));
        assertTrue(p1.isFirstDayOfRepetition(15));
        assertTrue(p1.isFirstDayOfRepetition(50));
        assertFalse("Out of range considered first day", p1.isFirstDayOfRepetition(57));
        assertFalse("Second day considered first day", p1.isFirstDayOfRepetition(2));
        assertFalse("Second day considered first day", p1.isFirstDayOfRepetition(9));
    }
    
    public void testIsFirstDayOfRepNonOnePeriodStart() throws Exception {
        Period p1 = createPeriod("B", 4, 5, 5);
        assertTrue(p1.isFirstDayOfRepetition(4));
        assertTrue(p1.isFirstDayOfRepetition(9));
        assertTrue(p1.isFirstDayOfRepetition(14));
        assertTrue(p1.isFirstDayOfRepetition(19));
        assertTrue(p1.isFirstDayOfRepetition(24));
        assertFalse("Out of range considered first day", p1.isFirstDayOfRepetition(29));
        assertFalse("Second day considered first day", p1.isFirstDayOfRepetition(5));
        assertFalse("Second day considered first day", p1.isFirstDayOfRepetition(10));
    }

    public void testIsLastDayOfRep() throws Exception {
        Period p1 = createPeriod("A", 1, 7, 8);
        assertTrue(p1.isLastDayOfRepetition(7));
        assertTrue(p1.isLastDayOfRepetition(14));
        assertTrue(p1.isLastDayOfRepetition(21));
        assertTrue(p1.isLastDayOfRepetition(56));
        assertFalse("Out of range considered last day", p1.isLastDayOfRepetition(63));
        assertFalse("Second day considered last day", p1.isLastDayOfRepetition(2));
        assertFalse("Second day considered last day", p1.isLastDayOfRepetition(9));
    }
    
    public void testIsLastDayOfRepNonOnePeriodStart() throws Exception {
        Period p1 = createPeriod("B", 4, 5, 5);
        assertTrue(p1.isLastDayOfRepetition(8));
        assertTrue(p1.isLastDayOfRepetition(13));
        assertTrue(p1.isLastDayOfRepetition(18));
        assertTrue(p1.isLastDayOfRepetition(23));
        assertTrue(p1.isLastDayOfRepetition(28));
        assertFalse("Out of range considered last day", p1.isLastDayOfRepetition(33));
        assertFalse("Second day considered last day", p1.isLastDayOfRepetition(5));
        assertFalse("Second day considered last day", p1.isLastDayOfRepetition(10));
    }
    
    public void testIsFirstDayOfRepWithNegDays() throws Exception {
        Period p1 = createPeriod("A", -6, 5, 2);
        assertTrue(p1.isFirstDayOfRepetition(-6));
        assertTrue(p1.isFirstDayOfRepetition(-1));
        assertFalse("Out of range considered first day", p1.isFirstDayOfRepetition(5));
        assertFalse("Second day considered first day", p1.isFirstDayOfRepetition(-5));
    }
    
    public void testIsLastDayOfRepWithNegDays() throws Exception {
        Period p1 = createPeriod("A", -6, 5, 2);
        assertTrue(p1.isLastDayOfRepetition(-2));
        assertTrue(p1.isLastDayOfRepetition(3));
        assertFalse("Out of range considered last day", p1.isFirstDayOfRepetition(5));
        assertFalse("Second day considered last day", p1.isFirstDayOfRepetition(-5));
    }
}
