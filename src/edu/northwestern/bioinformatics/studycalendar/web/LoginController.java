package edu.northwestern.bioinformatics.studycalendar.web;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractFormController;
import org.springframework.web.servlet.view.RedirectView;

import edu.northwestern.bioinformatics.studycalendar.dao.LoginAuditDao;
import edu.northwestern.bioinformatics.studycalendar.domain.LoginAudit;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.LoginCheckInterceptor;
import gov.nih.nci.security.AuthenticationManager;

/**
 * @author Padmaja Vedula
 * @author Jaron Sampson
 * @author Rhett Sutphin
 */
public class LoginController extends AbstractFormController {
    private static final String DEFAULT_TARGET_VIEW = "/pages/studyList";
    private static final Log log = LogFactory.getLog(LoginController.class);
    private LoginAuditDao loginAuditDao;

    private AuthenticationManager authenticationManager;

    public LoginController() {
        setCommandClass(LoginCommand.class);
        setBindOnNewForm(true);
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return new LoginCommand(authenticationManager, loginAuditDao);
    }

    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors) throws Exception {
        return new ModelAndView(getFormView(request), errors.getModel());
    }

    private String getFormView(HttpServletRequest request) {
        return request.getParameter("ajax") == null ? "login" : "relogin";
    }

    protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        LoginCommand loginCredentials = (LoginCommand) oCommand;
        log.debug("Username: " + loginCredentials.getUsername());
        log.debug("System Config file is: " + System.getProperty("gov.nih.nci.security.configFile"));
        boolean loginSuccess = loginCredentials.login(request.getRemoteAddr());
        
        if (loginSuccess) {
            ApplicationSecurityManager.setUser(request, loginCredentials.getUsername());
            return new ModelAndView(getTargetView(request));
        } else {
            Map<String, Object> model = errors.getModel();
            model.put("failed", true);
            return new ModelAndView(getFormView(request), model);
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
    
    @Required
    public void setLoginAuditDao(LoginAuditDao loginAuditDao) {
        this.loginAuditDao = loginAuditDao;
    }
}

