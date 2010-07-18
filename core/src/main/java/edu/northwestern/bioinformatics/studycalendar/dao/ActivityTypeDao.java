package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;

import java.util.List;

/**
 * @author Nataliya Shurupova
 */
public class ActivityTypeDao extends StudyCalendarMutableDomainObjectDao<ActivityType> implements DeletableDomainObjectDao<ActivityType> {
    @Override
    public Class<ActivityType> domainClass() {
        return ActivityType.class;
    }

   /**
    * Returns a list of all the activity types currently available.
    *
    * @return      list of all the Activity types currently available
    */
    @Override
    @SuppressWarnings({ "unchecked" })
    public List<ActivityType> getAll() {
        return getHibernateTemplate().find("from ActivityType order by name");
    }

    /**
    * Finds the activity type by activity type.
    *
    * @param  name the name of the activity type we want to find
    * @return      the activity type found that corresponds to the type parameter
    */
    @SuppressWarnings({ "unchecked" })
    public ActivityType getByName(String name) {
        List<ActivityType> activityTypes = getHibernateTemplate().find("from ActivityType where name = ?", name);
        if (!activityTypes.isEmpty()) {
            return activityTypes.get(0);
        }
        return null;
    }

    /**
    * Deletes an activity type
    *
    * @param  activityType the activityType to delete
    */
    public void delete(ActivityType activityType) {
        getHibernateTemplate().delete(activityType);
    }

    public void deleteAll(List<ActivityType> t) {
        getHibernateTemplate().deleteAll(t);
    }
}

