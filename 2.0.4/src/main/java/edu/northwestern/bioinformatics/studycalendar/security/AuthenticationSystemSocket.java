package edu.northwestern.bioinformatics.studycalendar.security;

import org.acegisecurity.ui.AuthenticationEntryPoint;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.Authentication;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.BeansException;
import org.springframework.core.io.ClassPathResource;

import javax.servlet.Filter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import javax.servlet.FilterConfig;
import javax.servlet.FilterChain;
import java.io.IOException;

import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.LocalAuthenticationSystem;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationListener;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationEvent;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;

/**
 * @author Rhett Sutphin
 */
public class AuthenticationSystemSocket
    implements AuthenticationEntryPoint, Filter, AuthenticationManager, InitializingBean
{
    private AuthenticationSystemConfiguration configuration;

    ////// CONFIGURATION

    @Required
    public void setConfiguration(AuthenticationSystemConfiguration configuration) {
        this.configuration = configuration;
    }

    public void afterPropertiesSet() throws Exception {
        // ensure that initial configuration is valid at startup,
        // instead of waiting for the first request
        getSystem();
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
