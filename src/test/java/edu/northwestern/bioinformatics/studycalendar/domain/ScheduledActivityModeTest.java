package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import static edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode.*;
import edu.nwu.bioinformatics.commons.DateUtils;

import java.util.Calendar;
import java.util.List;

public class ScheduledActivityModeTest extends StudyCalendarTestCase {
    public void testScheduledGetAvailableModesPos() throws Exception {
        Scheduled scheduledState = new Scheduled("Scheduled", DateUtils.createDate(2007, Calendar.SEPTEMBER, 1));
        List<ScheduledActivityMode> modes = getAvailableModes(scheduledState, true);
        assertEquals("Wrong size of available modes", 6, modes.size());
    }

    public void testScheduledGetAvailableModesNeg() throws Exception {
        Scheduled scheduledState = new Scheduled("Scheduled", DateUtils.createDate(2007, Calendar.SEPTEMBER, 1));
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
