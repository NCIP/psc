package edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;


public class NotAvailableTest extends StudyCalendarTestCase {
    public void testSummary() throws Exception {
        assertEquals("NA - Not Available Reason", new NotAvailable("Not Available Reason").getTextSummary());
    }

    public void testSummaryWithoutReason() throws Exception {
        assertEquals("NA", new NotAvailable().getTextSummary());
    }
}