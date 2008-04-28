package edu.northwestern.bioinformatics.studycalendar.security;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.FilterChain;
import java.io.IOException;

/**
 * Proxy which delegates to the appropriate
 * {@link edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem#logoutFilter}
 *
 * @author Rhett Sutphin
 */
public class AuthenticationSystemLogoutFilter implements Filter {
    private AuthenticationSystemConfiguration configuration;

    public void init(FilterConfig filterConfig) throws ServletException { /* not invoked */ }

    public void doFilter(
        ServletRequest request, ServletResponse response, FilterChain filterChain
    ) throws IOException, ServletException {
        configuration.getAuthenticationSystem().logoutFilter()
            .doFilter(request, response, filterChain);
    }

    public void destroy() { /* not invoked */ }

    ////// CONFIGURATION

    public void setAuthenticationSystemConfiguration(AuthenticationSystemConfiguration configuration) {
        this.configuration = configuration;
    }
}
