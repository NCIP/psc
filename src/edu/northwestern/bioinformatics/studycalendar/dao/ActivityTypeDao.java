package edu.northwestern.bioinformatics.studycalendar.dao;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;

import java.util.List;

/**
 * @author Jaron Sampson
 */
public class ActivityTypeDao extends HibernateDaoSupport {
    public ActivityType getById(int id) {
        return (ActivityType) getHibernateTemplate().get(ActivityType.class, id);
    }

/* We don't need this yet.

    public void save(ActivityType type) {
        getHibernateTemplate().saveOrUpdate(type);
    }
*/
    
    public List<ActivityType> getAll() {
        return getHibernateTemplate().find("from ActivityType");
    }
}
