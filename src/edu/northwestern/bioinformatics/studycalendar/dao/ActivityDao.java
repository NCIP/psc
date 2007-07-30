package edu.northwestern.bioinformatics.studycalendar.dao;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

/**
 * @author Jaron Sampson
 */
public class ActivityDao extends StudyCalendarDao<Activity> {
    public Class<Activity> domainClass() {
        return Activity.class;
    }

    public List<Activity> getAll() {
        List<Activity> sortedList;
        sortedList = getHibernateTemplate().find("from Activity");
        Collections.sort(sortedList);
        return sortedList;
    }

    public void save(Activity activity) {
        getHibernateTemplate().saveOrUpdate(activity);
    }

    public Activity getByName(String name) {
        return (Activity) getHibernateTemplate().find("from Activity where name = ?", name).get(0);
    }
}
