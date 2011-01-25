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
}
