package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;

import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.SecurityServiceProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ModelAndViewDefiningException;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
public class URLAccessCheckInterceptor extends HandlerInterceptorAdapter {
    private static Log log = LogFactory.getLog(URLAccessCheckInterceptor.class);
    private static final String ACCESS_OPERATION = "ACCESS";
    private static final String APPLICATION_CONTEXT_STRING = "study_calendar";

    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String userName = ApplicationSecurityManager.getUser(request);
        if (userName != null) {
            if (log.isDebugEnabled()) {
                log.debug("request url " + request.getRequestURI() + " username in interceptor " + userName);
                log.debug("session id: " + request.getSession().getId());
            }
            AuthorizationManager authorizationManager
                = SecurityServiceProvider.getAuthorizationManager(APPLICATION_CONTEXT_STRING);
            if (!authorizationManager.checkPermission(userName, request.getRequestURI(), ACCESS_OPERATION)) {
                ModelAndView mv = new ModelAndView("errorPage");
                throw new ModelAndViewDefiningException(mv);
            }
            return true;
        } else {
            return false;
        }
    }
}
