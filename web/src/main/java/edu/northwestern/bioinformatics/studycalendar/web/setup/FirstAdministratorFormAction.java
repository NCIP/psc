package edu.northwestern.bioinformatics.studycalendar.web.setup;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.service.UserRoleService;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.web.CreateUserCommand;
import edu.northwestern.bioinformatics.studycalendar.web.osgi.InstalledAuthenticationSystem;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.ScopeType;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.core.collection.MutableAttributeMap;


/**
 * @author Rhett Sutphin
 */
public class FirstAdministratorFormAction extends FormAction {
    private SiteDao siteDao;
    private UserDao userDao;
    private UserService userService;
    private UserRoleService userRoleService;
    private InstalledAuthenticationSystem installedAuthenticationSystem;

    public FirstAdministratorFormAction() {
        super(CreateUserCommand.class);
        setFormObjectName("adminCommand");
        setValidator(new ValidatableValidator());
        setFormObjectScope(ScopeType.REQUEST);
    }

    protected Object createFormObject(RequestContext context) throws Exception {
        CreateUserCommand command = new CreateUserCommand(
            null, siteDao, userService, userDao, userRoleService, installedAuthenticationSystem
        );
        command.setUserActiveFlag(true);
        command.setPasswordModified(true);
        command.setInitialAdministrator(true);
        return command;
    }

    public Event setupReferenceData(RequestContext context) throws Exception {
        MutableAttributeMap requestScope = context.getRequestScope();
        requestScope.put("usesLocalPasswords", installedAuthenticationSystem.getAuthenticationSystem().usesLocalPasswords());
        return success();
    }

    ////// CONFIGURATION

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    @Required
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Required
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Required
    public void setUserRoleService(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    @Required
    public void setInstalledAuthenticationSystem(InstalledAuthenticationSystem installedAuthenticationSystem) {
        this.installedAuthenticationSystem = installedAuthenticationSystem;
    }
}
