package edu.northwestern.bioinformatics.studycalendar.dao;

import org.springframework.transaction.annotation.Transactional;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;

/**
 * @author Rhett Sutphin
 */
@Transactional(readOnly = true)
public class PeriodDao extends StudyCalendarMutableDomainObjectDao<Period> {
    @Override
    public Class<Period> domainClass() {
        return Period.class;
    }

    public void evict(Period period) {
        getHibernateTemplate().evict(period);
    }
}
