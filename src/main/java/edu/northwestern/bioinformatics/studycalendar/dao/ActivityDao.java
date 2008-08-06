package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;

import java.util.Collections;
import java.util.List;

/**
 * @author Jaron Sampson
 */
public class ActivityDao extends StudyCalendarMutableDomainObjectDao<Activity> implements DeletableDomainObjectDao<Activity> {
    @Override
    public Class<Activity> domainClass() {
        return Activity.class;
    }

   /**
    * Returns a list of all the activities currently available.
    *
    * @return      list of all the Activities currently available
    */
    public List<Activity> getAll() {
        List<Activity> sortedList;
        sortedList = getHibernateTemplate().find("from Activity");
        Collections.sort(sortedList);
        return sortedList;
    }

    /**
    * Finds the activity by activity name.
    *
    * @param  name the name of the activity we want to find
    * @return      the activity found that corresponds to the name parameter
    */
    public Activity getByName(String name) {
        List<Activity> activities = getHibernateTemplate().find("from Activity where name = ?", name);
        if (activities.size() == 0) {
            return null;
        }
        return activities.get(0);
    }

    /**
    * Finds the activity by activity code and source name.
    *
    * @param  code the code of the activity we want to find
    * @param  sourceName the source name for the activity we want to find
    * @return      the activity found that corresponds to the activity code and source name parameters
    */
    public Activity getByCodeAndSourceName(String code, String sourceName) {
        List<Activity> activities = getHibernateTemplate().find("from Activity a where code = ? and a.source.name = ?", new String[] {code, sourceName});
        if (activities.size() == 0) {
            return null;
        }
        return activities.get(0);
    }


    /**
    * Finds the activities by source id.
    *
    * @param  sourceId the source id for the activity we want to find
    * @return      the activity found that corresponds to the source id parameters
    */
    public List<Activity> getBySourceId(Integer sourceId) {
        List<Activity> activities = getHibernateTemplate().find("from Activity where source_id = ?", sourceId);
        return activities;
    }

    /**
    * Finds the activity doing a LIKE search with some search text for activity name or activity code.
    *
    * @param  searchText the text we are searching with
    * @return      a list of activities found based on the search text
    */
    public List<Activity> getActivitiesBySearchText(String searchText) {
        String search = "%" + searchText.toLowerCase() +"%";
        List<Activity> activities = getHibernateTemplate().find("from Activity where lower(name || code) LIKE ?", search);
        return activities;
    }

    /**
    * Deletes an activity
    *
    * @param  activity the activity to delete
    */
    public void delete(Activity activity) {
        getHibernateTemplate().delete(activity);
    }
}
