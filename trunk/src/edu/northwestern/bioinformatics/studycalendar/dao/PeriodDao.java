package edu.northwestern.bioinformatics.studycalendar.dao;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;

/**
 * @author Rhett Sutphin
 */
public class PeriodDao extends HibernateDaoSupport {
    public Period getById(int id) {
        return (Period) getHibernateTemplate().get(Period.class, id);
    }

    public void save(Period period) {
        getHibernateTemplate().saveOrUpdate(period);
    }
}
