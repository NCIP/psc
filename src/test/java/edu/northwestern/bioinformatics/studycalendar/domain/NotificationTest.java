package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.utils.FormatTools;
import edu.nwu.bioinformatics.commons.DateUtils;
import edu.nwu.bioinformatics.commons.testing.CoreTestCase;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Saurabh Agrawal
 */
public class NotificationTest extends CoreTestCase {

    private Notification notification;

    private AdverseEvent adverseEvent;
    private Date detectionDate;

    private ScheduledActivity scheduledActivity;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        adverseEvent = new AdverseEvent();
        adverseEvent.setDescription("desc");
        detectionDate = DateUtils.createDate(2007, Calendar.SEPTEMBER, 2);
        adverseEvent.setDetectionDate(detectionDate);
        scheduledActivity = Fixtures.createScheduledActivity("sch activity", 2008, 2, 3);


    }

    public void testCrateNotificationForAdverseEvent() {

        notification = new Notification(adverseEvent);

        String expectedTitle = "Serious Adverse Event on " + FormatTools.formatDate(detectionDate);
        assertEquals(expectedTitle, notification.getTitle());
        assertTrue("action is required", notification.isActionRequired());
        assertFalse(notification.isDismissed());
        assertEquals("desc", notification.getMessage());
        assertNull(notification.getAssignment());

    }

    public void testCrateNotificationForReconsents() {
        scheduledActivity.setId(2);
        notification = new Notification(scheduledActivity);

        String expectedTitle = "Reconsent scheduled for " + FormatTools.formatDate(new Date());

        assertEquals(expectedTitle, notification.getTitle());
        assertTrue("action is required", notification.isActionRequired());
        assertFalse(notification.isDismissed());
        assertEquals("/pages/cal/scheduleActivity?event=2", notification.getMessage());
        assertNull(notification.getAssignment());

    }
}
