package edu.northwestern.bioinformatics.studycalendar.security.internal;

import edu.northwestern.bioinformatics.studycalendar.security.AuthenticationSystemConfiguration;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.ui.AuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class AuthenticationSystemSocket
    implements AuthenticationEntryPoint, Filter, AuthenticationManager
{
    private AuthenticationSystemConfiguration configuration;

    ////// CONFIGURATION

    @Required
    public void setConfiguration(AuthenticationSystemConfiguration configuration) {
        this.configuration = configuration;
    }

    ////// IMPLEMENTATION OF AuthenticationEntryPoint

    public void commence(ServletRequest request, ServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        getSystem().entryPoint().commence(request, response, authException);
    }

    private AuthenticationSystem getSystem() {
        return configuration.getAuthenticationSystem();
    }

    ////// IMPLEMENTATION OF AuthenticationManager

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return getSystem().authenticationManager().authenticate(authentication);
    }

    ////// IMPLEMENTATION OF Filter

    public void init(FilterConfig filterConfig) throws ServletException { }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (getSystem().filter() == null) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            getSystem().filter().doFilter(servletRequest, servletResponse, filterChain);
        }
    }

    public void destroy() { }
}
