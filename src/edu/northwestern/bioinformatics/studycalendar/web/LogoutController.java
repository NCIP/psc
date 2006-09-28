package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.mvc.Controller;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author Jaron Sampson
 */
 
public class LogoutController implements Controller {
    private static final Logger log = Logger.getLogger(LoginController.class.getName());

    public LogoutController() {
    }
    
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	String userName = ApplicationSecurityManager.getUser(request);	    
    	log.info("UserName: " + userName + "logged out.");        		    
	    ApplicationSecurityManager.removeUserSession(request);	
	    return new ModelAndView("redirectToLogin");
    }
}
