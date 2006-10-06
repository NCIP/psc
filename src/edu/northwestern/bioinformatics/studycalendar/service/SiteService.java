package edu.northwestern.bioinformatics.studycalendar.service;

import org.springframework.transaction.annotation.Transactional;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;

/**
 * @author Padmaja Vedula
 */

@Transactional
public class SiteService {
	public static final String BASE_SITE_PG = "BaseSitePG";
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

      ////// CONFIGURATION

    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }
    
     public void setStudyCalendarAuthorizationManager(StudyCalendarAuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }
}
