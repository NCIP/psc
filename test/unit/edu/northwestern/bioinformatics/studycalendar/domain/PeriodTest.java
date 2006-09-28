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
}
