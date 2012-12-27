/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.internal;

import edu.northwestern.bioinformatics.studycalendar.security.AuthenticationSystemConfiguration;

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
    private Filter defaultLogoutFilter;

    public void init(FilterConfig filterConfig) throws ServletException { /* not invoked */ }

    public void doFilter(
        ServletRequest request, ServletResponse response, FilterChain filterChain
    ) throws IOException, ServletException {
        getEffectiveFilter().doFilter(request, response, filterChain);
    }

    private Filter getEffectiveFilter() {
        Filter explicit = configuration.getAuthenticationSystem().logoutFilter();
        return explicit == null ? defaultLogoutFilter : explicit;
    }

    public void destroy() { /* not invoked */ }

    ////// CONFIGURATION

    public void setAuthenticationSystemConfiguration(AuthenticationSystemConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setDefaultLogoutFilter(Filter defaultLogoutFilter) {
        this.defaultLogoutFilter = defaultLogoutFilter;
    }
}
