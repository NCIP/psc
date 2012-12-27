/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import gov.nih.nci.cabig.ctms.web.filters.ContextRetainingFilterAdapter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class AuditInfoFilter extends ContextRetainingFilterAdapter {
    @Override
    public void doFilter(
        final ServletRequest request, final ServletResponse response, final FilterChain chain
    ) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        String username = getApplicationSecurityManager().getUserName();
        if (username == null) {
            username = "<not logged in>";
        }

        DataAuditInfo.setLocal(new gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo(username, request
            .getRemoteAddr(), new Date(), httpReq.getRequestURI()));

        chain.doFilter(request, response);
        DataAuditInfo.setLocal(null);
    }

    private ApplicationSecurityManager getApplicationSecurityManager() {
        return (ApplicationSecurityManager) getApplicationContext().getBean("applicationSecurityManager");
    }
}
