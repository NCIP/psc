package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.nwu.bioinformatics.commons.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Jaron Sampson
 * @author Rhett Sutphin
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
    @SuppressWarnings({ "unchecked" })
    public List<Activity> getAll() {
        List<Activity> sortedList = getHibernateTemplate().find("from Activity");
        Collections.sort(sortedList);
        return sortedList;
    }

    /**
    * Finds the activity by activity name.
    *
    * @param  name the name of the activity we want to find
    * @return      the activity found that corresponds to the name parameter
    */
    @SuppressWarnings({ "unchecked" })
    public Activity getByName(String name) {
        return CollectionUtils.firstElement(
            (List<Activity>) getHibernateTemplate().find("from Activity where name = ?", name));
    }

    /**
    * Finds the activity by activity code and source name.
    *
    * @param  code the code of the activity we want to find
    * @param  sourceName the source name for the activity we want to find
    * @return      the activity found that corresponds to the activity code and source name parameters
    */
    @SuppressWarnings({ "unchecked" })
    public Activity getByCodeAndSourceName(String code, String sourceName) {
        return CollectionUtils.firstElement(
            (List<Activity>) getHibernateTemplate().find(
                "from Activity a where code = ? and a.source.name = ?", new String[] { code, sourceName }));
    }

    public Activity getByUniqueKey(String key) {
        Map<String, String> parts = Activity.splitPropertyChangeKey(key);
        return getByCodeAndSourceName(parts.get("code"), parts.get("source"));
    }

    /**
    * Finds the activities by source id.
    *
    * @param  sourceId the source id for the activity we want to find
    * @return      the activity found that corresponds to the source id parameters
    */
    @SuppressWarnings({ "unchecked" })
    public List<Activity> getBySourceId(Integer sourceId) {
        return (List<Activity>) getHibernateTemplate().find("from Activity where source_id = ?", sourceId);
    }

    /**
    * Finds the activity doing a LIKE search with some search text for activity name or activity code.
    *
    * @param  searchText the text we are searching with
    * @return      a list of activities found based on the search text
    */
    @SuppressWarnings({ "unchecked" })
    public List<Activity> getActivitiesBySearchText(String searchText) {
        String search = "%" + searchText.toLowerCase() +"%";
        return (List<Activity>) getHibernateTemplate().find(
            "from Activity where lower(name || code) LIKE ?", search);
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
