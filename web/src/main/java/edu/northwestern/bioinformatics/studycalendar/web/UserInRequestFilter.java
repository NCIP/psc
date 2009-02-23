package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import gov.nih.nci.cabig.ctms.web.filters.ContextRetainingFilterAdapter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class UserInRequestFilter extends ContextRetainingFilterAdapter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String username = ApplicationSecurityManager.getUser();
        if (username != null) {
            servletRequest.setAttribute("currentUser", getUserDao().getByName(username));
            // old behavior preserved for backwards compatibility
            servletRequest.setAttribute("user", username);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private UserDao getUserDao() {
        return (UserDao) getApplicationContext().getBean("userDao");
    }
}