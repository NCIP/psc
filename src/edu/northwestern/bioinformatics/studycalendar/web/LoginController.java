package edu.northwestern.bioinformatics.studycalendar.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractFormController;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

import edu.northwestern.bioinformatics.studycalendar.dao.auditing.LoginAuditDao;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import gov.nih.nci.security.AuthenticationManager;

/**
 * @author Padmaja Vedula
 * @author Jaron Sampson
 * @author Rhett Sutphin
 */
public class LoginController extends AbstractController {
    private static final String DEFAULT_TARGET_VIEW = "/pages/cal/studyList";
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);


    private String getFormView(HttpServletRequest request) {
        return request.getParameter("ajax") == null ? "login" : "relogin";
    }

    private RedirectView getTargetView(HttpServletRequest request) {
        // TODO: Use new security intercepter to get originally requested URL
        //String targetUrl = LoginCheckInterceptor.getRequestedUrlOnce(request.getSession());
        String targetUrl = null;

        if (targetUrl == null) {
            return new RedirectView(DEFAULT_TARGET_VIEW, true);
        } else {
            return new RedirectView(targetUrl);
        }
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        return new ModelAndView(getFormView(httpServletRequest));  //To change body of implemented methods use File | Settings | File Templates.
    }
}

