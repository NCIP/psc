package edu.northwestern.bioinformatics.studycalendar.web.setup;

import edu.northwestern.bioinformatics.studycalendar.web.admin.ProvisionUserCommand;
import edu.northwestern.bioinformatics.studycalendar.web.osgi.InstalledAuthenticationSystem;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSessionFactory;
import gov.nih.nci.security.AuthorizationManager;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.ScopeType;


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
        return new FirstAdministratorCommand(
            provisioningSessionFactory, csmAuthorizationManager,
            installedAuthenticationSystem.getAuthenticationSystem());
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
