package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;

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

    public void save(Site site) {
        getHibernateTemplate().saveOrUpdate(site);
    }

    public List<Site> getAll() {
        return getHibernateTemplate().find("from Site");
    }
}
