package edu.northwestern.bioinformatics.studycalendar.service;

import java.util.List;
import java.util.Map;

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
	public static final String PARTICIPANT_COORDINATOR_ACCESS_ROLE = "PARTICIPANT_COORDINATOR";
	public static final String SITE_COORDINATOR_GROUP = "SITE_COORDINATOR";
	public static final String PARTICIPANT_COORDINATOR_GROUP = "PARTICIPANT_COORDINATOR";
    private SiteDao siteDao;
    private StudyCalendarAuthorizationManager authorizationManager;

    public Site createSite(Site site) throws Exception {
        siteDao.save(site);
        saveSiteProtectionGroup(site.getName());
        return site;
    }
    
    public void saveSiteProtectionGroup(String siteName) throws Exception {
    	authorizationManager.createProtectionGroup(siteName, BASE_SITE_PG);
    }
    
    public void assignSiteCoordinators(ProtectionGroup site, List<String> userIds) throws Exception {
    	authorizationManager.assignProtectionGroupsToUsers(userIds, site, SITE_COORDINATOR_ACCESS_ROLE);
    }
    
    public void assignParticipantCoordinators(ProtectionGroup site, List<String> userIds) throws Exception {
    	authorizationManager.assignProtectionGroupsToUsers(userIds, site, PARTICIPANT_COORDINATOR_ACCESS_ROLE);
    }
    
    public void removeSiteCoordinators(ProtectionGroup site, List<String> userIds) throws Exception {
    	authorizationManager.removeProtectionGroupUsers(userIds, site);
    }
    
    public void removeParticipantCoordinators(ProtectionGroup site, List<String> userIds) throws Exception {
    	authorizationManager.removeProtectionGroupUsers(userIds, site);
    }
    
    public Map getSiteCoordinatorLists(String siteProtectionGroupName) throws Exception {
    	return authorizationManager.getUserPGLists(SITE_COORDINATOR_GROUP, siteProtectionGroupName);
    }
    
    public Map getParticipantCoordinatorLists(String siteProtectionGroupName) throws Exception {
    	return authorizationManager.getUserPGLists(PARTICIPANT_COORDINATOR_GROUP, siteProtectionGroupName);
    }
    
    public ProtectionGroup getSiteProtectionGroup(String siteName) throws Exception {
    	return authorizationManager.getSite(siteName);
    }
    
    public List getAllSiteProtectionGroups() throws Exception {
    	return authorizationManager.getSites();
    }

      ////// CONFIGURATION

    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }
    
     public void setStudyCalendarAuthorizationManager(StudyCalendarAuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }
}
