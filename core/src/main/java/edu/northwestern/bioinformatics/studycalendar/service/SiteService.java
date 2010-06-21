package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserRoleDao;
import edu.northwestern.bioinformatics.studycalendar.domain.BlackoutDate;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.SiteConsumer;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    private SiteConsumer siteConsumer;
    private UserService userService;
    private UserRoleDao userRoleDao;

    public Site getById(int id) {
        return nullSafeRefresh(siteDao.getById(id));
    }

    public Site getByAssignedIdentifier(final String assignedIdentifier) {
        return nullSafeRefresh(siteDao.getByAssignedIdentifier(assignedIdentifier));
    }

    private Site nullSafeRefresh(Site site) {
        if (site == null) {
            return null;
        } else {
            return siteConsumer.refresh(site);
        }
    }

    public List<Site> getAll() {
        return siteConsumer.refresh(siteDao.getAll());
    }

    public Site getByName(final String name) {
        return nullSafeRefresh(siteDao.getByName(name));
    }
    
    public Site createOrUpdateSite(Site site) {
        siteDao.save(site);
        return site;
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
        if (!site.hasAssignments()) {
            List<UserRole> userRoles = userRoleDao.getUserRolesForSite(site);
            if (userRoles != null) {
                for (UserRole userRole: userRoles){
                    userRole.removeSite(site);
                }
            }
            for (StudySite studySite: site.getStudySites()) {
                studySite.getStudy().getStudySites().remove(studySite);
            }
            siteDao.delete(site);
        }
    }

    /**
     * Creates a new site if existing site is null. Or merge existing site with new site if existing site is not null
     *
     * @param existingSite existing    site
     * @param newSite      new site
     * @throws Exception
     */
    public Site createOrMergeSites(final Site existingSite, final Site newSite) throws Exception {
        if (existingSite == null) {
            return createOrUpdateSite(newSite);
        } else if (existingSite.getProvider() == null){
            Site site = getById(existingSite.getId());
            site.setName(newSite.getName());
            if (newSite.getAssignedIdentifier() != null) site.setAssignedIdentifier(newSite.getAssignedIdentifier());
            return createOrUpdateSite(site);
        } else {
            throw new StudyCalendarSystemException("The provided site %s is not editable", existingSite.getAssignedIdentifier());
        }
    }

    public BlackoutDate resolveSiteForBlackoutDate(BlackoutDate blackoutDate) {
        Site site = siteDao.getByAssignedIdentifier(blackoutDate.getSite().getAssignedIdentifier());
        if (site == null) {
            throw new StudyCalendarValidationException("Site '%s' not found. Please define a site that exists.",
                    blackoutDate.getSite().getAssignedIdentifier());
        }
        blackoutDate.setSite(site);
        return blackoutDate;
    }

    ////// CONFIGURATION
    @Required
    public void setUserRoleDao(UserRoleDao userRoleDao) {
        this.userRoleDao = userRoleDao;
    }

    @Required
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    @Required
    public void setSiteConsumer(SiteConsumer siteConsumer) {
        this.siteConsumer = siteConsumer;
    }
}
