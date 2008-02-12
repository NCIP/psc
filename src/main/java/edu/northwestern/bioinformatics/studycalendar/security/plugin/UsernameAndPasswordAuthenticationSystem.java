package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import org.acegisecurity.ui.webapp.AuthenticationProcessingFilter;
import org.acegisecurity.ui.webapp.AuthenticationProcessingFilterEntryPoint;
import org.acegisecurity.ui.AuthenticationEntryPoint;

import javax.servlet.Filter;

import static edu.northwestern.bioinformatics.studycalendar.tools.spring.SpringBeanConfigurationTools.*;

/**
 * Base class for all authentication systems that use the local
 * PSC log in page to receive a username and password.  Subclasses will
 * probably only need to implement {@link #authenticationManager()}.
 *
 * @author Rhett Sutphin
 */
public abstract class UsernameAndPasswordAuthenticationSystem extends AbstractAuthenticationSystem {
    public static final String LOGIN_FORM_URL = "/public/login";
    public static final String LOGIN_ERROR_URL = "/public/login?login_error=1";

    @Override protected AuthenticationEntryPoint createEntryPoint() {
        AuthenticationProcessingFilterEntryPoint entryPoint
            = new AuthenticationProcessingFilterEntryPoint();
        entryPoint.setLoginFormUrl(LOGIN_FORM_URL);
        return prepareBean(getApplicationContext(), entryPoint);
    }

    @Override protected Filter createFilter() {
        AuthenticationProcessingFilter filter = new AuthenticationProcessingFilter();
        filter.setAuthenticationManager(authenticationManager());
        filter.setDefaultTargetUrl(DEFAULT_TARGET_PATH);
        filter.setAuthenticationFailureUrl(LOGIN_ERROR_URL);
        return prepareBean(getApplicationContext(), filter);
    }
}
