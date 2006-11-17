package edu.northwestern.bioinformatics.studycalendar.domain;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.utils.DayRange;

import java.util.List;

/**
 * @author Moses Hohman
 * @author Rhett Sutphin
 */
public class PeriodTest extends StudyCalendarTestCase {
    private Period period = new Period();

    public void testDisplayNameWithName() throws Exception {
        period.setName("Name-o");
        assertEquals("Name-o", period.getDisplayName());
    }

    public void testDisplayNameWithNoName() throws Exception {
        period.setName(null);
        assertEquals("[period]", period.getDisplayName());
    }

    public void testDurationNeverNull() {
        Period p = new Period();
        p.setDuration(null);
        assertNotNull(p.getDuration());
    }

    public void testDefaults() throws Exception {
        assertEquals("Default start day is 1", 1, (int) period.getStartDay());
        assertEquals("Default duration is 1 day", 1, (int) period.getDuration().getQuantity());
        assertEquals("Default duration is 1 day", Duration.Unit.day, period.getDuration().getUnit());
        assertEquals("Default reps is 1", 1, period.getRepetitions());
    }

    public void testDayRange() {
        period.setStartDay(1);
        period.setRepetitions(2);
        period.setDuration(new Duration(2, Duration.Unit.week));
        assertDayRange(1, 28, period.getTotalDayRange());
    }
    
    public void testDayRangeWithNegativeStart() throws Exception {
        period.setStartDay(-7);
        period.setDuration(new Duration(4, Duration.Unit.day));
        period.setRepetitions(1);

        assertDayRange(-7, -4, period.getTotalDayRange());
        period.setRepetitions(2);
        assertDayRange(-7, 0, period.getTotalDayRange());
        period.setRepetitions(3);
        assertDayRange(-7, 4, period.getTotalDayRange());
    }
    
    public void testGetDayRanges() throws Exception {
        period.setStartDay(-4);
        period.setDuration(new Duration(3, Duration.Unit.day));
        period.setRepetitions(4);

        List<DayRange> actual = period.getDayRanges();
        assertEquals("Wrong number of ranges", 4, actual.size());
        assertDayRange(-4, -2, actual.get(0));
        assertDayRange(-1,  1, actual.get(1));
        assertDayRange(2, 4, actual.get(2));
        assertDayRange(5, 7, actual.get(3));
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
