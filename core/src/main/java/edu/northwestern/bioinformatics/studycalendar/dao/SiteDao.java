package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.nwu.bioinformatics.commons.CollectionUtils;
import gov.nih.nci.cabig.ctms.tools.hibernate.MoreRestrictions;
import org.hibernate.criterion.DetachedCriteria;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
@Transactional(readOnly = true)
public class SiteDao extends StudyCalendarMutableDomainObjectDao<Site>
    implements Serializable, DeletableDomainObjectDao<Site> {

    @Override
    public Class<Site> domainClass() {
        return Site.class;
    }

    /**
     * Finds all the sites available
     *
     * @return      a list of all the available sites
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public List<Site> getAll() {
        return getHibernateTemplate().find("from Site order by name asc");
    }

    /**
     * Finds the site with the given name
     *
     * @param  name the name of the site to find
     * @return      a site that corresponds to the name given
     */
    public Site getByName(final String name) {
        return CollectionUtils.<Site>firstElement(getHibernateTemplate().find("from Site where name= ?", name));
    }

    /**
     * Finds a site by it's assigned identifier
     *
     * @param  assignedIdentifier the assigned identifier for the site we want to search for
     * @return      a site that corresponds to the given assigned identifier
     */
    public Site getByAssignedIdentifier(final String assignedIdentifier) {
        List<Site> results = getHibernateTemplate().find("from Site where assignedIdentifier= ? or (assignedIdentifier=null and name=?)", new String[]{assignedIdentifier,assignedIdentifier});
        return CollectionUtils.firstElement(results);
    }

    @SuppressWarnings({ "unchecked" })
    public List<Site> getByAssignedIdentifiers(List<String> assignedIdentifiers) {
        List fromDatabase = getHibernateTemplate().findByCriteria(
            DetachedCriteria.forClass(Site.class).add(
                MoreRestrictions.in("assignedIdentifier", assignedIdentifiers)));

        List<Site> inOrder = new ArrayList<Site>();
        for (String assignedIdentifier : assignedIdentifiers) {
            for (Iterator<Site> foundIt = fromDatabase.iterator(); foundIt.hasNext();) {
                Site found = foundIt.next();
                if (found.getAssignedIdentifier().equals(assignedIdentifier)) {
                    inOrder.add(found);
                    foundIt.remove();
                    break;
                }
            }
        }
        return inOrder;
    }

    /**
     * Returns the number of sites available
     *
     * @return      an integer for the number of sites available
     */
    public int getCount() {
        Long count = (Long) getHibernateTemplate().find("select COUNT(s) from Site s").get(0);
        return count.intValue();
    }

    /**
     * Deletes a site
     *
     * @param  site the site to delete
     */
    public void delete(final Site site) {
        site.stopManaging(); // it's bogus that hibernate apparently can't do this itself
        getHibernateTemplate().delete(site);
    }

    public void deleteAll(List<Site> t) {
        for (Site site : t) site.stopManaging();
        getHibernateTemplate().deleteAll(t);
    }

    /**
    * Finds the Sites doing a LIKE search with some search text for sites.
    *
    * @param  searchText the text we are searching with
    * @return      a list of sites found based on the search text
    */

    public List<Site> searchSitesBySearchText(final String searchText) {
		String like = "%" + searchText.toLowerCase() + "%";
		List<Site> sites = getHibernateTemplate().find("from Site s where lower(s.name) LIKE ? ORDER BY s.name DESC ", like);
		return sites;
	}
}
