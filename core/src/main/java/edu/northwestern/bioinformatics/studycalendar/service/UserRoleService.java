package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserRoleDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Transactional
public class UserRoleService implements Serializable {
    private UserRoleDao userRoleDao;
    private UserDao userDao;
    private StudySiteService studySiteService;

    public void assignUserRole(User user, Role role, Site site) throws Exception {
        UserRole userRole = user.getUserRole(role);
        if (userRole == null) {
            userRole = new UserRole(user, role);
            user.addUserRole(userRole);
        }
        
        if (role.isSiteSpecific()) {
            if (!userRole.getSites().contains(site)) {
                userRole.addSite(site);
            }
        }
        userRoleDao.save(userRole);
    }

    public void assignUserRole(User user, Role role) throws Exception {
        assignUserRole(user, role, null);
    }


    public void removeUserRoleAssignment(User user, Role role, Site site) throws Exception {
        UserRole userRole = user.getUserRole(role);
        if (userRole != null) {
            userRole.removeSite(site);

            if (role.isSiteSpecific()) {
                /* Remove StudySite relationships for site being removed */
                List<StudySite> removeStudySites = studySiteService.getStudySitesForSubjectCoordinator(user, site);
                userRole.getStudySites().removeAll(removeStudySites);
            }

            userRoleDao.save(userRole);

            if (userRole.getSites().isEmpty()) {
                user.removeUserRole(userRole);
                userDao.save(user);
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

    ////// CONFIGURATION

    @Required
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Required
    public void setUserRoleDao(UserRoleDao userRoleDao) {
        this.userRoleDao = userRoleDao;
    }

    @Required
    public void setStudySiteService(StudySiteService studySiteService) {
        this.studySiteService = studySiteService;
    }

}
