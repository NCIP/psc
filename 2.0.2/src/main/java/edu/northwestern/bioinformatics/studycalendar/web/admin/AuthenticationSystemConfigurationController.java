package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.security.AuthenticationSystemConfiguration;
import static edu.northwestern.bioinformatics.studycalendar.security.AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.KnownAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationPropertyEditor;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.SYSTEM_ADMINISTRATOR)
public class AuthenticationSystemConfigurationController extends PscAbstractCommandController<AuthenticationSystemConfigurationCommand> {
    private AuthenticationSystemConfiguration authenticationSystemConfiguration;

    public AuthenticationSystemConfigurationController() {
        setCrumb(new DefaultCrumb("Configure authentication system"));
        setValidator(new ValidatableValidator());
    }

    @Override
    protected Object getCommand(HttpServletRequest request) throws Exception {
        String requestedAuthSystem = request.getParameter("conf[" + AUTHENTICATION_SYSTEM.getKey() + "].value");
        String custom = request.getParameter("customAuthenticationSystemClass");
        if (!StringUtils.isBlank(custom)) {
            requestedAuthSystem = custom;
        }
        return new AuthenticationSystemConfigurationCommand(
            requestedAuthSystem, authenticationSystemConfiguration, getApplicationContext());
    }

    @Override
    protected boolean suppressBinding(HttpServletRequest request) {
        return !isSubmit(request);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    // mostly copied from BaseCommandController; used instead of initBinder in order to have access to the command
    protected ServletRequestDataBinder createBinder(HttpServletRequest request, Object oCommand) throws Exception {
        AuthenticationSystemConfigurationCommand command = (AuthenticationSystemConfigurationCommand) oCommand;
        ServletRequestDataBinder binder = new ServletRequestDataBinder(oCommand, getCommandName());
        prepareBinder(binder);
        for (ConfigurationProperty<?> property : command.getWorkConfiguration().getProperties().getAll()) {
            binder.registerCustomEditor(Object.class, "conf[" + property.getKey() + "].value",
                new ConfigurationPropertyEditor(property));
        }
        return binder;
    }

    private boolean isSubmit(HttpServletRequest request) {
        return !"GET".equals(request.getMethod());
    }

    @Override
    protected ModelAndView handle(
        AuthenticationSystemConfigurationCommand command, BindException errors,
        HttpServletRequest request, HttpServletResponse response
    ) throws Exception {
        boolean isCustom = command.getWorkConfiguration().isCustomAuthenticationSystem();
        String system = command.getWorkConfiguration().get(AUTHENTICATION_SYSTEM);

        Map<String, Object> model = new HashMap<String, Object>(errors.getModel());
        model.put("knownAuthenticationSystems", KnownAuthenticationSystem.values());
        model.put("authenticationSystemKey", AUTHENTICATION_SYSTEM.getKey());
        model.put("isCustomAuthenticationSystem", isCustom);
        model.put("currentAuthenticationSystemDisplayName",
            isCustom ? system : KnownAuthenticationSystem.valueOf(system).getDisplayName());

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

    public void setAuthenticationSystemConfiguration(AuthenticationSystemConfiguration authenticationSystemConfiguration) {
        this.authenticationSystemConfiguration = authenticationSystemConfiguration;
    }
}
