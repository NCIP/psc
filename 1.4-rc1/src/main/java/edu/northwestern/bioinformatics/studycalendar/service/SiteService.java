package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
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
    private SiteDao siteDao;
    private UserDao userDao;
    private StudySiteDao studySiteDao;
    private StudyCalendarAuthorizationManager authorizationManager;

    public Site createSite(Site site) throws Exception {
        siteDao.save(site);
        saveSiteProtectionGroup(createExternalObjectId(site));
        return site;
    }
    
    protected void saveSiteProtectionGroup(String siteName) throws Exception {
    	authorizationManager.createProtectionGroup(siteName);
    }

    public void assignProtectionGroup(Site site, User user, Role role) throws Exception {
        ProtectionGroup sitePG = authorizationManager.getPGByName(createExternalObjectId(site));
    	authorizationManager.assignProtectionGroupsToUsers(user.getCsmUserId().toString(), sitePG, role.csmRole());
    }

    public void removeProtectionGroup(Site site, User user) throws Exception {
        ProtectionGroup sitePG = authorizationManager.getPGByName(createExternalObjectId(site));
    	authorizationManager.removeProtectionGroupUsers(asList(user.getCsmUserId().toString()), sitePG);
    }

    /**
     * This method is incomplete.  It should probably be replaced with calls like
     * {@link User}.getUserRole(desiredRole).getSites().
     * 
     * @param userName
     * @return
     */
    @Deprecated
    public List<Site> getSitesForUser(String userName) {
        User user = userDao.getByName(userName);
        if (user == null) return Collections.emptyList();

        Set<Site> sites = new LinkedHashSet<Site>();
        sites.addAll(getSitesForSiteCoordinator(user));
        sites.addAll(getSitesForSubjectCoordinator(user));

        return new ArrayList<Site>(sites);
    }

    private List<Site> getSitesForSiteCoordinator(User user) {
        UserRole siteCoord = user.getUserRole(Role.SITE_COORDINATOR);
        if (siteCoord == null) return Collections.emptyList();
        return new ArrayList<Site>(siteCoord.getSites());
    }

    public Collection<Site> getSitesForSubjectCoordinator(String username) {
        User user = userDao.getByName(username);
        if (user == null) return Collections.emptyList();

        return getSitesForSubjectCoordinator(user);
    }

    private Collection<Site> getSitesForSubjectCoordinator(User user) {
        UserRole coord = user.getUserRole(Role.SUBJECT_COORDINATOR);
        if (coord == null) return Collections.emptyList();
        return new ArrayList<Site>(coord.getSites());
    }

    public Collection<Site> getSitesForSubjectCoordinator(String userName, Study study) {
        Collection<Site> sites = getSitesForSubjectCoordinator(userName);
        Set<Site> sitesForStudy = new HashSet<Site>();
        for (Site site : sites) {
            if (StudySite.findStudySite(study, site) != null) {
                sitesForStudy.add(site);
            }
        }
        return sitesForStudy;
    }

    ////// CONFIGURATION

    @Required
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

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
