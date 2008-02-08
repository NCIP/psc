package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
@Transactional (readOnly=true)
public class PlannedActivityDao extends StudyCalendarMutableDomainObjectDao<PlannedActivity> {
    @Override
    public Class<PlannedActivity> domainClass() {
        return PlannedActivity.class;
    }

    @Transactional(readOnly=false)
    public void delete(PlannedActivity event) {
        getHibernateTemplate().delete(event);
    }

    public List<PlannedActivity> getPlannedActivitiesForAcivity(Integer activityId) {

        return (List<PlannedActivity>) getHibernateTemplate().find("select pa from PlannedActivity as pa where pa.activity.id=?",activityId);
    }

   
}
