package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;

import java.util.List;
import java.io.Serializable;

import org.springframework.transaction.annotation.Transactional;


/**
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
@Transactional(readOnly = true)
public class SiteDao extends StudyCalendarMutableDomainObjectDao<Site> implements Serializable {

    public Class<Site> domainClass() {
        return Site.class;
    }

    public List<Site> getAll() {
        return getHibernateTemplate().find("from Site");
    }    

    // TODO: this method assumes success
    public Site getByName(String name) {
        List<Site> results = getHibernateTemplate().find("from Site where name= ?", name);
        return results.get(0);
    }

    public int getCount() {
        Long count = (Long) getHibernateTemplate().find("select COUNT(s) from Site s").get(0);
        return count.intValue();
    }
}
