package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.NotificationDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Notification;

/**
 * @author Rhett Sutphin
 */
public class DismissAeCommand {
    private Notification notification;
    private NotificationDao notificationDao;

    public DismissAeCommand(NotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }

    ////// LOGIC

    public void dismiss() {
        notification.setDismissed(true);
        notificationDao.save(notification);
    }

    ////// BOUND PROPERTIES

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }
}
