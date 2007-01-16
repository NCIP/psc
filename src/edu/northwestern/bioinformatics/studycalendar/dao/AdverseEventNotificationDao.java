package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.AdverseEventNotification;

/**
 * @author Rhett Sutphin
 */
public class AdverseEventNotificationDao extends StudyCalendarDao<AdverseEventNotification> {
    public Class<AdverseEventNotification> domainClass() {
        return AdverseEventNotification.class;
    }

    public void save(AdverseEventNotification notification) {
        getHibernateTemplate().saveOrUpdate(notification);
    }
}
