/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.security.AuthenticationSystemConfiguration;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.Membrane;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.northwestern.bioinformatics.studycalendar.web.osgi.InstalledAuthenticationSystem;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationPropertyEditor;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.SYSTEM_ADMINISTRATOR;

/**
 * @author Rhett Sutphin
 */
public class AuthenticationSystemSelectorController
    extends PscAbstractCommandController<AuthenticationSystemSelectorCommand> implements PscAuthorizedHandler
{
    private InstalledAuthenticationSystem installedAuthenticationSystem;
    private BundleContext bundleContext;
    private Membrane membrane;
    private Configuration storedAuthenticationSystemConfiguration;

    public AuthenticationSystemSelectorController() {
        setCrumb(new DefaultCrumb("Configure authentication system"));
        setValidator(new ValidatableValidator());
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(SYSTEM_ADMINISTRATOR);
    }

    @Override
    protected Object getCommand(HttpServletRequest request) throws Exception {
        return new AuthenticationSystemSelectorCommand(
            request.getParameter(String.format(
                "conf[%s].value", AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM.getKey())),
            storedAuthenticationSystemConfiguration,
            new AuthenticationSystemDirectory(bundleContext, membrane),
            installedAuthenticationSystem);
    }

    @Override
    protected boolean suppressBinding(HttpServletRequest request) {
        // This prevents validation from happening during the AJAX request which loads the
        // configuration property fields.
        return !isSubmit(request);
    }

    private boolean isSubmit(HttpServletRequest request) {
        return !"GET".equals(request.getMethod());
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        Configuration configuration = ((AuthenticationSystemSelectorCommand) getCommand(request)).getWorkConfiguration();
        for (ConfigurationProperty<?> property : configuration.getProperties().getAll()) {
            binder.registerCustomEditor(Object.class, "conf[" + property.getKey() + "].value",
                new ConfigurationPropertyEditor(property));
        }
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected ModelAndView handle(
        AuthenticationSystemSelectorCommand command, BindException errors,
        HttpServletRequest request, HttpServletResponse response
    ) throws Exception {
        Map<String, Object> model = errors.getModel();
        model.put("authenticationSystemKey", AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM.getKey());

        if (getControllerTools().isAjaxRequest(request)) {
            return new ModelAndView("admin/ajax/updateSelectedAuthenticationSystem", model);
        } else if (isSubmit(request) && !errors.hasErrors()) {
            command.apply();
            return new ModelAndView("redirect:/pages/admin/configureAuthentication");
        } else {
            return new ModelAndView("admin/configureAuthenticationSystem", model);
        }
    }

    ////// CONFIGURATION

    @Required
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Required
    public void setMembrane(Membrane membrane) {
        this.membrane = membrane;
    }

    @Required
    public void setInstalledAuthenticationSystem(InstalledAuthenticationSystem system) {
        this.installedAuthenticationSystem = system;
    }

    @Required
    public void setStoredAuthenticationSystemConfiguration(Configuration storedAuthenticationSystemConfiguration) {
        this.storedAuthenticationSystemConfiguration = storedAuthenticationSystemConfiguration;
    }
}
