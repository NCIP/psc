package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import static edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools.createExternalObjectId;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    public Site createOrUpdateSite(Site site) {
        siteDao.save(site);
        if (site.getId() == null || authorizationManager.getPGByName(createExternalObjectId(site)) == null) {
            // no need to update the protection group when you update the site because the protection group are created by class name+id which never changes.
            saveSiteProtectionGroup(site);
        }
        return site;
    }

    protected void saveSiteProtectionGroup(final Site site) {
        authorizationManager.createProtectionGroup(createExternalObjectId(site));
    }

    public void assignProtectionGroup(Site site, User user, Role role) throws Exception {
        ProtectionGroup sitePG = authorizationManager.getPGByName(createExternalObjectId(site));
        authorizationManager.assignProtectionGroupsToUsers(user.getCsmUserId().toString(), sitePG, role.csmGroup());
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

    public void removeSite(final Site site) throws Exception {
        boolean siteCanBeDeleted = checkIfSiteCanBeDeleted(site);
        if (siteCanBeDeleted) {//first remove the protection group
            authorizationManager.removeProtectionGroup(createExternalObjectId(site));

            //it should also delete the study sites and holidays
            siteDao.delete(site);
        }
    }

    public boolean checkIfSiteCanBeDeleted(final Site site) {

        //site can be deleted only if it has not assignments

        List<StudySite> studySiteList = site.getStudySites();
        for (StudySite studySite : studySiteList) {
            List<StudySubjectAssignment> studySubjectAssignmentList = studySite.getStudySubjectAssignments();
            if (studySubjectAssignmentList != null && !studySubjectAssignmentList.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a new site if existing site is null. Or merge exisisting site with new site if existing site is not null
     *
     * @param existingSite existing    site
     * @param newSite      new site
     * @throws Exception
     */
    public Site createOrMergeSites(final Site existingSite, final Site newSite) throws Exception {
        if (existingSite == null) {
            return createOrUpdateSite(newSite);
        } else {
            BeanUtils.copyProperties(newSite, existingSite, new String[]{"studySites", "id", "blackoutDates"});
            return createOrUpdateSite(existingSite);
        }


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

    public Site getByAssignedIdentifier(final String assignedIdentifier) {
        return siteDao.getByAssignedIdentifier(assignedIdentifier);
    }


}
