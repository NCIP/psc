package edu.northwestern.bioinformatics.studycalendar.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

import gov.nih.nci.security.AuthenticationManager;
import gov.nih.nci.security.SecurityServiceProvider;
import gov.nih.nci.security.exceptions.CSException;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.LocalUser;

import org.apache.log4j.Logger;


/**
 * @author Padmaja Vedula
 */
public class LoginController extends SimpleFormController {
	
    static final Logger log = Logger.getLogger(LoginController.class.getName());
    static AuthenticationManager authMgr = null;
    public static final String CSM_STUDYCAL_CONTEXT_NAME = "study_calendar";


    public LoginController() {
        setCommandClass(LoginCommand.class);
        setFormView("login");
        //setSuccessView("/pages/studyList");
        setBindOnNewForm(true);
        // LocalUser.release(); // will be moved to logout later
    }
    
    protected ModelAndView onSubmit(Object loginData) throws Exception{
	
        LoginCommand loginCredentials = (LoginCommand) loginData;
        log.debug("Login ID: " + loginCredentials.getUserId());
        log.debug("System Config file is: "
                + System.getProperty("gov.nih.nci.security.configFile"));

        //check login credentials using Authentication Manager of CSM
        boolean loginSuccess = false;
        try {
            loginSuccess = getAuthenticationManager().login(
                    loginCredentials.getUserId(), loginCredentials.getPassword());
        } catch (CSException ex) {
            loginSuccess = false;
            log.debug("The user was denied access to the study calendar application." + ex);
        }
        if (loginSuccess) {
        	LocalUser.init(loginCredentials.getUserId());
        	log.debug("Login successful : "+loginCredentials.getUserId());
            return new ModelAndView(new RedirectView("/pages/studyList", true));
        } else {
            // have to add an error page or redirect to login page with error msg
            loginCredentials = new LoginCommand();
            return new ModelAndView(new RedirectView(getFormView()));
        }
    }

    /**
     * Returns the AuthenticationManager for the csm authentication. This method follows the
     * singleton pattern so that only one AuthenticationManager is created for
     * the app.
     * 
     * @return
     * @throws CSException
     */
    protected AuthenticationManager getAuthenticationManager()
        throws CSException {
        if (authMgr == null) {
            synchronized (LoginController.class) {
                if (authMgr == null) {
                    authMgr = SecurityServiceProvider
                    .getAuthenticationManager(CSM_STUDYCAL_CONTEXT_NAME);
                }
            }
        }
        log.debug("AuthenticationManager Class||"+authMgr);
        return authMgr;
    }
}

