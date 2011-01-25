package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.lang.DateTools;
import junit.framework.TestCase;

import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class ScheduledActivityStateTest extends TestCase {
    public void testCanceledSummary() throws Exception {
        assertEquals("Canceled for 8/22/2004 - Reason",
            ScheduledActivityMode.CANCELED.createStateInstance(DateTools.createDate(2004, Calendar.AUGUST, 22), "Reason").getTextSummary());
    }

    public void testCanceledSummaryWithoutReason() throws Exception {
        assertEquals("Canceled for 8/22/2004",
            ScheduledActivityMode.CANCELED.createStateInstance(DateTools.createDate(2004, Calendar.AUGUST, 22), null).getTextSummary());
    }

    public void testOccurredSummary() throws Exception {
        assertEquals("Occurred on 9/22/2004 - Reason",
            ScheduledActivityMode.OCCURRED.createStateInstance(DateTools.createDate(2004, Calendar.SEPTEMBER, 22), "Reason").getTextSummary());
    }

    public void testOccurredSummaryWithNoReason() throws Exception {
        assertEquals("Occurred on 11/2/2004",
            ScheduledActivityMode.OCCURRED.createStateInstance(DateTools.createDate(2004, Calendar.NOVEMBER, 2), null).getTextSummary());
    }

    public void testConditionalSummary() throws Exception {
        assertEquals("Conditional for 9/22/2004 - Conditional Reason",
            ScheduledActivityMode.CONDITIONAL.createStateInstance(DateTools.createDate(2004, Calendar.SEPTEMBER, 22), "Conditional Reason").
                getTextSummary());
    }

    public void testConditionalSummaryWithNoReason() throws Exception {
        assertEquals("Conditional for 11/2/2004",
            ScheduledActivityMode.CONDITIONAL.createStateInstance(DateTools.createDate(2004, Calendar.NOVEMBER, 2), null).
                getTextSummary());
    }

    public void testNaSummary() throws Exception {
        assertEquals("NA for 8/22/2004 - Not Available Reason",
            ScheduledActivityMode.NOT_APPLICABLE.createStateInstance(DateTools.createDate(2004, Calendar.AUGUST, 22), "Not Available Reason").
                getTextSummary());
    }

    public void testNaSummaryWithoutReason() throws Exception {
        assertEquals("NA for 8/22/2004",
            ScheduledActivityMode.NOT_APPLICABLE.createStateInstance(DateTools.createDate(2004, Calendar.AUGUST, 22), null).
                getTextSummary());
    }

    public void testScheduledSummary() throws Exception {
        assertEquals("Scheduled for 9/22/2004 - Reason",
            ScheduledActivityMode.SCHEDULED.createStateInstance(DateTools.createDate(2004, Calendar.SEPTEMBER, 22), "Reason").
                getTextSummary());
    }

    public void testScheduledSummaryWithNoReason() throws Exception {
        assertEquals("Scheduled for 11/2/2004",
            ScheduledActivityMode.SCHEDULED.createStateInstance(DateTools.createDate(2004, Calendar.NOVEMBER, 2), null).getTextSummary());
    }
}
