package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.security.AuthenticationSystemConfiguration;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.Membrane;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.web.osgi.InstalledAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.SYSTEM_ADMINISTRATOR)
public class AuthenticationSystemSelectorController
    extends PscAbstractCommandController<AuthenticationSystemSelectorCommand>
{
    private InstalledAuthenticationSystem installedAuthenticationSystem;
    private BundleContext bundleContext;
    private Membrane membrane;
    private Configuration storedAuthenticationSystemConfiguration;

    public AuthenticationSystemSelectorController() {
        setCrumb(new DefaultCrumb("Configure authentication system"));
    }

    @Override
    protected Object getCommand(HttpServletRequest httpServletRequest) throws Exception {
        return new AuthenticationSystemSelectorCommand(
            storedAuthenticationSystemConfiguration,
            new AuthenticationSystemDirectory(bundleContext, membrane),
            installedAuthenticationSystem);
    }

    @Override
    protected boolean suppressBinding(HttpServletRequest request) {
        return !isSubmit(request);
    }

    private boolean isSubmit(HttpServletRequest request) {
        return !"GET".equals(request.getMethod());
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
