package edu.northwestern.bioinformatics.studycalendar.dao;

import org.springframework.transaction.annotation.Transactional;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;

/**
 * @author Rhett Sutphin
 */
@Transactional (readOnly=true)
public class PlannedEventDao extends StudyCalendarMutableDomainObjectDao<PlannedActivity> {
    @Override
    public Class<PlannedActivity> domainClass() {
        return PlannedActivity.class;
    }

    @Transactional(readOnly=false)
    public void delete(PlannedActivity event) {
        getHibernateTemplate().delete(event);
    }
}
