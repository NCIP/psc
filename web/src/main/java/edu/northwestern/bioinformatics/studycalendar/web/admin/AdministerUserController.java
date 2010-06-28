package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.JsonArrayEditor;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AuthorizedFor;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSessionFactory;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembershipLoader;
import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.SYSTEM_ADMINISTRATOR)
@AuthorizedFor({ PscRole.SYSTEM_ADMINISTRATOR, PscRole.USER_ADMINISTRATOR })
public class AdministerUserController extends PscAbstractCommandController<ProvisionUserCommand> {
    private AuthorizationManager authorizationManager;
    private SuiteRoleMembershipLoader suiteRoleMembershipLoader;
    private ProvisioningSessionFactory provisioningSessionFactory;
    private SiteDao siteDao;

    @Override
    protected ProvisionUserCommand getCommand(HttpServletRequest request) throws Exception {
        String userIdent = ServletRequestUtils.getRequiredStringParameter(request, "user");
        User targetUser = authorizationManager.getUserById(userIdent);

        return new ProvisionUserCommand(
            targetUser,
            suiteRoleMembershipLoader.getProvisioningRoleMemberships(targetUser.getUserId()),
            provisioningSessionFactory.createSession(targetUser.getUserId()),
            authorizationManager,
            // TODO: implement authorization limits
            Arrays.asList(SuiteRole.values()), siteDao.getAll(), true);
    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        binder.registerCustomEditor(JSONArray.class, "roleChanges", new JsonArrayEditor());
    }

    @Override
    protected ModelAndView handle(ProvisionUserCommand command, BindException errors, HttpServletRequest request, HttpServletResponse response) throws Exception {
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
    public void setProvisioningSessionFactory(ProvisioningSessionFactory provisioningSessionFactory) {
        this.provisioningSessionFactory = provisioningSessionFactory;
    }

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    @Required
    public void setSuiteRoleMembershipLoader(SuiteRoleMembershipLoader suiteRoleMembershipLoader) {
        this.suiteRoleMembershipLoader = suiteRoleMembershipLoader;
    }
}
