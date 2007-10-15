package edu.northwestern.bioinformatics.studycalendar.service;

import static java.util.Collections.singletonList;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;
import static edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools.*;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Required;

import java.util.*;

/**
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
@Transactional
public class SiteService {
	public static final String BASE_SITE_PG = "BaseSitePG";
	public static final String SITE_COORDINATOR_ACCESS_ROLE = "SITE_COORDINATOR";
	public static final String PARTICIPANT_COORDINATOR_ACCESS_ROLE = "PARTICIPANT_COORDINATOR";
    public static final String RESEARCH_ASSOCIATE_ACCESS_ROLE = "RESEARCH_ASSOCIATE";
    public static final String SITE_COORDINATOR_GROUP = "SITE_COORDINATOR";
	public static final String PARTICIPANT_COORDINATOR_GROUP = "PARTICIPANT_COORDINATOR";
    public static final String ASSIGNED_USERS = "ASSIGNED_USERS";
    public static final String AVAILABLE_USERS = "AVAILABLE_USERS";
	
    private SiteDao siteDao;
    private StudySiteDao studySiteDao;
    private StudyCalendarAuthorizationManager authorizationManager;


    public Site createSite(Site site) throws Exception {
        siteDao.save(site);
        saveSiteProtectionGroup(createExternalObjectId(site));
        return site;
    }
    
    protected void saveSiteProtectionGroup(String siteName) throws Exception {
    	authorizationManager.createProtectionGroup(siteName, BASE_SITE_PG);
    }
    
    public void assignSiteCoordinators(Site site, List<String> userIds) throws Exception {
        assignProtectionGroup(site, userIds, SITE_COORDINATOR_ACCESS_ROLE);
    }
    
    public void assignParticipantCoordinators(Site site, List<String> userIds) throws Exception {
    	assignProtectionGroup(site, userIds, PARTICIPANT_COORDINATOR_ACCESS_ROLE);
    }

    public void assignSiteResearchAssociates(Site site, List<String> userIds) throws Exception {
        assignProtectionGroup(site, userIds, RESEARCH_ASSOCIATE_ACCESS_ROLE);
    }

    public void assignSiteCoordinators(Site site, String userId) throws Exception {
        assignSiteCoordinators(site, singletonList(userId));
    }

    public void assignParticipantCoordinators(Site site, String userId) throws Exception {
    	assignParticipantCoordinators(site, singletonList(userId));
    }

    public void assignSiteResearchAssociates(Site site, String userId) throws Exception {
        assignSiteResearchAssociates(site, singletonList(userId));
    }

    private void assignProtectionGroup(Site site, List<String> userIds, String accessRole) throws Exception {
        ProtectionGroup sitePG = authorizationManager.getPGByName(createExternalObjectId(site));
    	authorizationManager.assignProtectionGroupsToUsers(userIds, sitePG, accessRole);
    }

    public void removeAllSiteRoles(Site site, List<String> userIds) throws Exception {
        removeParticipantCoordinators(site, userIds);
        removeResearchAssociates(site, userIds);
        removeSiteCoordinators(site, userIds);
    }

    public void removeAllSiteRoles(Site site, String userId) throws Exception {
        removeAllSiteRoles(site, Collections.singletonList(userId));
    }

    
    public void removeSiteCoordinators(Site site, List<String> userIds) throws Exception {
    	ProtectionGroup sitePG = authorizationManager.getPGByName(createExternalObjectId(site));
    	authorizationManager.removeProtectionGroupUsers(userIds, sitePG);
    }
    
    public void removeParticipantCoordinators(Site site, List<String> userIds) throws Exception {
    	ProtectionGroup sitePG = authorizationManager.getPGByName(createExternalObjectId(site));
    	authorizationManager.removeProtectionGroupUsers(userIds, sitePG);
    }

    public void removeResearchAssociates(Site site, List<String> userIds) throws Exception{
        ProtectionGroup sitePG = authorizationManager.getPGByName(createExternalObjectId(site));
    	authorizationManager.removeProtectionGroupUsers(userIds, sitePG);
    }
    
    public Map getSiteCoordinatorLists(Site site) throws Exception {
    	return authorizationManager.getUserPGLists(SITE_COORDINATOR_GROUP, createExternalObjectId(site));
    }
    
    public Map getParticipantCoordinatorLists(Site site) throws Exception {
    	return authorizationManager.getUserPGLists(PARTICIPANT_COORDINATOR_GROUP, createExternalObjectId(site));
    }
    
    public List<Site> getSitesForUser(String userName) {
        Set<Site> sites = new LinkedHashSet<Site>();
        sites.addAll(getSitesForSiteCd(userName));
        sites.addAll(getSitesForParticipantCoordinator(userName));

        return new ArrayList<Site>(sites);
    }

    public List<Site> getSitesForSiteCd(String userName) {
        List<ProtectionGroup> sitePGs = authorizationManager.getSitePGsForUser(userName);
        List<Site> sites = new ArrayList<Site>(sitePGs.size());
        for (ProtectionGroup sitePG : sitePGs) {
            sites.add(DomainObjectTools.loadFromExternalObjectId(sitePG.getProtectionGroupName(),siteDao));
        }
        return sites;
    }

    public Collection<Site> getSitesForParticipantCoordinator(String userName) {
        List<ProtectionGroup> studySitePGs = authorizationManager.getStudySitePGsForUser(userName);
        Set<Site> sites = new LinkedHashSet<Site>();
        for (ProtectionGroup studySitePG : studySitePGs) {
            StudySite studySite =
                    loadFromExternalObjectId(studySitePG.getProtectionGroupName(), studySiteDao);
            sites.add(studySite.getSite());
        }
        return sites;
    }
    
    ////// CONFIGURATION

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    @Required
    public void setStudySiteDao(StudySiteDao studySiteDao) {
        this.studySiteDao = studySiteDao;
    }

    @Required
    public void setStudyCalendarAuthorizationManager(StudyCalendarAuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }
}
