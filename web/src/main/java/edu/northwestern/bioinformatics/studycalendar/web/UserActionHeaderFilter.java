/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.UserActionDao;
import edu.northwestern.bioinformatics.studycalendar.domain.UserAction;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.AuditEvent;
import gov.nih.nci.cabig.ctms.web.filters.ContextRetainingFilterAdapter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author Jalpa Patel
 */
public class UserActionHeaderFilter extends ContextRetainingFilterAdapter {

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
                throws IOException, ServletException {
        String uaHeader = ((HttpServletRequest)request).getHeader("X-PSC-User-Action");
        if (uaHeader != null) {
            String[] ua = uaHeader.split("/");
            String userActionId = ua[ua.length-1];
             if (userActionId != null) {
                UserAction userAction = getUserActionDao().getByGridId(userActionId);
                if (userAction != null) {
                    String currentUserName = getApplicationSecurityManager().getUserName();
                    if (userAction.getUser().getName().equals(currentUserName)) {
                        AuditEvent.setUserAction(userAction);
                    } else {
                        log.debug("UserAction's user '{}' and logged in user '{}' are not the same user.",
                                new Object[] { userAction.getUser().getLoginName(), currentUserName});
                    }
                } else {
                    log.debug("No UserAction found with the grid id {}", new Object[] { userActionId});
                }
            }
        }
        chain.doFilter(request, response);

        AuditEvent.setUserAction(null);
    }

    private ApplicationSecurityManager getApplicationSecurityManager() {
        return (ApplicationSecurityManager) getApplicationContext().getBean("applicationSecurityManager");
    }

    private UserActionDao getUserActionDao() {
        return (UserActionDao) getApplicationContext().getBean("userActionDao");
    }
}
