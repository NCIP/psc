package edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate;

import gov.nih.nci.cabig.ctms.lang.DateTools;
import junit.framework.TestCase;

import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class CanceledTest extends TestCase {
    public void testSummary() throws Exception {
        assertEquals("Canceled for 8/22/2004 - Reason", new Canceled("Reason", DateTools.createDate(2004, Calendar.AUGUST, 22)).getTextSummary());
    }

    public void testSummaryWithoutReason() throws Exception {
        assertEquals("Canceled for 8/22/2004", new Canceled(null, DateTools.createDate(2004, Calendar.AUGUST, 22)).getTextSummary());
    }

    public void testConditionalCanceledAvailableStates() throws Exception {
        Canceled event = new Canceled("Reason", DateTools.createDate(2004, Calendar.AUGUST, 22));
        assertEquals("Wrong number of available states", 2, event.getAvailableStates(true).size());
    }

    public void testRegularCanceledAvailableStates() throws Exception {
        Canceled event = new Canceled("Reason", DateTools.createDate(2004, Calendar.AUGUST, 22));
        assertEquals("Wrong number of available states", 2, event.getAvailableStates(false).size());
    }
}
