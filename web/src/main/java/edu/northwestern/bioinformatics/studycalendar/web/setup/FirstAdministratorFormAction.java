package edu.northwestern.bioinformatics.studycalendar.web.setup;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.web.admin.ProvisionUserCommand;
import edu.northwestern.bioinformatics.studycalendar.web.osgi.InstalledAuthenticationSystem;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSessionFactory;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.security.AuthorizationManager;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.ScopeType;

import java.util.Arrays;
import java.util.Collections;


/**
 * @author Rhett Sutphin
 */
public class FirstAdministratorFormAction extends FormAction {
    private InstalledAuthenticationSystem installedAuthenticationSystem;
    private AuthorizationManager csmAuthorizationManager;
    private ProvisioningSessionFactory provisioningSessionFactory;

    public FirstAdministratorFormAction() {
        super(ProvisionUserCommand.class);
        setFormObjectName("adminCommand");
        setValidator(new ValidatableValidator());
        setFormObjectScope(ScopeType.REQUEST);
    }

    @Override
    protected ProvisionUserCommand createFormObject(RequestContext context) throws Exception {
        ProvisionUserCommand command = ProvisionUserCommand.createForUnknownUser(
            provisioningSessionFactory, csmAuthorizationManager,
            installedAuthenticationSystem.getAuthenticationSystem(),
            Arrays.asList(SuiteRole.SYSTEM_ADMINISTRATOR),
            Collections.<Site>emptyList(), false
        );
        command.setLookUpBoundUser(true);
        JSONObject addSysAdmin = new JSONObject();
        addSysAdmin.put(ProvisionUserCommand.JSON_CHANGE_PROP_KIND, "add");
        addSysAdmin.put(ProvisionUserCommand.JSON_CHANGE_PROP_ROLE, PscRole.SYSTEM_ADMINISTRATOR.getCsmName());
        command.getRoleChanges().put(addSysAdmin);
        return command;
    }

    public Event setupReferenceData(RequestContext context) throws Exception {
        MutableAttributeMap requestScope = context.getRequestScope();
        requestScope.put("usesLocalPasswords",
            installedAuthenticationSystem.getAuthenticationSystem().usesLocalPasswords());
        return success();
    }

    ////// CONFIGURATION

    @Required
    public void setCsmAuthorizationManager(AuthorizationManager csmAuthorizationManager) {
        this.csmAuthorizationManager = csmAuthorizationManager;
    }

    @Required
    public void setProvisioningSessionFactory(ProvisioningSessionFactory provisioningSessionFactory) {
        this.provisioningSessionFactory = provisioningSessionFactory;
    }

    @Required
    public void setInstalledAuthenticationSystem(InstalledAuthenticationSystem installedAuthenticationSystem) {
        this.installedAuthenticationSystem = installedAuthenticationSystem;
    }
}
