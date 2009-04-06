package edu.northwestern.bioinformatics.studycalendar.domain;

import static edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode.*;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import junit.framework.TestCase;

import java.util.Calendar;
import java.util.List;

public class ScheduledActivityModeTest extends TestCase {
    public void testScheduledGetAvailableModesPos() throws Exception {
        Scheduled scheduledState = new Scheduled("Scheduled", DateTools.createDate(2007, Calendar.SEPTEMBER, 1));
        List<ScheduledActivityMode> modes = getAvailableModes(scheduledState, true);
        assertEquals("Wrong size of available modes", 6, modes.size());
    }

    public void testScheduledGetAvailableModesNeg() throws Exception {
        Scheduled scheduledState = new Scheduled("Scheduled", DateTools.createDate(2007, Calendar.SEPTEMBER, 1));
        List<ScheduledActivityMode> modes = getAvailableModes(scheduledState, false);
        assertEquals("Wrong size of available modes", 4, modes.size());
    }

    public void testGetUnscheduleMode() throws Exception {
        assertEquals(CANCELED, SCHEDULED.getUnscheduleMode());
        assertEquals(NOT_APPLICABLE, CONDITIONAL.getUnscheduleMode());
        assertNull(OCCURRED.getUnscheduleMode());
        assertNull(CANCELED.getUnscheduleMode());
        assertNull(NOT_APPLICABLE.getUnscheduleMode());
    }
    
    public void testIsOutstanding() throws Exception {
        assertTrue(SCHEDULED.isOutstanding());
        assertTrue(CONDITIONAL.isOutstanding());
        assertFalse(OCCURRED.isOutstanding());
        assertFalse(CANCELED.isOutstanding());
        assertFalse(NOT_APPLICABLE.isOutstanding());
    }
}
