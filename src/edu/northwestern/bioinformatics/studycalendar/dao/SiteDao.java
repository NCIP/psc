package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;


/**
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
@Transactional(readOnly = true)
public class SiteDao extends StudyCalendarGridIdentifiableDao<Site> {

    public Class<Site> domainClass() {
        return Site.class;
    }

    public Site getDefaultSite() {
        List<Site> results = getHibernateTemplate().find("from Site where name=?", Site.DEFAULT_SITE_NAME);
        if (results.size() == 0) {
            throw new StudyCalendarError("No default site in database (should have a site named '" + Site.DEFAULT_SITE_NAME + "')");
        }
        return results.get(0);
    }

    public List<Site> getAll() {
        return getHibernateTemplate().find("from Site");
    }    

    @Transactional(readOnly = false)
    public void save(Site site) {
        getHibernateTemplate().saveOrUpdate(site);
    }
    
    public Site getByName(String name) {
        List<Site> results = getHibernateTemplate().find("from Site where name= ?", name);
        return results.get(0);
    }
    
}
