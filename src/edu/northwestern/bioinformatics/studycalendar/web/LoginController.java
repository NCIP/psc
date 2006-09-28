package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.LoginCheckInterceptor;
import gov.nih.nci.security.AuthenticationManager;
import gov.nih.nci.security.exceptions.CSException;
import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ModelAndViewDefiningException;
import org.springframework.web.servlet.mvc.AbstractFormController;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.beans.factory.annotation.Required;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Padmaja Vedula
 * @author Jaron Sampson
 */
public class LoginController extends AbstractFormController {
    private static final String DEFAULT_TARGET_VIEW = "/pages/studyList";
    private static final Logger log = Logger.getLogger(LoginController.class.getName());

    private AuthenticationManager authenticationManager;

    public LoginController() {
        setCommandClass(LoginCommand.class);
        setBindOnNewForm(true);
    }
    
    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors) throws Exception {
        String userName = ApplicationSecurityManager.getUser(request);
        if (userName != null) {
            log.debug("UserName: " + userName + "already logged in. Redirecting.");        	
            throw new ModelAndViewDefiningException(new ModelAndView("redirectToStudyList"));
        }
        Map<String, Object> model = errors.getModel();
        LoginCommand command = new LoginCommand();
        model.put("command", command);
        return new ModelAndView("login", model);
    }

    
    protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        LoginCommand loginCredentials = (LoginCommand) oCommand;
        log.debug("Login ID: " + loginCredentials.getUserId());
        log.debug("System Config file is: "
            + System.getProperty("gov.nih.nci.security.configFile"));

        // check login credentials using Authentication Manager of CSM
        boolean loginSuccess;
        try {
            loginSuccess = authenticationManager.login(
                loginCredentials.getUserId(), loginCredentials.getPassword());
        } catch (CSException ex) {
            loginSuccess = false;
            log.debug("The user was denied access to the study calendar application.", ex);
        }

        if (loginSuccess) {
            ApplicationSecurityManager.setUser(request, loginCredentials.getUserId());
            log.debug("Login successful : " + loginCredentials.getUserId() + "session id: " + request.getSession().getId());
            return new ModelAndView(getTargetView(request));
        } else {
            // have to add an error page or redirect to login page with error msg
            return new ModelAndView("login");
        }
    }

    private RedirectView getTargetView(HttpServletRequest request) {
        String targetUrl = LoginCheckInterceptor.getRequestedUrlOnce(request.getSession());

        if (targetUrl == null) {
            return new RedirectView(DEFAULT_TARGET_VIEW, true);
        } else {
            return new RedirectView(targetUrl);
        }
    }

    ////// CONFIGURATION

    @Required
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

}

