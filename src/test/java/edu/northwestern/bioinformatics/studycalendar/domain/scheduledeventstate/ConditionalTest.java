package edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.nwu.bioinformatics.commons.DateUtils;

import java.util.Calendar;


public class ConditionalTest extends StudyCalendarTestCase {
    public void testSummary() throws Exception {
        assertEquals("Conditional for 9/22/2004 - Conditional Reason",
            new Conditional("Conditional Reason", DateUtils.createDate(2004, Calendar.SEPTEMBER, 22)).getTextSummary());
    }

    public void testSummaryWithNoReason() throws Exception {
        assertEquals("Conditional for 11/2/2004",
            new Conditional(null, DateUtils.createDate(2004, Calendar.NOVEMBER, 2)).getTextSummary());
    }

    public void testConditionalAvailableStates() throws Exception {
        Conditional event = new Conditional("Reason", DateUtils.createDate(2004, Calendar.SEPTEMBER, 22));
        event.setConditional(true);
        assertEquals("Wrong number of available states", 3, event.getAvailableStates().size());
    }
}