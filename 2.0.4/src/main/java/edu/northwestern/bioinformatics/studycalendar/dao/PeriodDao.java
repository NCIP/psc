package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Rhett Sutphin
 */
@Transactional(readOnly = true)
public class PeriodDao extends StudyCalendarMutableDomainObjectDao<Period> implements DeletableDomainObjectDao<Period> {
    @Override
    public Class<Period> domainClass() {
        return Period.class;
    }

    /**
    * To manage caching of a period, this method will remove the period specified from hibernate's first level cache
    *
    * @param  period the period to remove from the cache
    */
    public void evict(Period period) {
        getHibernateTemplate().evict(period);
    }

    /**
    * Deletes a period
    *
    * @param  period the period to delete
    */
    public void delete(Period period) {
        getHibernateTemplate().delete(period);
    }
}
