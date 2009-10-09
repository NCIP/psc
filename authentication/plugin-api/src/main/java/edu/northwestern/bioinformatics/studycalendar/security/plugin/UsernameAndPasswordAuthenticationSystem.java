package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import static edu.northwestern.bioinformatics.studycalendar.tools.spring.SpringBeanConfigurationTools.prepareBean;
import org.acegisecurity.Authentication;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.ui.AuthenticationEntryPoint;
import org.acegisecurity.ui.webapp.AuthenticationProcessingFilter;
import org.acegisecurity.ui.webapp.AuthenticationProcessingFilterEntryPoint;

import javax.servlet.Filter;

/**
 * Base class for all authentication systems that use the local
 * PSC log in page to receive a username and password.  Subclasses will
 * probably only need to implement {@link #authenticationManager()} or
 * {@link #createAuthenticationManager()}.
 *
 * @author Rhett Sutphin
 */
public abstract class UsernameAndPasswordAuthenticationSystem extends AbstractAuthenticationSystem {
    public static final String LOGIN_FORM_URL = "/public/login";
    public static final String LOGIN_ERROR_URL = "/public/login?login_error=1";
    public static final String LOGIN_TO_ROOT = "/";

    @Override protected AuthenticationEntryPoint createEntryPoint() {
        AuthenticationProcessingFilterEntryPoint entryPoint
            = new AuthenticationProcessingFilterEntryPoint();
        if (usesLocalPasswords()) {
            entryPoint.setLoginFormUrl(LOGIN_FORM_URL);
        } else {
            entryPoint.setLoginFormUrl(LOGIN_TO_ROOT);
        }
        return prepareBean(getApplicationContext(), entryPoint);
    }

    @Override protected Filter createFilter() {
        AuthenticationProcessingFilter filter = new AuthenticationProcessingFilter();
        filter.setAuthenticationManager(authenticationManager());
        filter.setDefaultTargetUrl(DEFAULT_TARGET_PATH);
        filter.setAuthenticationFailureUrl(LOGIN_ERROR_URL);
        filter.setFilterProcessesUrl("/auth/login_form_security_check");
        return prepareBean(getApplicationContext(), filter);
    }

    /**
     * Creates a {@link UsernamePasswordAuthenticationToken}.
     * <p>
     * The Acegi filter used in the GUI will always return an authentication of this form,
     * so the authentication manager provided by any subclass must be prepared to deal with
     * it.
     */
    public final Authentication createUsernamePasswordAuthenticationRequest(String username, String password) {
        System.out.println("---- creating username and password authentication request ");
        return new UsernamePasswordAuthenticationToken(username, password);
    }

    public Authentication createTokenAuthenticationRequest(String token) {
        return null;
    }
}
