package edu.northwestern.bioinformatics.studycalendar.domain;

import junit.framework.TestCase;

/**
 * @author Moses Hohman
 */
public class PeriodTest extends TestCase {
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
        PlannedEvent e = Fixtures.createPlannedEvent("Any", 5);
        assertNull(e.getPeriod());
        period.addPlannedEvent(e);
        assertSame(period, e.getPeriod());
    }

    public void testAddPlannedEventAdds() throws Exception {
        assertEquals(0, period.getPlannedEvents().size());
        PlannedEvent e = Fixtures.createPlannedEvent("Any", 5);
        period.addPlannedEvent(e);

        assertEquals(1, period.getPlannedEvents().size());
        assertSame(e, period.getPlannedEvents().iterator().next());
    }
}
