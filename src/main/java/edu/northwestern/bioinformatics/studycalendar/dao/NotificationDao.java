package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Notification;

/**
 * @author Saruabh Agrawal
 */
public class NotificationDao extends StudyCalendarMutableDomainObjectDao<Notification> {
    @Override
    public Class<Notification> domainClass() {
        return Notification.class;
    }

    public void save(final Notification notification) {

    }
}
