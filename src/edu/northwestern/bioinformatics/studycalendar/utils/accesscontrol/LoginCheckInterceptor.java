package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ModelAndViewDefiningException;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
public class LoginCheckInterceptor extends HandlerInterceptorAdapter {
    private static Log log = LogFactory.getLog(LoginCheckInterceptor.class);

    public static final String REQUESTED_URL_ATTRIBUTE = LoginCheckInterceptor.class.getName() + ".REQUESTED_URL";

    /**
     * Retrieves the URL requested by the user when he or she was not logged in and then
     * immediately clears it from the session.
     *
     * @param session
     * @return the requested URL
     */
    public static String getRequestedUrlOnce(HttpSession session) {
        String requestedUrl = (String) session.getAttribute(REQUESTED_URL_ATTRIBUTE);
        session.setAttribute(REQUESTED_URL_ATTRIBUTE, null);
        return requestedUrl;
    }

    public boolean preHandle(
        HttpServletRequest request, HttpServletResponse response, Object handler
    ) throws Exception {
        String userName = ApplicationSecurityManager.getUser(request);
        if (userName == null) {
            if (log.isDebugEnabled()) {
                log.debug("request path " + request.getPathInfo() + " request url " + request.getRequestURI() + " username in interceptor " + userName);
                log.debug("session id: " + request.getSession().getId());
            }

            request.getSession().setAttribute(REQUESTED_URL_ATTRIBUTE, getFullPath(request));

            throw new ModelAndViewDefiningException(new ModelAndView("redirectToLogin"));
        }
        return true;
    }

    private String getFullPath(HttpServletRequest request) {
        StringBuffer fullPath = request.getRequestURL();
        if (request.getQueryString() != null) fullPath.append('?').append(request.getQueryString());
        return fullPath.toString();
    }
}
