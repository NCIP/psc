package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.UserRoleDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import static edu.northwestern.bioinformatics.studycalendar.domain.UserRole.findByRole;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import gov.nih.nci.security.authorization.domainobjects.Group;

public class UserRoleService {
    private SiteService siteService;
    private UserRoleDao userRoleDao;
    private UserDao userDao;
    private StudyCalendarAuthorizationManager authorizationManager;

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
            
            userRoleDao.save(userRole);

            if (role.isSiteSpecific()) {
                siteService.removeProtectionGroup(site, user);
            }

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

    @Required
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    @Required
    public void setUserRoleDao(UserRoleDao userRoleDao) {
        this.userRoleDao = userRoleDao;
    }

    @Required
    public void setStudyCalendarAuthorizationManager(StudyCalendarAuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }
}
