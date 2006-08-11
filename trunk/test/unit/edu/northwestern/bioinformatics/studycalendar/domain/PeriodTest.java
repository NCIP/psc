package edu.northwestern.bioinformatics.studycalendar.domain;

import junit.framework.TestCase;

/**
 * @author Moses Hohman
 */
public class PeriodTest extends TestCase {
    private Period period = new Period();

    public void testDurationNeverNull() {
        Period period = new Period();
        period.setDuration(null);
        assertNotNull(period.getDuration());
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
        PlannedEvent e = new PlannedEvent();
        assertNull(e.getPeriod());
        period.addPlannedEvent(e);
        assertSame(period, e.getPeriod());
    }

    public void testAddPlannedEventAdds() throws Exception {
        assertNull(period.getPlannedEvents());
        PlannedEvent e = new PlannedEvent();
        period.addPlannedEvent(e);

        assertEquals(1, period.getPlannedEvents().size());
        assertSame(e, period.getPlannedEvents().iterator().next());
    }
}
