package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.FilterAdapter;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class UserInRequestFilter extends FilterAdapter {
    public void init(FilterConfig filterConfig) throws ServletException { }
    public void destroy() { }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String username = ApplicationSecurityManager.getUser((HttpServletRequest) servletRequest);
        if (username != null) servletRequest.setAttribute("user", username);
        filterChain.doFilter(servletRequest, servletResponse);
    }
}