package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.nwu.bioinformatics.commons.spring.Validatable;

import java.util.List;
import java.util.Set;

import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.apache.commons.lang.StringUtils;

public class NewUserCommand implements Validatable {
    private String name;
    private Set<Role> userRoles;
    private UserService userService;

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

    public void validate(Errors errors) {
        if(StringUtils.isEmpty(name)) {
            errors.rejectValue("name", "error.user.name.not.specified");
        } else {
            if(userService.getUserByName(name) != null){
                errors.rejectValue("name", "error.user.name.already.exists");
            }
        }
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
