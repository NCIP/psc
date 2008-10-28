package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.nwu.bioinformatics.commons.CollectionUtils;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nshurupova
 * Date: Oct 15, 2008
 * Time: 2:19:32 PM
 * To change this template use File | Settings | File Templates.
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
        log.info("===== getByName " + name);
        List<ActivityType> activityTypes = getHibernateTemplate().find("from ActivityType where name = ?", name);
        log.info("===== activitiyTypes " + activityTypes);
        if (!activityTypes.isEmpty()) {
            log.info("===== activityTypes is not empty");
            return activityTypes.get(0);
        }
        log.info("===== before returning null ");
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

