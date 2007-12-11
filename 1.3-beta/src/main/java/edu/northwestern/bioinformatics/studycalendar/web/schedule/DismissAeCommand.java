package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.AdverseEventNotificationDao;
import edu.northwestern.bioinformatics.studycalendar.domain.AdverseEventNotification;

/**
 * @author Rhett Sutphin
 */
public class DismissAeCommand {
    private AdverseEventNotification notification;
    private AdverseEventNotificationDao notificationDao;

    public DismissAeCommand(AdverseEventNotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }

    ////// LOGIC

    public void dismiss() {
        notification.setDismissed(true);
        notificationDao.save(notification);
    }

    ////// BOUND PROPERTIES

    public AdverseEventNotification getNotification() {
        return notification;
    }

    public void setNotification(AdverseEventNotification notification) {
        this.notification = notification;
    }
}
