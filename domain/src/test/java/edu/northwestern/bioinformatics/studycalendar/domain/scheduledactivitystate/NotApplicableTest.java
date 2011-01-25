package edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate;

import gov.nih.nci.cabig.ctms.lang.DateTools;
import junit.framework.TestCase;

import java.util.Calendar;


public class NotApplicableTest extends TestCase {
    public void testSummary() throws Exception {
        assertEquals("NA on 8/22/2004 - Not Available Reason", new NotApplicable("Not Available Reason", DateTools.createDate(2004, Calendar.AUGUST, 22)).getTextSummary());
    }

    public void testSummaryWithoutReason() throws Exception {
        assertEquals("NA on 8/22/2004", new NotApplicable(null, DateTools.createDate(2004, Calendar.AUGUST, 22)).getTextSummary());
    }
}