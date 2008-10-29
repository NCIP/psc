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
//        return CollectionUtils.firstElement(
//            (List<ActivityType>) getHibernateTemplate().find("from ActivityType where name = ?", name));
//    }
        log.debug("===== getByName {}", name);
        List<ActivityType> activityTypes = getHibernateTemplate().find("from ActivityType where name = ?", name);
        log.debug("===== activitiyTypes " + activityTypes);
        if (!activityTypes.isEmpty()) {
            log.debug("===== activityTypes is not empty");
            return activityTypes.get(0);
        }
        log.debug("===== before returning null ");
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

}

