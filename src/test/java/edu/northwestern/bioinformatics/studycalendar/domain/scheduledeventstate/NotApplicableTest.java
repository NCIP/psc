package edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;


public class NotApplicableTest extends StudyCalendarTestCase {
    public void testSummary() throws Exception {
        assertEquals("NA - Not Available Reason", new NotApplicable("Not Available Reason").getTextSummary());
    }

    public void testSummaryWithoutReason() throws Exception {
        assertEquals("NA", new NotApplicable().getTextSummary());
    }

    public void testAvailableStates() throws Exception {
        NotApplicable event = new NotApplicable("Reason");
        assertEquals("Wrong number of available states", 3, event.getAvailableStates(true).size());
    }
}