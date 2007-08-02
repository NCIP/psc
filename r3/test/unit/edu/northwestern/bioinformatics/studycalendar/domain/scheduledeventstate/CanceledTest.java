package edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Rhett Sutphin
 */
public class CanceledTest extends StudyCalendarTestCase {
    public void testSummary() throws Exception {
        assertEquals("Canceled - Reason", new Canceled("Reason").getTextSummary());
    }

    public void testSummaryWithoutReason() throws Exception {
        assertEquals("Canceled", new Canceled().getTextSummary());
    }
}
