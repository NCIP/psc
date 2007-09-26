package edu.northwestern.bioinformatics.studycalendar.web.sso;

import javax.servlet.http.HttpServletRequest;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.context.SecurityContextHolder;

import gov.nih.nci.cabig.ctms.web.sso.DefaultGridProxyRetrievalStrategy;
import gov.nih.nci.cabig.ctms.web.sso.Utils;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;

/**
 * @author <a href="mailto:joshua.phillips@semanticbits.com>Joshua Phillips</a>
 * @author Rhett Sutphin
 */
public class StudyCalendarGridProxyRetrievalStrategy extends DefaultGridProxyRetrievalStrategy {
//    private static final Log log = LogFactory.getLog(StudyCalendarGridProxyRetrievalStrategy.class);
    private static final Logger log = LoggerFactory.getLogger(StudyCalendarGridProxyRetrievalStrategy.class);

    @Override
    public String processRequest(HttpServletRequest request) {
        String proxy = super.processRequest(request);
        if (proxy != null) {
            log.debug("Proxy found. Getting identity...");
            try {
                String identity = Utils.getGridIdentity(proxy);
                log.debug("Identity is: " + identity);
                
                //TODO: This is a hack. Should examine how to use group-based
                //authorization.
                int idx = identity.lastIndexOf('=');
                String localUserId = identity.substring(idx + 1);
                UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(localUserId, "");
                SecurityContextHolder.getContext().setAuthentication(authRequest);
            } catch (Exception ex) {
                log.warn("Couldn't get identity from proxy string", ex);
            }
        } else {
            log.debug("No proxy found.");
        }
        return proxy;
    }
}
