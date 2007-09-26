package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.nwu.bioinformatics.commons.spring.Validatable;

import java.util.Set;

import org.springframework.validation.Errors;
import org.apache.commons.lang.StringUtils;

public class CreateUserCommand implements Validatable {
    private String name;
    private Set<Role> userRoles;
    private UserService userService;
    private Boolean activeFlag;
    private Integer id;
    private String password;
    private String rePassword;

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
}
