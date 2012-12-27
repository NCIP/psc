/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.setup;

import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.execution.ScopeType;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.DataBinder;
import org.osgi.framework.BundleContext;
import edu.northwestern.bioinformatics.studycalendar.web.osgi.InstalledAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.web.admin.AuthenticationSystemSelectorCommand;
import edu.northwestern.bioinformatics.studycalendar.web.admin.AuthenticationSystemDirectory;
import edu.northwestern.bioinformatics.studycalendar.security.AuthenticationSystemConfiguration;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.Membrane;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationPropertyEditor;

/**
 * @author Jalpa Patel
 */
public class AuthenticationSystemSetupFormAction extends FormAction {
    private InstalledAuthenticationSystem installedAuthenticationSystem;
    private BundleContext bundleContext;
    private Membrane membrane;
    private Configuration storedAuthenticationSystemConfiguration;

    public AuthenticationSystemSetupFormAction() {
        super(AuthenticationSystemSelectorCommand.class);
        setFormObjectName("authenticationSystemSetupCommand");
        setValidator(new ValidatableValidator());
        setFormObjectScope(ScopeType.REQUEST);
    }

    public Event setupReferenceData(RequestContext context) throws Exception {
        MutableAttributeMap requestScope = context.getRequestScope();
        AuthenticationSystemSelectorCommand command = (AuthenticationSystemSelectorCommand)createFormObject(context);
        String authenticationSystemValue = context.getFlowScope().get("authenticationSystemValue").toString();
        requestScope.put("command", command);
        requestScope.put("authenticationSystemName", command.getDirectory().get(authenticationSystemValue).getName());
        requestScope.put("authenticationSystemKey", AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM.getKey());
        requestScope.put("authenticationSystemValue",authenticationSystemValue);
        return success();
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected void initBinder(RequestContext context, DataBinder binder){
        super.initBinder(context, binder);
        Configuration configuration = ((AuthenticationSystemSelectorCommand) createFormObject(context)).getWorkConfiguration();
        for (ConfigurationProperty<?> property : configuration.getProperties().getAll()) {
            binder.registerCustomEditor(Object.class, "conf[" + property.getKey() + "].value",
                new ConfigurationPropertyEditor(property));
        }
    }

    @Override
    protected Object createFormObject(RequestContext context){
        return new AuthenticationSystemSelectorCommand(
            context.getFlowScope().get("authenticationSystemValue").toString(),
            storedAuthenticationSystemConfiguration,
            new AuthenticationSystemDirectory(bundleContext, membrane),
            installedAuthenticationSystem);
    }

    ////// CONFIGURATION

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setMembrane(Membrane membrane) {
        this.membrane = membrane;
    }

    public void setStoredAuthenticationSystemConfiguration(Configuration storedAuthenticationSystemConfiguration) {
        this.storedAuthenticationSystemConfiguration = storedAuthenticationSystemConfiguration;
    }

    @Required
    public void setInstalledAuthenticationSystem(InstalledAuthenticationSystem installedAuthenticationSystem) {
        this.installedAuthenticationSystem = installedAuthenticationSystem;
    }
}
