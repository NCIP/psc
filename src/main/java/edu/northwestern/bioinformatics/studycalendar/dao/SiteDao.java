package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.nwu.bioinformatics.commons.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

/**
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
@Transactional(readOnly = true)
public class SiteDao extends StudyCalendarMutableDomainObjectDao<Site> implements Serializable {

    @Override
    public Class<Site> domainClass() {
        return Site.class;
    }

    public List<Site> getAll() {
        return getHibernateTemplate().find("from Site");
    }

    // TODO: this method assumes success
    public Site getByName(final String name) {
        List<Site> results = getHibernateTemplate().find("from Site where name= ?", name);
        return results.get(0);
    }

    public Site getByAssignedIdentifier(final String assignedIdentifier) {
        List<Site> results = getHibernateTemplate().find("from Site where assignedIdentifier= ?", assignedIdentifier);
        return CollectionUtils.firstElement(results);
    }

    public int getCount() {
        Long count = (Long) getHibernateTemplate().find("select COUNT(s) from Site s").get(0);
        return count.intValue();
    }

    public void delete(final Site site) {
        getHibernateTemplate().delete(site);
    }
}
