package edu.northwestern.bioinformatics.studycalendar.web.taglibs.security;

import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.SecurityServiceProvider;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.acegisecurity.context.SecurityContextHolder;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;


/**
 * @author Padmaja Vedula
 */

public class SecureOperation extends TagSupport {
//    private static Log log = LogFactory.getLog(SecureOperation.class);
    private static Logger log = LoggerFactory.getLogger(SecureOperation.class);

    private String operation;
    private String element;

    public void setOperation(String val) {
        this.operation = val;
    }

    public String getOperation() {
        return this.operation;
    }

    public void setElement(String val) {
        this.element = val;
    }

    public String getElement() {
        return this.element;
    }
   //TODO: remove this class
    public int doStartTag() throws JspTagException {
        AuthorizationManager authorizationManager = null;

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        if (log.isDebugEnabled()) log.debug("username   ---------          " + userName);
        
        try {
            authorizationManager = SecurityServiceProvider.getAuthorizationManager("study_calendar");
        } catch (Exception e) {
            log.error("Exception when acquiring authorization manager", e);
            throw new JspTagException(e.getMessage() + " (Rethrown exception; see log for details)");
        }

        try {
            return isAllowed(authorizationManager, userName, getElement(), getOperation());
        } catch (Exception e) {
            log.error("Exception evaluating SecureOperation startTag", e);
            throw new JspTagException(e.getMessage() + " (Rethrown exception; see log for details)");
        }
    }

    public int doEndTag() throws JspTagException {
        return EVAL_PAGE;
    }

    private int isAllowed(AuthorizationManager i_authorizationManager, String i_userName, String i_element, String i_operation) throws Exception {
        boolean allowed = i_authorizationManager.checkPermission(i_userName, i_element, i_operation);
        int k = 0;
        if (!allowed) {
            k = SKIP_BODY;
        } else {
            k = EVAL_BODY_INCLUDE;
        }
        return k;
    }

}