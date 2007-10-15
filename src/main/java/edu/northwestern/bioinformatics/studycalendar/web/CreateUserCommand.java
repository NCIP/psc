package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import gov.nih.nci.security.util.StringEncrypter;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CreateUserCommand implements Validatable {
    private Set<UserRole> userRoles;
    private UserService userService;
    private String rePassword;
    private User user;
    private SiteDao siteDao;
    private Map<Site, Map<Role,RoleCell>> rolesGrid;

    public CreateUserCommand(User user, SiteDao siteDao, UserService userService) {
        this.user = user;
        this.siteDao = siteDao;
        this.userService = userService;
        userRoles = new HashSet<UserRole>();
        if(user == null) user = new User();
        buildRolesGrid(user.getUserRoles());
    }

    private void buildRolesGrid(Set<UserRole> userRoles) {
        boolean selected;
        rolesGrid = new HashMap<Site, Map<Role,RoleCell>>();
        
        for(Site site : siteDao.getAll()) {
            for(Role role : Role.values()) {
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

    public User getUser() {
        return user;
    }

    public Set<UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Set<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    public String getRePassword() {
        return rePassword;
    }

    public void setRePassword(String rePassword) {
        this.rePassword = rePassword;
    }

    public void validate(Errors errors){
        if (user != null) {
            if (user.getName() == null || StringUtils.isEmpty(user.getName())) {
                errors.rejectValue("user.name", "error.user.name.not.specified");
            } else {
                if (user.getId() == null && userService.getUserByName(user.getName()) != null){
                    errors.rejectValue("user.name", "error.user.name.already.exists");
                }
            }
            try {
                if (user.getPlainTextPassword() == null || StringUtils.isBlank(user.getPlainTextPassword())){
                    errors.rejectValue("user.password", "error.user.password.not.specified");
                } else {
                    if (!user.getPlainTextPassword().equals(rePassword)) {
                        errors.rejectValue("rePassword", "error.user.repassword.does.not.match.password");
                    }
                }
            } catch(StringEncrypter.EncryptionException encryptExcep) {
                errors.rejectValue("user.password", "error.user.password.not.specified");
            }
            // TODO: check grid for roles
           /* if (user.getUserRoles() == null || user.getUserRoles().size() <= 0) {
                errors.rejectValue("userRoles", "error.user.role.not.specified");
            }*/
        }

    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public User apply() throws Exception {
        interpretRolesGrid(rolesGrid);
        user.clearUserRoles();
        user.addAllUserRoles(userRoles);

        return userService.saveUser(user);
    }

    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    public Map<Site, Map<Role, RoleCell>> getRolesGrid() {
        return rolesGrid;
    }

    public void interpretRolesGrid(Map<Site, Map<Role,RoleCell>> rolesGrid) {
        for(Site site : rolesGrid.keySet()) {
            for(Role role : rolesGrid.get(site).keySet()) {
                if (rolesGrid.get(site).get(role).isSelected()) {
                    UserRole newUserRole = new UserRole();
                    newUserRole.setUser(user);
                    newUserRole.setRole(role);
                    if (role.isSiteSpecific()) {
                        for (UserRole userRole : userRoles) {
                            if (userRole.getRole().equals(role)) {
                                newUserRole = userRole;
                                break;
                            }
                        }
                        newUserRole.getSites().add(site);
                    }

                    userRoles.add(newUserRole);
                }
            }
        }
    }

    public static class RoleCell {
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
}
