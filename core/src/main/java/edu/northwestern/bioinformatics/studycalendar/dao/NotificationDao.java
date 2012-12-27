/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Notification;

import java.util.List;
/**
 * @author Saruabh Agrawal
 */
public class NotificationDao extends StudyCalendarMutableDomainObjectDao<Notification> implements DeletableDomainObjectDao<Notification> {
    @Override
    public Class<Notification> domainClass() {
        return Notification.class;
    }

    public void save(final Notification notification) {

    }

    /**
    * Deletes notification
    *
    * @param  notification the activity to delete
    */
    public void delete(Notification notification) {
        getHibernateTemplate().delete(notification);
    }

    public void deleteAll(List<Notification> t) {
        getHibernateTemplate().deleteAll(t);
    }
}
