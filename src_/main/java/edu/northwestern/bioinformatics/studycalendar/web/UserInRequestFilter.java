package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.FilterAdapter;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.FilterChain;
import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class UserInRequestFilter extends FilterAdapter {
    public void init(FilterConfig filterConfig) throws ServletException { }
    public void destroy() { }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String username = ApplicationSecurityManager.getUser();
        if (username != null) servletRequest.setAttribute("user", username);
        filterChain.doFilter(servletRequest, servletResponse);
    }
}