package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;
import static edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools.createExternalObjectId;
import static edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools.loadFromExternalObjectId;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import static java.util.Arrays.asList;

/**
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
@Transactional
public class SiteService {
	public static final String BASE_SITE_PG = "BaseSitePG";
	public static final String SITE_COORDINATOR_ACCESS_ROLE = Role.STUDY_COORDINATOR.csmRole();
	public static final String PARTICIPANT_COORDINATOR_ACCESS_ROLE = Role.PARTICIPANT_COORDINATOR.csmRole();
    public static final String RESEARCH_ASSOCIATE_ACCESS_ROLE = Role.RESEARCH_ASSOCIATE.csmRole();
    public static final String SITE_COORDINATOR_GROUP = Role.SITE_COORDINATOR.csmGroup();
	public static final String PARTICIPANT_COORDINATOR_GROUP = Role.PARTICIPANT_COORDINATOR.csmGroup();
    public static final String ASSIGNED_USERS = "ASSIGNED_USERS";
    public static final String AVAILABLE_USERS = "AVAILABLE_USERS";
	
    private SiteDao siteDao;
    private StudySiteDao studySiteDao;
    private StudyCalendarAuthorizationManager authorizationManager;
    private UserDao userDao;


    public Site createSite(Site site) throws Exception {
        siteDao.save(site);
        saveSiteProtectionGroup(createExternalObjectId(site));
        return site;
    }
    
    protected void saveSiteProtectionGroup(String siteName) throws Exception {
    	authorizationManager.createProtectionGroup(siteName, BASE_SITE_PG);
    }

    public void assignProtectionGroup(Site site, User user, Role role) throws Exception {
        ProtectionGroup sitePG = authorizationManager.getPGByName(createExternalObjectId(site));
    	authorizationManager.assignProtectionGroupsToUsers(user.getCsmUserId().toString(), sitePG, role.csmRole());
    }

    public void removeProtectionGroup(Site site, User user) throws Exception {
        ProtectionGroup sitePG = authorizationManager.getPGByName(createExternalObjectId(site));
    	authorizationManager.removeProtectionGroupUsers(asList(user.getCsmUserId().toString()), sitePG);
    }

    public void assignSiteCoordinatorsInCsm(Site site, List<String> userIds) throws Exception {
        assignProtectionGroup(site, userIds, SITE_COORDINATOR_ACCESS_ROLE);
    }
    
    public void assignParticipantCoordinatorsInCsm(Site site, List<String> userIds) throws Exception {
    	assignProtectionGroup(site, userIds, PARTICIPANT_COORDINATOR_ACCESS_ROLE);
    }

    public void assignSiteResearchAssociatesInCsm(Site site, List<String> userIds) throws Exception {
        assignProtectionGroup(site, userIds, RESEARCH_ASSOCIATE_ACCESS_ROLE);
    }

    private void assignProtectionGroup(Site site, List<String> userIds, String accessRole) throws Exception {
        ProtectionGroup sitePG = authorizationManager.getPGByName(createExternalObjectId(site));
    	authorizationManager.assignProtectionGroupsToUsers(userIds, sitePG, accessRole);
    }

    public void assignProtectionGroup(Site site, String userId, String[] accessRoles) throws Exception {
        ProtectionGroup sitePG = authorizationManager.getPGByName(createExternalObjectId(site));
    	authorizationManager.assignProtectionGroupsToUsers(Collections.singletonList(userId), sitePG, accessRoles);
    }

    public void assignProtectionGroup(Site site, List<String> userIds, String[] accessRoles) throws Exception {
        ProtectionGroup sitePG = authorizationManager.getPGByName(createExternalObjectId(site));
    	authorizationManager.assignProtectionGroupsToUsers(userIds, sitePG, accessRoles);
    }

    public void removeProtectionGroup(Site site, List<String> userIds) throws Exception {
        ProtectionGroup sitePG = authorizationManager.getPGByName(createExternalObjectId(site));
    	authorizationManager.removeProtectionGroupUsers(userIds, sitePG);
    }

    public void removeSiteCoordinators(Site site, List<String> userIds) throws Exception {
        removeProtectionGroup(site, userIds);
    }
    
    public void removeParticipantCoordinators(Site site, List<String> userIds) throws Exception {
    	removeProtectionGroup(site, userIds);
    }

    public void removeResearchAssociates(Site site, List<String> userIds) throws Exception{
        removeProtectionGroup(site, userIds);
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

    @Required
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }
}
