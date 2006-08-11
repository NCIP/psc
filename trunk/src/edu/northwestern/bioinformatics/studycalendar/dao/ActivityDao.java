package edu.northwestern.bioinformatics.studycalendar.dao;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;

/**
 * @author Jaron Sampson
 */
public class ActivityDao extends HibernateDaoSupport {
    public Activity getById(int id) {
        return (Activity) getHibernateTemplate().get(Activity.class, id);
    }

    public void save(Activity activity) {
        getHibernateTemplate().saveOrUpdate(activity);
    }
}
