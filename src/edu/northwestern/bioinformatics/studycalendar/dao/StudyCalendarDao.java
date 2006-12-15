package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.DomainObject;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @author Rhett Sutphin
 */
public abstract class StudyCalendarDao<T extends DomainObject> extends HibernateDaoSupport {
    public T getById(int id) {
        return (T) getHibernateTemplate().get(domainClass(), id);
    }
    
    public abstract Class<T> domainClass();
}
