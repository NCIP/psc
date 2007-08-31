package edu.northwestern.bioinformatics.studycalendar.web.taglibs.security;

import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.SecurityServiceProvider;
import gov.nih.nci.security.acegi.csm.authorization.DelegatingObjectPrivilegeCSMAuthorizationCheck;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.Authentication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.beans.BeansException;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.WebApplicationContext;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;

import java.util.Set;
import java.util.HashSet;


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

    public int doStartTag() throws JspTagException {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        if (log.isDebugEnabled()) log.debug("username   ---------          " + userName);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(pageContext.getServletContext());
        DelegatingObjectPrivilegeCSMAuthorizationCheck authorizationCheck =
                (DelegatingObjectPrivilegeCSMAuthorizationCheck) context.getBean("stringAuthorizationCheck");

        try {
            return isAllowed(authorizationCheck, authentication, getOperation(), getElement());
        } catch (Exception e) {
            log.error("Exception evaluating SecureOperation startTag", e);
            throw new JspTagException(e.getMessage() + " (Rethrown exception; see log for details)");
        }
    }

    public int doEndTag() throws JspTagException {
        return EVAL_PAGE;
    }

    protected int isAllowed(DelegatingObjectPrivilegeCSMAuthorizationCheck i_authorizationCheck, Authentication i_authentication, String i_operation, String i_element) throws Exception {
        boolean allowed = i_authorizationCheck.checkAuthorization(i_authentication, i_operation, i_element);
        
        int k = 0;
        if (!allowed) {
            k = SKIP_BODY;
        } else {
            k = EVAL_BODY_INCLUDE;
        }
        return k;
    }

}