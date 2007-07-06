package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.BaseCommandController;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.validation.BindException;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.RoleEditor;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@AccessControl(protectionGroups = StudyCalendarProtectionGroup.STUDY_ADMINISTRATOR)
public class NewUserController extends PscSimpleFormController {
    private UserService userService;

    public NewUserController() {
        setCommandClass(NewUserCommand.class);
        setFormView("createUser");
        setValidator(new ValidatableValidator());
    }


    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, Object> refdata = new HashMap<String, Object>();
        List<Role> roles = Arrays.asList(Role.values());
        refdata.put("roles", roles);

        List<User> users = userService.getAllUsers();
        refdata.put("users", users);

        String actionText = ServletRequestUtils.getIntParameter(httpServletRequest, "editId") == null ? "Create" : "Edit";
        refdata.put("actionText", actionText);

        return refdata;
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        binder.registerCustomEditor(Role.class, "userRoles", new RoleEditor());
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        NewUserCommand command = (NewUserCommand) oCommand;
        
        User user = command.getId() != null ? userService.getUserById(command.getId()) : new User();

        user.setName(command.getName());
        user.setRoles(command.getUserRoles());
        user.setActiveFlag(command.getActiveFlag());

        userService.saveUser(user);

        Map model = referenceData(request, oCommand, errors);
        command.reset();
        model.put(BaseCommandController.DEFAULT_COMMAND_NAME, command);

        return new ModelAndView("createUser", model);
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
    	NewUserCommand command = new NewUserCommand();
        command.setUserService(userService);
        command.setActiveFlag(new Boolean(true));

        Integer editId = ServletRequestUtils.getIntParameter(request, "editId");
        if(editId != null) {
           User user = userService.getUserById(editId);
           command.setId(user.getId());
           command.setName(user.getName());
           command.setUserRoles(user.getRoles());
           command.setActiveFlag(user.getActiveFlag());
        }

        return command;
    }

    

    @Required
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
