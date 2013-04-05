/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import gov.nih.nci.cabig.ctms.web.filters.ContextRetainingFilterAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class UserInRequestFilter extends ContextRetainingFilterAdapter {
    private static final String USER_ATTRIBUTE = "currentUser";

    private final Logger slog = LoggerFactory.getLogger(getClass()); // since the superclass one is JCL

    @Override
    public void doFilter(
        ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain
    ) throws IOException, ServletException {
        String username = getApplicationSecurityManager().getUserName();
        if (username != null) {
            PscUser user = getApplicationSecurityManager().getUser();
            slog.trace("Adding user {} to request attributes as {}", user, USER_ATTRIBUTE);
            servletRequest.setAttribute(USER_ATTRIBUTE, user);
            // old behavior preserved for backwards compatibility
            servletRequest.setAttribute("user", username);
        } else {
            slog.trace("No authenticated user, so {} not set", USER_ATTRIBUTE);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private ApplicationSecurityManager getApplicationSecurityManager() {
        return (ApplicationSecurityManager) getApplicationContext().getBean("applicationSecurityManager");
    }
}