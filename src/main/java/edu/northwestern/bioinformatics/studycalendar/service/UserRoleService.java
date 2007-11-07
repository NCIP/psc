package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserRoleDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.UserRole.findByRole;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.List;

public class UserRoleService {
    private SiteService siteService;
    private UserRoleDao userRoleDao;
    private UserDao userDao;
    private StudyCalendarAuthorizationManager authorizationManager;
    private StudySiteService studySiteService;

    public void assignUserRole(User user, Role role, Site site) throws Exception {
        UserRole userRole = findByRole(user.getUserRoles(), role);
        if (userRole == null) {
            userRole = new UserRole(user, role);
            user.addUserRole(userRole);
            authorizationManager.assignCsmGroups(user, user.getUserRoles());
        }
        
        if (role.isSiteSpecific()) {
            if (!userRole.getSites().contains(site)) {
                userRole.addSite(site);
            }
            siteService.assignProtectionGroup(site, user, role);
        }
        userRoleDao.save(userRole);
    }

    public void assignUserRole(User user, Role role) throws Exception {
        assignUserRole(user, role, null);
    }


    public void removeUserRoleAssignment(User user, Role role, Site site) throws Exception {
        UserRole userRole = findByRole(user.getUserRoles(), role);
        if (userRole != null) {
            userRole.removeSite(site);

            if (role.isSiteSpecific()) {
                siteService.removeProtectionGroup(site, user);

                /* Remove orphaned study site relationships */
                List<StudySite> removeStudySites = studySiteService.getStudySitesForParticipantCoordinator(user, site);
                userRole.getStudySites().removeAll(removeStudySites);
            }

            userRoleDao.save(userRole);

            if (userRole.getSites().isEmpty()) {
                user.removeUserRole(userRole);
                userDao.save(user);
                authorizationManager.assignCsmGroups(user, user.getUserRoles());
            }
        }
    }

    public void removeUserRoleAssignment(User user, Role role) throws Exception {
        removeUserRoleAssignment(user, role, null);
    }

    public List<User> getSiteAssociatedUsers(List<Site> sites, List<User> users) {
        List<User> associatedUsers = new ArrayList<User>();
        for (Site site : sites) {
            for (User user : users) {
                for (UserRole userRole : user.getUserRoles()) {
                    if (userRole.getSites().contains(site) && !associatedUsers.contains(user)) {
                        associatedUsers.add(user);
                        break;
                    }
                }
            }
        }
        return associatedUsers;
    }


    private void removeStudySites(UserRole userRole, Site site) {
        for (StudySite studySite : userRole.getStudySites()) {
            if (studySite.getSite().equals(site)) {
                userRole.removeStudySite(studySite);
            }
        }
    }

    @Required
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Required
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    @Required
    public void setUserRoleDao(UserRoleDao userRoleDao) {
        this.userRoleDao = userRoleDao;
    }

    @Required
    public void setStudySiteService(StudySiteService studySiteService) {
        this.studySiteService = studySiteService;
    }

    @Required
    public void setStudyCalendarAuthorizationManager(StudyCalendarAuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }
}
