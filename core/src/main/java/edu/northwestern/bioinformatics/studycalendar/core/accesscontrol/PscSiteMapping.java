package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import gov.nih.nci.cabig.ctms.suite.authorization.SiteMapping;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class PscSiteMapping implements SiteMapping<Site> {
    private SiteDao siteDao;

    public String getSharedIdentity(Site site) {
        return site.getAssignedIdentifier();
    }

    public List<Site> getApplicationInstances(List<String> identifiers) {
        return siteDao.getByAssignedIdentifiers(identifiers);
    }

    public boolean isInstance(Object o) {
        return o instanceof Site;
    }

    ////// CONFIGURATION

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }
}
