package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.BlackoutDate;
import edu.nwu.bioinformatics.commons.CollectionUtils;

import java.io.Serializable;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

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
}

