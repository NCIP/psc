package edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;


public class NotAvailableTest extends StudyCalendarTestCase {
    public void testSummary() throws Exception {
        assertEquals("NA - Not Available Reason", new NotAvailable("Not Available Reason").getTextSummary());
    }

    public void testSummaryWithoutReason() throws Exception {
        assertEquals("NA", new NotAvailable().getTextSummary());
    }

    public void testNotAvailableAvailableStates() throws Exception {
        NotAvailable event = new NotAvailable("Reason");
        assertEquals("Wrong number of available states", 3, event.getAvailableStates(true).size());
    }
}