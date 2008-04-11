package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import static edu.northwestern.bioinformatics.studycalendar.domain.Role.*;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.service.UserRoleService;
import edu.northwestern.bioinformatics.studycalendar.security.AuthenticationSystemConfiguration;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;

import java.util.*;
import java.io.Serializable;

public class CreateUserCommand implements Validatable, Serializable {
    private String password;
    private String rePassword;
    private boolean passwordModified;
    private User user;
    private Map<Site, Map<Role,RoleCell>> rolesGrid;
    private boolean userActiveFlag;

    private UserService userService;
    private SiteDao siteDao;
    private UserRoleService userRoleService;
    private UserDao userDao;
    private AuthenticationSystemConfiguration authenticationSystemConfiguration;

    public CreateUserCommand(
        User user, SiteDao siteDao, UserService userService, UserDao userDao,
        UserRoleService userRoleService,
        AuthenticationSystemConfiguration authenticationSystemConfiguration
    ) {
        this.user = user == null ? new User() : user;
        this.siteDao = siteDao;
        this.userService = userService;
        this.userDao = userDao;
        this.userRoleService = userRoleService;
        this.authenticationSystemConfiguration = authenticationSystemConfiguration;
        this.passwordModified = false;

        buildRolesGrid(this.user.getUserRoles());
    }

    private void buildRolesGrid(Set<UserRole> userRoles) {
        boolean selected;
        rolesGrid = new LinkedHashMap<Site, Map<Role,RoleCell>>();
        
        for(Site site : siteDao.getAll()) {
            for(Role role : values()) {
                selected = false;
                for(UserRole userRole : userRoles) {
                    if (userRole.getRole().equals(role) && (!role.isSiteSpecific() || userRole.getSites().contains(site)) ) {
                        selected = true;
                        break;
                    }
                }

                if (!rolesGrid.containsKey(site)) {
                    rolesGrid.put(site, new HashMap<Role, RoleCell>());
                }

                rolesGrid.get(site).put(role, createRoleCell(selected,role.isSiteSpecific()));
            }
        }

    }

    public void validate(Errors errors){
        if (user != null) {
            if (user.getName() == null || StringUtils.isEmpty(user.getName())) {
                errors.rejectValue("user.name", "error.user.name.not.specified");
            } else {
                if (user.getId() == null && userDao.getByName(user.getName()) != null){
                    errors.rejectValue("user.name", "error.user.name.already.exists");
                }
            }
            if (updatePassword() && authenticationSystemConfiguration.isLocalAuthenticationSystem()) {
                if (password == null || StringUtils.isBlank(password)){
                    errors.rejectValue("password", "error.user.password.not.specified");
                } else {
                    if (!password.equals(rePassword)) {
                        errors.rejectValue("rePassword", "error.user.repassword.does.not.match.password");
                    }
                }
            }
        }
        for (Site site : getRolesGrid().keySet()) {
            // prevent the removal of the last site coordinator for a site that has assignments
            if (site.hasAssignments()) {
                List<User> siteCoords = userDao.getSiteCoordinators(site);
                if (siteCoords.size() == 1 && siteCoords.contains(getUser())) {
                    if (!getRolesGrid().get(site).get(SITE_COORDINATOR).isSelected()) {
                        errors.rejectValue(gridFieldName(site, SITE_COORDINATOR),
                            "error.user.last-site-coordinator",
                            new Object[] { getUser().getName(), site.getName() },
                            "Last site coordinator");
                    }
                }
            }
            // prevent the removal of subject coordinators with assignments
            if (!getRolesGrid().get(site).get(SUBJECT_COORDINATOR).isSelected()) {
                if (getUser().hasAssignment(site)) {
                    errors.rejectValue(
                        gridFieldName(site, SUBJECT_COORDINATOR),
                        "error.user.subject-coordinator-has-subjects",
                        new Object[] { getUser().getName(), site.getName() },
                        "Subject coordinator has subjects"
                    );
                }
            }
        }

    }

    private String gridFieldName(Site site, Role role) {
        return String.format("rolesGrid[%d][%s].selected", site.getId(), role);
    }

    public User apply() throws Exception {
        if (updatePassword()) {
            user.setActiveFlag(isUserActiveFlag());
            userService.saveUser(user, getOrCreatePassword());
        } else {
            // must be update only
            user.setActiveFlag(isUserActiveFlag());
            userDao.save(user);
        }
        assignUserRolesFromRolesGrid();
        return user;
    }

    private boolean updatePassword() {
        return passwordModified || user.getId() == null;
    }

    // generate a random password when creating a new user in a regime that doesn't use the internal passwords
    private String getOrCreatePassword() {
        if (authenticationSystemConfiguration.isLocalAuthenticationSystem()) {
            return getPassword();
        } else {
            int length = 16 + (int) Math.round(16 * Math.random());
            StringBuilder generated = new StringBuilder();
            while (generated.length() < length) {
                generated.append((char) (' ' + Math.round(('~' - ' ') * Math.random())));
            }
            return generated.toString();
        }
    }

    protected void assignUserRolesFromRolesGrid() throws Exception {
        for(Site site : rolesGrid.keySet()) {
            for(Role role : rolesGrid.get(site).keySet()) {
                if (role.isSiteSpecific()) {
                    if (rolesGrid.get(site).get(role).isSelected()) {
                        userRoleService.assignUserRole(user, role, site);
                    } else {
                        userRoleService.removeUserRoleAssignment(user, role, site);
                    }
                }
            }
        }

        Set<Role> roleList = rolesGrid.values().iterator().next().keySet();
        for(Role role : roleList) {
            if (!role.isSiteSpecific()) {
                int selected = 0;
                int notSelected = 0;
                for (Site innerSite : rolesGrid.keySet()) {
                    if (!rolesGrid.get(innerSite).get(role).isSelected()) notSelected++;
                    if (rolesGrid.get(innerSite).get(role).isSelected()) selected++;
                }
                if (selected == notSelected) {
                    if (user.getUserRole(role) == null) {
                        userRoleService.assignUserRole(user, role);
                    } else {
                        userRoleService.removeUserRoleAssignment(user, role);
                    }
                } else if (selected == 1) {
                    userRoleService.assignUserRole(user, role);
                } else if (notSelected == 1) {
                    userRoleService.removeUserRoleAssignment(user, role);
                } else if (selected > notSelected) {
                    userRoleService.assignUserRole(user, role);
                } else if (notSelected > selected) {
                    userRoleService.removeUserRoleAssignment(user, role);
                }
            }
        }
    }

    public static class RoleCell implements Serializable {
        private boolean selected;
        private boolean siteSpecific;

        public RoleCell(boolean selected, boolean siteSpecific) {
            this.selected = selected;
            this.siteSpecific = siteSpecific;
        }

        public boolean isSelected() {
            return selected;
        }

        public boolean isSiteSpecific() {
            return siteSpecific;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public void setSiteSpecific(boolean siteSpecific) {
            this.siteSpecific = siteSpecific;
        }
    }

    protected static RoleCell createRoleCell(boolean selected, boolean siteSpecific) {
        return new RoleCell(selected, siteSpecific);
    }

    ////// BOUND PROPERTIES

    public User getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRePassword() {
        return rePassword;
    }

    public void setRePassword(String rePassword) {
        this.rePassword = rePassword;
    }

    public Map<Site, Map<Role, RoleCell>> getRolesGrid() {
        return rolesGrid;
    }

    public boolean isPasswordModified() {
        return passwordModified;
    }

    public void setPasswordModified(boolean passwordModified) {
        this.passwordModified = passwordModified;
    }

    public void setUserActiveFlag(boolean userActiveFlag) {
        this.userActiveFlag = userActiveFlag;
    }

    public boolean isUserActiveFlag() {
        return userActiveFlag;
    }
}
