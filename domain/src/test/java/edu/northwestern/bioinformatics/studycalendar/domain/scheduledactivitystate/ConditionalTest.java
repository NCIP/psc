package edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate;

import gov.nih.nci.cabig.ctms.lang.DateTools;
import junit.framework.TestCase;

import java.util.Calendar;


public class ConditionalTest extends TestCase {
    public void testSummary() throws Exception {
        assertEquals("Conditional for 9/22/2004 - Conditional Reason",
            new Conditional("Conditional Reason", DateTools.createDate(2004, Calendar.SEPTEMBER, 22)).getTextSummary());
    }

    public void testSummaryWithNoReason() throws Exception {
        assertEquals("Conditional for 11/2/2004",
            new Conditional(null, DateTools.createDate(2004, Calendar.NOVEMBER, 2)).getTextSummary());
    }
}