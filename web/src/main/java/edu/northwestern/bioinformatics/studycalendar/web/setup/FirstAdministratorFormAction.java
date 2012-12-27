/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.setup;

import edu.northwestern.bioinformatics.studycalendar.core.CsmUserCache;
import edu.northwestern.bioinformatics.studycalendar.web.admin.AdministerUserCommand;
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
    private CsmUserCache csmUserCache;

    public FirstAdministratorFormAction() {
        super(AdministerUserCommand.class);
        setFormObjectName("adminCommand");
        setValidator(new ValidatableValidator());
        setFormObjectScope(ScopeType.REQUEST);
    }

    @Override
    protected AdministerUserCommand createFormObject(RequestContext context) throws Exception {
        return new FirstAdministratorCommand(
            provisioningSessionFactory, csmAuthorizationManager,
            installedAuthenticationSystem.getAuthenticationSystem(), csmUserCache);
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

    @Required
    public void setCsmUserCache(CsmUserCache csmUserCache) {
        this.csmUserCache = csmUserCache;
    }
}
