package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.BlackoutDate;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

/**
 * @author Nataliya Shurupova
 */

@Transactional(readOnly = true)
public class BlackoutDateDao extends StudyCalendarMutableDomainObjectDao<BlackoutDate>
    implements Serializable, DeletableDomainObjectDao<BlackoutDate> {

    @Override
    public Class<BlackoutDate> domainClass() {
        return BlackoutDate.class;
    }

    public void delete(final BlackoutDate site) {
        getHibernateTemplate().delete(site);
    }

    public void deleteAll(List<BlackoutDate> t) {
        getHibernateTemplate().deleteAll(t);
    }
}

