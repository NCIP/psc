package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.AdverseEventNotification;

/**
 * @author Rhett Sutphin
 */
public class AdverseEventNotificationDao extends StudyCalendarMutableDomainObjectDao<AdverseEventNotification> {
    @Override
    public Class<AdverseEventNotification> domainClass() {
        return AdverseEventNotification.class;
    }
}
