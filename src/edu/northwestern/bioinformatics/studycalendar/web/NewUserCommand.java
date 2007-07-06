package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.nwu.bioinformatics.commons.spring.Validatable;

import java.util.Set;

import org.springframework.validation.Errors;
import org.apache.commons.lang.StringUtils;

public class NewUserCommand implements Validatable {
    private String name;
    private Set<Role> userRoles;
    private UserService userService;
    private Boolean activeFlag;
    private Integer id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Role> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Set<Role> userRoles) {
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

    public void validate(Errors errors) {
        if(StringUtils.isEmpty(name)) {
            errors.rejectValue("name", "error.user.name.not.specified");
        } else {
            if(id == null && userService.getUserByName(name) != null){
                errors.rejectValue("name", "error.user.name.already.exists");
            }
        }
        if(userRoles == null || userRoles.size() <= 0) {
            errors.rejectValue("userRoles", "error.user.role.not.specified");
        }
    }

    public void reset() {
        name = null;
        userRoles = null;
        activeFlag = new Boolean(true);
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
