package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
@Transactional(readOnly=true)
public class PlannedActivityDao extends StudyCalendarMutableDomainObjectDao<PlannedActivity> implements DeletableDomainObjectDao<PlannedActivity> {
    @Override
    public Class<PlannedActivity> domainClass() {
        return PlannedActivity.class;
    }

    /**
    * Deletes a planned activity
    *
    * @param  event the planned activity to delete
    */
    @Transactional(readOnly=false)
    public void delete(PlannedActivity event) {
        getHibernateTemplate().delete(event);
    }

    /**
    * Finds all planned activities for a activity id
    *
    * @param  activityId the activity id to search with
    * @return      a list of planned activities with the activity id passed in
    */
    public List<PlannedActivity> getPlannedActivitiesForAcivity(Integer activityId) {
        return (List<PlannedActivity>) getHibernateTemplate().find("select pa from PlannedActivity as pa where pa.activity.id=?",activityId);
    }
}
