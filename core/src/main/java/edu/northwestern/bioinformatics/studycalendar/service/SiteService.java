package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.StudyCalendarAuthorizationManager;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.DomainObjectTools.createExternalObjectId;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.SiteConsumer;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
@Transactional
public class SiteService {
    private SiteDao siteDao;
    private StudyCalendarAuthorizationManager authorizationManager;
    private SiteConsumer siteConsumer;
    private UserService userService;

    public Site getById(int id) {
        return siteConsumer.refresh(siteDao.getById(id));
    }

    public Site getByAssignedIdentifier(final String assignedIdentifier) {
        return siteConsumer.refresh(siteDao.getByAssignedIdentifier(assignedIdentifier));
    }

    public List<Site> getAll() {
        return siteConsumer.refresh(siteDao.getAll());
    }
    
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

    public Collection<Site> getSitesForSubjectCoordinator(String username) {
        User user = userService.getUserByName(username);
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
        if (checkIfSiteCanBeDeleted(site)) {
            // first remove the protection group
            authorizationManager.removeProtectionGroup(createExternalObjectId(site));

            // it should also delete the study sites and holidays
            siteDao.delete(site);
        }
    }

    public boolean checkIfSiteCanBeDeleted(final Site site) {
        // site can be deleted only if it has not assignments
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
            Site site = getById(existingSite.getId());
            site.setName(newSite.getName());
            if (newSite.getAssignedIdentifier() != null) site.setAssignedIdentifier(newSite.getAssignedIdentifier());
            return createOrUpdateSite(site);
        }
    }

    ////// CONFIGURATION

    @Required
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    @Required
    public void setStudyCalendarAuthorizationManager(StudyCalendarAuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }

    @Required
    public void setSiteConsumer(SiteConsumer siteConsumer) {
        this.siteConsumer = siteConsumer;
    }
}
