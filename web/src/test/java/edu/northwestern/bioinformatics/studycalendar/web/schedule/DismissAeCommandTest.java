package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.NotificationDao;
import edu.northwestern.bioinformatics.studycalendar.domain.AdverseEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.Notification;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.nwu.bioinformatics.commons.DateUtils;

import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class DismissAeCommandTest extends StudyCalendarTestCase {
    private DismissAeCommand command;
    private NotificationDao notificationDao;
    private Notification notification;

    protected void setUp() throws Exception {
        super.setUp();
        notificationDao = registerDaoMockFor(NotificationDao.class);
        command = new DismissAeCommand(notificationDao);

        AdverseEvent ae = new AdverseEvent();
        ae.setDescription("How many?");
        ae.setDetectionDate(DateUtils.createDate(2005, Calendar.MAY, 1));

        notification = new Notification(ae);
        notification.setDismissed(false);
        command.setNotification(notification);
    }

    public void testDismiss() throws Exception {
        notificationDao.save(notification);
        replayMocks();
        command.dismiss();
        verifyMocks();
        assertTrue(notification.isDismissed());
    }
}
