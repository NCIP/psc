package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.service.UserRoleService;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.RoleEditor;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.northwestern.bioinformatics.studycalendar.web.osgi.InstalledAuthenticationSystem;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import gov.nih.nci.cabig.ctms.editors.DaoBasedEditor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.USER_ADMINISTRATOR;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.SYSTEM_ADMINISTRATOR;

@AccessControl(roles = {Role.STUDY_ADMIN, Role.SYSTEM_ADMINISTRATOR})
@Deprecated // don't remove until all features are supported by AdministerUserController
public class CreateUserController extends PscCancellableFormController implements PscAuthorizedHandler {
    private UserService userService;
    private SiteDao siteDao;
    private UserRoleService userRoleService;
    private UserDao userDao;
    private InstalledAuthenticationSystem installedAuthenticationSystem;

    public CreateUserController() {
        setCommandClass(CreateUserCommand.class);
        setFormView("createUser");
        setValidator(new ValidatableValidator());
        setSuccessView("listUsers");
        setCancelView("listUsers");
        setCrumb(new Crumb());
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(USER_ADMINISTRATOR, SYSTEM_ADMINISTRATOR);
    }

    @Override
    protected Map referenceData(HttpServletRequest request, Object o, Errors errors) throws Exception {
        CreateUserCommand command = (CreateUserCommand) o;
        Map<String, Object> refdata = new HashMap<String, Object>();
        List<Role> roles = Arrays.asList(Role.values());
        refdata.put("roles", roles);

        String actionText = ServletRequestUtils.getIntParameter(request, "id") == null ? "Create" : "Edit";
        refdata.put("actionText", actionText);

        refdata.put("user", command.getUser());
        refdata.put("usingLocalAuthenticationSystem", installedAuthenticationSystem.getAuthenticationSystem().usesLocalPasswords());

        return refdata;
    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);

        binder.registerCustomEditor(Site.class, new DaoBasedEditor(siteDao));
        binder.registerCustomEditor(Role.class, new RoleEditor());
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        CreateUserCommand command = (CreateUserCommand) oCommand;

        command.apply();

        return new ModelAndView(new RedirectView(getSuccessView()));
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        Integer editId = ServletRequestUtils.getIntParameter(request, "id");
        User user = (editId != null) ? userDao.getById(editId) : new User();

        return new CreateUserCommand(user, siteDao, userService, userDao, userRoleService, installedAuthenticationSystem);
    }

    @Override
    protected ModelAndView onCancel(Object command) throws Exception {
        // TODO: fix this
        return new ModelAndView(new RedirectView(getCancelView()));
    }

    ////// CONFIGURATION

    @Required
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    @Required
    public void setUserRoleService(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    @Required
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Required
    public void setInstalledAuthenticationSystem(InstalledAuthenticationSystem installedAuthenticationSystem) {
        this.installedAuthenticationSystem = installedAuthenticationSystem;
    }

    private static class Crumb extends DefaultCrumb {
        @Override
        public String getName(DomainContext context) {
            StringBuilder sb = new StringBuilder();
            if (context.getUser() == null || context.getUser().getId() == null) {
                sb.append( "Create User");
            } else {
                sb.append(" Edit User ").append(context.getUser().getName());
            }
            return sb.toString();
        }

        @Override
        public Map<String, String> getParameters(DomainContext context) {
            Map<String, String> params = new HashMap<String, String>();
            if (context.getUser() != null && context.getUser().getId() != null) {
                params.put("id", context.getUser().getId().toString());
            }
            return params;
        }
    }
}
