package edu.northwestern.bioinformatics.studycalendar.domain;

import static edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode.*;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Canceled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Conditional;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Missed;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.NotApplicable;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Occurred;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.*;
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

    public void testGetByName() throws Exception {
        assertSame(SCHEDULED, getByName("scheduled"));
        assertSame(CONDITIONAL, getByName("conditional"));
        assertSame(OCCURRED, getByName("occurred"));
        assertSame(CANCELED, getByName("canceled"));
        assertSame(NOT_APPLICABLE, getByName("NA"));
        assertSame(MISSED, getByName("missed"));
    }

    public void testGetByNameIsCaseInsensitive() throws Exception {
        assertSame(SCHEDULED, getByName("Scheduled"));
        assertSame(NOT_APPLICABLE, getByName("na"));
    }

    public void testCreateStateInstance() throws Exception {
        assertCreatedInstance(SCHEDULED, Scheduled.class);
        assertCreatedInstance(CONDITIONAL, Conditional.class);
        assertCreatedInstance(NOT_APPLICABLE, NotApplicable.class);
        assertCreatedInstance(OCCURRED, Occurred.class);
        assertCreatedInstance(CANCELED, Canceled.class);
        assertCreatedInstance(MISSED, Missed.class);
    }

    private void assertCreatedInstance(ScheduledActivityMode<?> mode, Class<?> expectedClass) {
        assertTrue("Wrong class created for " + mode,
            expectedClass.isAssignableFrom(mode.createStateInstance().getClass()));
    }

    public void testCreateStateWithDateAndReason() throws Exception {
        ScheduledActivityState created =
            CONDITIONAL.createStateInstance(DateTools.createDate(2009, Calendar.APRIL, 6), "Fancy");
        assertTrue("Wrong type", created instanceof Conditional);
        assertDayOfDate("Wrong date", 2009, Calendar.APRIL, 6, created.getDate());
        assertEquals("Wrong reason", "Fancy", created.getReason());
    }

    public void testCreateStateWithDatePartsAndReason() throws Exception {
        ScheduledActivityState created =
            NOT_APPLICABLE.createStateInstance(2009, Calendar.APRIL, 6, "Fancier");
        assertTrue("Wrong type", created instanceof NotApplicable);
    }
}
