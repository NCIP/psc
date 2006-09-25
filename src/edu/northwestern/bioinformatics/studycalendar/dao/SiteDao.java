package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;

import java.util.HashSet;
import java.util.List;

import java.util.Set;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;


/**
 * @author
 */
public class SiteDao extends HibernateDaoSupport {
    public Site getById(int id) {
        return (Site) getHibernateTemplate().get(Site.class, new Integer(id));
    }

    public Site getDefaultSite() {
        List<Site> results = getHibernateTemplate().find("from Site where name=?", Site.DEFAULT_SITE_NAME);
        if (results.size() == 0) {
            throw new StudyCalendarError("No default site in database (should have a site named '" + Site.DEFAULT_SITE_NAME + "')");
        }
        return results.get(0);
    }

    public void save(Site site) {
        getHibernateTemplate().saveOrUpdate(site);
    }

    public List<Site> getAll() {
        return getHibernateTemplate().find("from Site");
    }
}
