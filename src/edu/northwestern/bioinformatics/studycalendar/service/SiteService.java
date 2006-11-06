package edu.northwestern.bioinformatics.studycalendar.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;

/**
 * @author Padmaja Vedula
 *
 */

@Transactional
public class SiteService {
	public static final String BASE_SITE_PG = "BaseSitePG";
	public static final String SITE_COORDINATOR_ACCESS_ROLE = "SITE_COORDINATOR";
	public static final String PARTICIPANT_COORDINATOR_ACCESS_ROLE = "PARTICIPANT_COORDINATOR";
	public static final String SITE_COORDINATOR_GROUP = "SITE_COORDINATOR";
	public static final String PARTICIPANT_COORDINATOR_GROUP = "PARTICIPANT_COORDINATOR";
    public static final String ASSIGNED_USERS = "ASSIGNED_USERS";
    public static final String AVAILABLE_USERS = "AVAILABLE_USERS";
	
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
    
    public void assignSiteCoordinators(Site site, List<String> userIds) throws Exception {
    	ProtectionGroup sitePG = authorizationManager.getPGByName(site.getName());
    	authorizationManager.assignProtectionGroupsToUsers(userIds, sitePG, SITE_COORDINATOR_ACCESS_ROLE);
    }
    
    public void assignParticipantCoordinators(Site site, List<String> userIds) throws Exception {
    	ProtectionGroup sitePG = authorizationManager.getPGByName(site.getName());
    	authorizationManager.assignProtectionGroupsToUsers(userIds, sitePG, PARTICIPANT_COORDINATOR_ACCESS_ROLE);
    }
    
    public void removeSiteCoordinators(Site site, List<String> userIds) throws Exception {
    	ProtectionGroup sitePG = authorizationManager.getPGByName(site.getName());
    	authorizationManager.removeProtectionGroupUsers(userIds, sitePG);
    }
    
    public void removeParticipantCoordinators(Site site, List<String> userIds) throws Exception {
    	ProtectionGroup sitePG = authorizationManager.getPGByName(site.getName());
    	authorizationManager.removeProtectionGroupUsers(userIds, sitePG);
    }
    
    public Map getSiteCoordinatorLists(Site site) throws Exception {
    	return authorizationManager.getUserPGLists(SITE_COORDINATOR_GROUP, site.getName());
    }
    
    public Map getParticipantCoordinatorLists(Site site) throws Exception {
    	return authorizationManager.getUserPGLists(PARTICIPANT_COORDINATOR_GROUP, site.getName());
    }
    
    public ProtectionGroup getSiteProtectionGroup(String siteName) throws Exception {
    	return authorizationManager.getPGByName(siteName);
    }
    
    public List getAllSiteProtectionGroups() throws Exception {
    	return authorizationManager.getSites();
    }
    
    public List getSitesForSiteCd(String userName) throws Exception {
    	List<ProtectionGroup> sitePGs = authorizationManager.getSitePGsForUser(userName);
    	List<Site> sites = new ArrayList<Site>();
    	for (ProtectionGroup sitePG : sitePGs) {
    		sites.add(siteDao.getByName(sitePG.getProtectionGroupName()));
    	}
    	return sites;
    }
    
      ////// CONFIGURATION

    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }
    
     public void setStudyCalendarAuthorizationManager(StudyCalendarAuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }
}
