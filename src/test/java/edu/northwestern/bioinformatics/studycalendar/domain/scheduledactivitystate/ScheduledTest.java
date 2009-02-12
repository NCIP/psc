package edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate;

import gov.nih.nci.cabig.ctms.lang.DateTools;
import junit.framework.TestCase;

import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class ScheduledTest extends TestCase {
    public void testSummary() throws Exception {
        assertEquals("Scheduled for 9/22/2004 - Reason",
            new Scheduled("Reason", DateTools.createDate(2004, Calendar.SEPTEMBER, 22)).getTextSummary());
    }

    public void testSummaryWithNoReason() throws Exception {
        assertEquals("Scheduled for 11/2/2004",
            new Scheduled(null, DateTools.createDate(2004, Calendar.NOVEMBER, 2)).getTextSummary());
    }

    public void testConditionalScheduledAvailableStates() throws Exception {
        Scheduled event = new Scheduled("Reason", DateTools.createDate(2004, Calendar.SEPTEMBER, 22));
        assertEquals("Wrong number of available states", 6, event.getAvailableStates(true).size());
    }

    public void testRegularScheduledAvailableStates() throws Exception {
        Scheduled event = new Scheduled("Reason", DateTools.createDate(2004, Calendar.SEPTEMBER, 22));
        assertEquals("Wrong number of available states", 4, event.getAvailableStates(false).size());
    }
}
