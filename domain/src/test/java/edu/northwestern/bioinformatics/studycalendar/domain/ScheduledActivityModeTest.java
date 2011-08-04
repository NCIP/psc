package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.lang.DateTools;
import junit.framework.TestCase;

import java.util.Calendar;

import static edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode.*;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.assertDayOfDate;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.assertTimeOfDate;

public class ScheduledActivityModeTest extends TestCase {
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
        assertCreatedInstance(SCHEDULED);
        assertCreatedInstance(CONDITIONAL);
        assertCreatedInstance(NOT_APPLICABLE);
        assertCreatedInstance(OCCURRED);
        assertCreatedInstance(CANCELED);
        assertCreatedInstance(MISSED);
    }

    private void assertCreatedInstance(ScheduledActivityMode mode) {
        assertEquals("Wrong state created for " + mode,
            mode, mode.createStateInstance().getMode());
    }

    public void testCreateStateWithDateAndReason() throws Exception {
        ScheduledActivityState created =
            CONDITIONAL.createStateInstance(DateTools.createDate(2009, Calendar.APRIL, 6), "Fancy");
        assertEquals("Wrong mode", CONDITIONAL, created.getMode());
        assertDayOfDate("Wrong date", 2009, Calendar.APRIL, 6, created.getDate());
        assertEquals("Wrong reason", "Fancy", created.getReason());
    }

    public void testCreateStateWithDatePartsAndReason() throws Exception {
        ScheduledActivityState created =
            NOT_APPLICABLE.createStateInstance(2009, Calendar.APRIL, 6, "Fancier");
        assertEquals("Wrong type", NOT_APPLICABLE, created.getMode());
    }

    public void testCreateStateWithDateAndReasonDefaultWithTimeFalse() throws Exception {
        ScheduledActivityState created =
            CONDITIONAL.createStateInstance(DateTools.createDate(2009, Calendar.APRIL, 6), "Fancy");
        assertEquals("Wrong withDate", (Object) false, created.getWithTime());
    }

    public void testCreateStateWithDateAndReasonAndTime() throws Exception {
        ScheduledActivityState created =
            CONDITIONAL.createStateInstance(DateTools.createDate(2009, Calendar.APRIL, 6, 9, 15, 0), "Fancy", true);

        assertEquals("Wrong mode", CONDITIONAL, created.getMode());
        assertDayOfDate("Wrong date", 2009, Calendar.APRIL, 6, created.getDate());
        assertTimeOfDate("Wrong time", 9, 15, 0, 0, created.getDate());
        assertEquals("Wrong reason", "Fancy", created.getReason());
        assertEquals("Wrong withDate", (Object) true, created.getWithTime());
    }
}
