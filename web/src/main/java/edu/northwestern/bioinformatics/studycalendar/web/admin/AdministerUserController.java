package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.core.CsmUserCache;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRoleGroup;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.JsonObjectEditor;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.northwestern.bioinformatics.studycalendar.web.osgi.InstalledAuthenticationSystem;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSessionFactory;
import gov.nih.nci.security.AuthorizationManager;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class AdministerUserController
    extends PscAbstractCommandController<AdministerUserCommand>
    implements PscAuthorizedHandler
{
    private static final String USER_PARAMETER_NAME = "user";

    private AuthorizationManager authorizationManager;
    private ApplicationSecurityManager applicationSecurityManager;
    private PscUserService pscUserService;
    private ProvisioningSessionFactory provisioningSessionFactory;
    private CsmUserCache csmUserCache;
    private InstalledAuthenticationSystem installedAuthenticationSystem;

    public AdministerUserController() {
        setValidator(new ValidatableValidator());
    }

    @Override
    public Collection<ResourceAuthorization> authorizations(
        String httpMethod, Map<String, String[]> queryParameters
    ) {
        return ResourceAuthorization.createCollection(
            PscRole.USER_ADMINISTRATOR, PscRole.SYSTEM_ADMINISTRATOR);
    }

    @Override
    protected AdministerUserCommand getCommand(HttpServletRequest request) throws Exception {
        String username = ServletRequestUtils.getStringParameter(request, USER_PARAMETER_NAME);
        PscUser targetUser;
        if (username == null) {
            targetUser = null;
        } else {
            targetUser = pscUserService.getProvisionableUser(username);
        }

        return AdministerUserCommand.create(targetUser,
            provisioningSessionFactory, authorizationManager,
            installedAuthenticationSystem.getAuthenticationSystem(),
            applicationSecurityManager, pscUserService,
            csmUserCache, applicationSecurityManager.getUser());
    }

    @Override
    protected void initBinder(
        HttpServletRequest request, ServletRequestDataBinder binder
    ) throws Exception {
        super.initBinder(request, binder);
        binder.registerCustomEditor(JSONObject.class, "roleChanges", new JsonObjectEditor());
        binder.registerCustomEditor(
            Date.class, "user.csmUser.endDate", getControllerTools().getDateEditor(false));
    }

    @Override
    protected ModelAndView handle(
        AdministerUserCommand command, BindException errors,
        HttpServletRequest request, HttpServletResponse response
    ) throws Exception {
        if (command.isNewUser() && request.getParameter(USER_PARAMETER_NAME) != null) {
            response.sendError(
                HttpServletResponse.SC_NOT_FOUND,
                "No user " + request.getParameter(USER_PARAMETER_NAME));
            return null;
        }
        if ("POST".equals(request.getMethod()) && !errors.hasErrors()) {
            command.apply();
            return new ModelAndView("redirectToUserList");
        } else {
            ModelAndView mv = new ModelAndView("admin/administerUser", errors.getModel());
            mv.addObject("startingNewUser", 
                command.isNewUser() && "GET".equals(request.getMethod()));
            mv.addObject("roleGroupCells", RoleGroupCell.create(command.getProvisionableRoleGroups()));
            return mv;
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
    public void setPscUserService(PscUserService pscUserService) {
        this.pscUserService = pscUserService;
    }

    @Required
    public void setCsmUserCache(CsmUserCache csmUserCache) {
        this.csmUserCache = csmUserCache;
    }

    @Required
    public void setInstalledAuthenticationSystem(
        InstalledAuthenticationSystem installedAuthenticationSystem
    ) {
        this.installedAuthenticationSystem = installedAuthenticationSystem;
    }

    public static class RoleGroupCell {
        private PscRoleGroup group;
        private Collection<ProvisioningRole> roles;
        private int row;
        private int column;

        public static Collection<RoleGroupCell> create(Map<PscRoleGroup, Collection<ProvisioningRole>> map) {
            Collection<RoleGroupCell> cells = new ArrayList<RoleGroupCell>();
            int index = 0;
            for (PscRoleGroup group : map.keySet()) {
                cells.add(new RoleGroupCell(group, map.get(group), (index / 3) + 1, (index % 3) + 1));
                index++;
            }
            return cells;
        }

        private RoleGroupCell(PscRoleGroup group, Collection<ProvisioningRole> roles, int row, int column) {
            this.group = group;
            this.roles = roles;
            this.row = row;
            this.column = column;
        }

        public PscRoleGroup getGroup() {
            return group;
        }

        public Collection<ProvisioningRole> getRoles() {
            return roles;
        }

        public int getRow() {
            return row;
        }

        public int getColumn() {
            return column;
        }
    }
}
