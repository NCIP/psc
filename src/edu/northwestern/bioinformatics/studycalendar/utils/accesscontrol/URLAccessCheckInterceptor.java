package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;

import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.SecurityServiceProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ModelAndViewDefiningException;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
public class URLAccessCheckInterceptor extends HandlerInterceptorAdapter {
    private static Log log = LogFactory.getLog(URLAccessCheckInterceptor.class);
    private static final String ACCESS_OPERATION = "ACCESS";

    private AuthorizationManager authorizationManager;

    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String userName = ApplicationSecurityManager.getUser(request);
        if (userName != null) {
            String peName = getProtectionElementObjectId(request);
            if (log.isDebugEnabled()) {
                log.debug("request url " + request.getRequestURI() + " username in interceptor " + userName);
                log.debug("pe: " + peName);
                log.debug("session id: " + request.getSession().getId());
            }
            if (!authorizationManager.checkPermission(userName, peName, ACCESS_OPERATION)) {
                ModelAndView mv = new ModelAndView("errorPage");
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
                throw new ModelAndViewDefiningException(mv);
            }
            return true;
        } else {
            return false;
        }
    }

    private String getProtectionElementObjectId(HttpServletRequest request) {
        String baseUri = request.getRequestURI();
        String cp = request.getContextPath();
        if (cp != null) {
            return baseUri.substring(cp.length());
        } else {
            return baseUri;
        }
    }

    @Required
    public void setAuthorizationManager(AuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }
}
