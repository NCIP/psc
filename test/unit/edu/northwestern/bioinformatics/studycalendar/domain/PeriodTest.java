package edu.northwestern.bioinformatics.studycalendar.domain;

import junit.framework.TestCase;

/**
 * @author Moses Hohman
 */
public class PeriodTest extends TestCase {
    public void testDurationNeverNull() {
        Period period = new Period();
        period.setDuration(null);
        assertNotNull(period.getDuration());
    }

    public void testEndDay() {
        Period period = new Period();
        assertNull("not null when startDay null", period.getEndDay());
        period.setStartDay(1);
        assertNull("not null when duration blank", period.getEndDay());
        period.setRepetitions(2);
        period.setDuration(new Duration(2, Duration.Unit.week));
        assertEquals(new Integer(28), period.getEndDay());
    }
}
