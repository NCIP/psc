package edu.northwestern.bioinformatics.studycalendar.dao;

import org.springframework.transaction.annotation.Transactional;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;

/**
 * @author Rhett Sutphin
 */
@Transactional (readOnly=true)
public class PlannedEventDao extends StudyCalendarMutableDomainObjectDao<PlannedEvent> {
    @Override
    public Class<PlannedEvent> domainClass() {
        return PlannedEvent.class;
    }

    @Transactional(readOnly=false)
    public void delete(PlannedEvent event) {
        getHibernateTemplate().delete(event);
    }
}
