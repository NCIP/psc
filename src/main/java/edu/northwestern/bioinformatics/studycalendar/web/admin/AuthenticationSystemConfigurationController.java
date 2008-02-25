package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.security.AuthenticationSystemConfiguration;
import static edu.northwestern.bioinformatics.studycalendar.security.AuthenticationSystemConfiguration.*;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.KnownAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class AuthenticationSystemConfigurationController extends PscAbstractCommandController<AuthenticationSystemConfigurationCommand> {
    private AuthenticationSystemConfiguration authenticationSystemConfiguration;
    private ControllerTools controllerTools;

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

        if (controllerTools.isAjaxRequest(request)) {
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

    public void setControllerTools(ControllerTools controllerTools) {
        this.controllerTools = controllerTools;
    }
}
