package edu.northwestern.bioinformatics.studycalendar.dao;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

/**
 * @author Jaron Sampson
 */
public class ActivityDao extends HibernateDaoSupport {
    public Activity getById(int id) {
        return (Activity) getHibernateTemplate().get(Activity.class, id);
    }

    public List<Activity> getAll() {
    	List<Activity> sortedList = new LinkedList<Activity>();
    	sortedList = getHibernateTemplate().find("from Activity");
    	Collections.sort(sortedList);
        return sortedList;    
    }

    public void save(Activity activity) {
        getHibernateTemplate().saveOrUpdate(activity);
    }
}
