package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ModelAndViewDefiningException;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.HashMap;

import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;

/**
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
public class LoginCheckInterceptor extends HandlerInterceptorAdapter {
    private Logger log = LoggerFactory.getLogger(getClass());
    private ControllerTools controllerTools;

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
        String userName = ApplicationSecurityManager.getUser();
        if (userName == null) {
            if (log.isDebugEnabled()) {
                log.debug("request path " + request.getPathInfo() + " request url " + request.getRequestURI() + " username in interceptor " + userName);
                log.debug("session id: " + request.getSession().getId());
            }

            request.getSession().setAttribute(REQUESTED_URL_ATTRIBUTE, getFullPath(request));

            Map<String, Object> model = new HashMap<String, Object>();
            if (controllerTools.isAjaxRequest(request)) {
                log.debug("Ajax request intercepted");
                model.put("ajax", " ");
            }
            throw new ModelAndViewDefiningException(new ModelAndView("redirectToLogin", model));
        }
        return true;
    }

    private String getFullPath(HttpServletRequest request) {
        StringBuffer fullPath = request.getRequestURL();
        if (request.getQueryString() != null) fullPath.append('?').append(request.getQueryString());
        return fullPath.toString();
    }

    @Required
    public void setControllerTools(ControllerTools controllerTools) {
        this.controllerTools = controllerTools;
    }
}
