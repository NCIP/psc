package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.utils.FilterAdapter;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.DataAuditInfo;

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
public class AuditInfoFilter extends FilterAdapter {

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        String username = ApplicationSecurityManager.getUser(httpReq);
        if (username != null) {
            DataAuditInfo.setLocal(
                new DataAuditInfo(username, request.getRemoteAddr(), new Date(),
                    httpReq.getRequestURI()));
        }
        chain.doFilter(request, response);
        DataAuditInfo.setLocal(null);
    }
}
