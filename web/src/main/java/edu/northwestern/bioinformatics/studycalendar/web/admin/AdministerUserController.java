package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.JsonArrayEditor;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.northwestern.bioinformatics.studycalendar.web.osgi.InstalledAuthenticationSystem;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSessionFactory;
import gov.nih.nci.security.AuthorizationManager;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class AdministerUserController
    extends PscAbstractCommandController<ProvisionUserCommand>
    implements PscAuthorizedHandler
{
    private static final String USER_PARAMETER_NAME = "user";

    private AuthorizationManager authorizationManager;
    private ApplicationSecurityManager applicationSecurityManager;
    private PscUserService pscUserService;
    private ProvisioningSessionFactory provisioningSessionFactory;
    private SiteDao siteDao;
    private InstalledAuthenticationSystem installedAuthenticationSystem;

    @Override
    public Collection<ResourceAuthorization> authorizations(
        String httpMethod, Map<String, String[]> queryParameters
    ) {
        return ResourceAuthorization.createCollection(
            PscRole.USER_ADMINISTRATOR, PscRole.SYSTEM_ADMINISTRATOR);
    }

    @Override
    protected ProvisionUserCommand getCommand(HttpServletRequest request) throws Exception {
        String username = ServletRequestUtils.getStringParameter(request, USER_PARAMETER_NAME);
        PscUser targetUser;
        if (username == null) {
            targetUser = null;
        } else {
            targetUser = pscUserService.getProvisionableUser(username);
        }

        return ProvisionUserCommand.create(targetUser,
            provisioningSessionFactory, authorizationManager,
            installedAuthenticationSystem.getAuthenticationSystem(),
            siteDao, applicationSecurityManager.getUser());
    }

    @Override
    protected void initBinder(
        HttpServletRequest request, ServletRequestDataBinder binder
    ) throws Exception {
        super.initBinder(request, binder);
        binder.registerCustomEditor(JSONArray.class, "roleChanges", new JsonArrayEditor());
        binder.registerCustomEditor(
            Date.class, "user.endDate", getControllerTools().getDateEditor(false));
    }

    @Override
    protected ModelAndView handle(
        ProvisionUserCommand command, BindException errors,
        HttpServletRequest request, HttpServletResponse response
    ) throws Exception {
        if (command.isNewUser() && request.getParameter(USER_PARAMETER_NAME) != null) {
            response.sendError(
                HttpServletResponse.SC_NOT_FOUND,
                "No user " + request.getParameter(USER_PARAMETER_NAME));
            return null;
        }
        if ("POST".equals(request.getMethod())) {
            command.apply();
            return new ModelAndView("redirectToUserList");
        } else {
            return new ModelAndView("admin/administerUser", errors.getModel());
        }
    }

    ////// CONFIGURATION

    @Required
    public void setAuthorizationManager(AuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }

    @Required
    public void setApplicationSecurityManager(
        ApplicationSecurityManager applicationSecurityManager
    ) {
        this.applicationSecurityManager = applicationSecurityManager;
    }

    @Required
    public void setProvisioningSessionFactory(
        ProvisioningSessionFactory provisioningSessionFactory
    ) {
        this.provisioningSessionFactory = provisioningSessionFactory;
    }

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    @Required
    public void setPscUserService(PscUserService pscUserService) {
        this.pscUserService = pscUserService;
    }

    @Required
    public void setInstalledAuthenticationSystem(
        InstalledAuthenticationSystem installedAuthenticationSystem
    ) {
        this.installedAuthenticationSystem = installedAuthenticationSystem;
    }
}
