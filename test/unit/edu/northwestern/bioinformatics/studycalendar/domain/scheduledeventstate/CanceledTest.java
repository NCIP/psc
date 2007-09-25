package edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.nwu.bioinformatics.commons.DateUtils;

import java.util.Calendar;

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

    public void testConditionalCanceledAvailableStates() throws Exception {
        Canceled event = new Canceled("Reason");
        event.setConditional(true);
        assertEquals("Wrong number of available states", 1, event.getAvailableStates().size());
    }

    public void testRegularCanceledAvailableStates() throws Exception {
        Canceled event = new Canceled("Reason");
        event.setConditional(false);
        assertEquals("Wrong number of available states", 1, event.getAvailableStates().size());
    }
}
