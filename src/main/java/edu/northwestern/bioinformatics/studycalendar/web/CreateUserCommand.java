package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.nwu.bioinformatics.commons.spring.Validatable;

import java.util.*;

import org.springframework.validation.Errors;
import org.apache.commons.lang.StringUtils;

public class CreateUserCommand implements Validatable {
    private String name;
    private Set<UserRole> userRoles;
    private UserService userService;
    private Boolean activeFlag;
    private Integer id;
    private String password;
    private String rePassword;
    private User user;
    private SiteDao siteDao;
    private Map<Site, Map<Role,RoleCell>> rolesGrid;

    public CreateUserCommand(User user, SiteDao siteDao) {
        this.user = user;
        this.siteDao = siteDao;
        userRoles = new HashSet<UserRole>();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Set<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    public Boolean getActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(Boolean activeFlag) {
        this.activeFlag = activeFlag;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public void validate(Errors errors) {
        if(StringUtils.isEmpty(name)) {
            errors.rejectValue("name", "error.user.name.not.specified");
        } else {
            if(id == null && userService.getUserByName(name) != null){
                errors.rejectValue("name", "error.user.name.already.exists");
            }
        }
        if(password == null || StringUtils.isBlank(password)){
            errors.rejectValue("password", "error.user.password.not.specified");
        } else {
            if(!password.equals(rePassword)) {
                errors.rejectValue("rePassword", "error.user.repassword.does.not.match.password");
                //errors.rejectValue("password"); reject normall password too
            }
        }
        if(userRoles == null || userRoles.size() <= 0) {
            errors.rejectValue("userRoles", "error.user.role.not.specified");
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
        user.setUserRoles(userRoles);

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
                    newUserRole.setRole(role);
                    if (role.isSiteSpecific() == true) {
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
    }

    protected static RoleCell createRoleCell(boolean selected, boolean siteSpecific) {
        return new RoleCell(selected, siteSpecific);
    }
}
