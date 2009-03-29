package edu.northwestern.bioinformatics.studycalendar.web;

import java.io.IOException;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import edu.northwestern.bioinformatics.studycalendar.web.tools.FilterAdapter;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;

/**
 * @author Rhett Sutphin
 */
public class AuditInfoFilter extends FilterAdapter {
    @Override
    public void doFilter(
        final ServletRequest request, final ServletResponse response, final FilterChain chain
    ) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        String username = ApplicationSecurityManager.getUserName();
        if (username == null) {
            username = "<not logged in>";
        }

        DataAuditInfo.setLocal(new gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo(username, request
            .getRemoteAddr(), new Date(), httpReq.getRequestURI()));

        chain.doFilter(request, response);
        DataAuditInfo.setLocal(null);
    }

}
