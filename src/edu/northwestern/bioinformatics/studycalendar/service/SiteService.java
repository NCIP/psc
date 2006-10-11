package edu.northwestern.bioinformatics.studycalendar.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;

/**
 * @author Padmaja Vedula
 */

@Transactional
public class SiteService {
	public static final String BASE_SITE_PG = "BaseSitePG";
	public static final String SITE_COORDINATOR_ACCESS_ROLE = "SITE_COORDINATOR";
    private SiteDao siteDao;
    private StudyCalendarAuthorizationManager authorizationManager;

    public Site createSite(Site site) {
        siteDao.save(site);
        try {
        	saveSiteProtectionGroup(site.getName());
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return site;
    }
    
    public void saveSiteProtectionGroup(String siteName) throws Exception {
    	authorizationManager.createProtectionGroup(siteName, BASE_SITE_PG);
    }
    
    public void assignSiteCoordinators(ProtectionGroup site, List<String> userIds) throws Exception {
    	authorizationManager.assignProtectionGroupsToUsers(userIds, site, SITE_COORDINATOR_ACCESS_ROLE);
    }

      ////// CONFIGURATION

    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }
    
     public void setStudyCalendarAuthorizationManager(StudyCalendarAuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }
}
