package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import static java.util.Collections.EMPTY_LIST;

import static org.apache.commons.lang.StringUtils.EMPTY;

/**
 * @author Jaron Sampson
 */
public class ActivityDao extends StudyCalendarMutableDomainObjectDao<Activity> {
    @Override
    public Class<Activity> domainClass() {
        return Activity.class;
    }

    public List<Activity> getAll() {
        List<Activity> sortedList;
        sortedList = getHibernateTemplate().find("from Activity");
        Collections.sort(sortedList);
        return sortedList;
    }

    public Activity getByName(String name) {
//        return (Activity) getHibernateTemplate().find("from Activity where name = ?", name).get(0);
        List<Activity> activities = getHibernateTemplate().find("from Activity where name = ?", name);
        if (activities.size() == 0) {
            return null;
        }
        return activities.get(0);
    }

    public Activity getByCodeAndSourceName(String code, String sourceName) {
        List<Activity> activities = getHibernateTemplate().find("from Activity a where code = ? and a.source.name = ?", new String[] {code, sourceName});
        if (activities.size() == 0) {
            return null;
        }
        return activities.get(0);
    }

    public List<Activity> getActivitiesBySearchText(String searchText) {
        String search = "%" + searchText.toLowerCase() +"%";
        List<Activity> activities = getHibernateTemplate().find("from Activity where lower(name || code) LIKE ?", search);
        return activities;
    }


    public void delete(Activity activity) {
        getHibernateTemplate().delete(activity);
    }


}
