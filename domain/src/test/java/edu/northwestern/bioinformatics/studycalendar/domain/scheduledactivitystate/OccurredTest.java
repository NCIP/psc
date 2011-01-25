package edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate;

import gov.nih.nci.cabig.ctms.lang.DateTools;
import junit.framework.TestCase;

import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class OccurredTest extends TestCase {
    public void testSummary() throws Exception {
        assertEquals("Occurred on 9/22/2004 - Reason",
            new Occurred("Reason", DateTools.createDate(2004, Calendar.SEPTEMBER, 22)).getTextSummary());
    }

    public void testSummaryWithNoReason() throws Exception {
        assertEquals("Occurred on 11/2/2004",
            new Occurred(null, DateTools.createDate(2004, Calendar.NOVEMBER, 2)).getTextSummary());
    }
}
