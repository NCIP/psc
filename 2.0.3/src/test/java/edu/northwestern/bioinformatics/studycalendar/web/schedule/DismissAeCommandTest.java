package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.AdverseEventNotificationDao;
import edu.northwestern.bioinformatics.studycalendar.domain.AdverseEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.AdverseEventNotification;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.nwu.bioinformatics.commons.DateUtils;

import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class DismissAeCommandTest extends StudyCalendarTestCase {
    private DismissAeCommand command;
    private AdverseEventNotificationDao notificationDao;
    private AdverseEventNotification notification = new AdverseEventNotification();

    protected void setUp() throws Exception {
        super.setUp();
        notificationDao = registerDaoMockFor(AdverseEventNotificationDao.class);
        command = new DismissAeCommand(notificationDao);

        notification.setDismissed(false);
        AdverseEvent ae = new AdverseEvent();
        ae.setDescription("How many?");
        ae.setDetectionDate(DateUtils.createDate(2005, Calendar.MAY, 1));
        notification.setAdverseEvent(ae);
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
