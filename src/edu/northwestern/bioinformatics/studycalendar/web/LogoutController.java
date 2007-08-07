package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Jaron Sampson
 */
public class LogoutController implements Controller {
//    private static final Log log = LogFactory.getLog(LoginController.class);
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String username = ApplicationSecurityManager.getUser(request);
        ApplicationSecurityManager.removeUserSession(request);
        log.debug("User " + username + "logged out.");
        return new ModelAndView("redirectToLogin");
    }
}
