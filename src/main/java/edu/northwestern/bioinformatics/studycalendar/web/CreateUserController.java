package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.validation.BindException;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.RoleEditor;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@AccessControl(roles = Role.STUDY_ADMIN)
public class CreateUserController extends PscCancellableFormController {
    private UserService userService;

    public CreateUserController() {
        setCommandClass(CreateUserCommand.class);
        setFormView("createUser");
        setValidator(new ValidatableValidator());
        setSuccessView("listUsers");
        setCancelView("listUsers");
        setCrumb(new Crumb());
    }


    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, Object> refdata = new HashMap<String, Object>();
        List<Role> roles = Arrays.asList(Role.values());
        refdata.put("roles", roles);

        String actionText = ServletRequestUtils.getIntParameter(httpServletRequest, "editId") == null ? "Create" : "Edit";
        refdata.put("actionText", actionText);

        Integer editId = ServletRequestUtils.getIntParameter(httpServletRequest, "editId");
        if (editId != null) {
            User user = userService.getUserById(editId);
            refdata.put("user", user);
        }

        return refdata;
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        binder.registerCustomEditor(Role.class, "userRoles", new RoleEditor());
        // TODO: add binder to user domain object
        //binder.registerCustomEditor(User.class, "user", )
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        CreateUserCommand command = (CreateUserCommand) oCommand;
        
        User user = command.getId() != null ? userService.getUserById(command.getId()) : new User();

        user.setName(command.getName());
        user.setRoles(command.getUserRoles());
        user.setActiveFlag(command.getActiveFlag());
        user.setPlainTextPassword(command.getPassword());

        userService.saveUser(user);

        return new ModelAndView(new RedirectView(getSuccessView()));
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
    	CreateUserCommand command = new CreateUserCommand();
        command.setUserService(userService);
        command.setActiveFlag(new Boolean(true));

        Integer editId = ServletRequestUtils.getIntParameter(request, "editId");
        if(editId != null) {
           User user = userService.getUserById(editId);
           command.setId(user.getId());
           command.setName(user.getName());
           command.setUserRoles(user.getRoles());
           command.setActiveFlag(user.getActiveFlag());
           command.setPassword(user.getPlainTextPassword());
           command.setRePassword(user.getPlainTextPassword());
        }

        return command;
    }

    protected ModelAndView onCancel(Object command) throws Exception {
		return new ModelAndView(new RedirectView(getCancelView()));
	}

    

    @Required
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private static class Crumb extends DefaultCrumb {
        public String getName(BreadcrumbContext context) {
            StringBuilder sb = new StringBuilder();
            if (context.getUser() == null) {
                sb.append( "Create User");
            } else {
                sb.append(" Edit User ").append(context.getUser().getName());
            }
            return sb.toString();
        }

        public Map<String, String> getParameters(BreadcrumbContext context) {
            Map<String, String> params = new HashMap<String, String>();
            if (context.getUser() != null) {
                params.put("editId", context.getUser().getId().toString());
            }
            return params;
        }
    }
}
