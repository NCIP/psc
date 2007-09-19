package edu.northwestern.bioinformatics.studycalendar.web.taglibs.security;

import org.acegisecurity.*;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.intercept.ObjectDefinitionSource;
import org.acegisecurity.intercept.web.FilterSecurityInterceptor;
import org.acegisecurity.intercept.web.PathBasedFilterInvocationDefinitionMap;
import org.acegisecurity.vote.AbstractAccessDecisionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.Iterator;


/**
 * @author Padmaja Vedula
 * @author John Dzak
 */

public class SecureOperation extends TagSupport {
    private static Logger log = LoggerFactory.getLogger(SecureOperation.class);

    private String element;
    private AbstractAccessDecisionManager authorizationDecisionManager;
    private PathBasedFilterInvocationDefinitionMap definitionMap;

    public void setElement(String val) {
        this.element = val;
    }

    public String getElement() {
        return this.element;
    }

    public int doStartTag() throws JspTagException {
        initializeBeans();
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        if (log.isDebugEnabled()) log.debug("username   ---------          " + userName);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        ConfigAttributeDefinition elementRoles = getElementRoles();

        try {
            return isAllowed(authentication, getElement(), elementRoles);
        } catch (Exception e) {
            log.error("Exception evaluating SecureOperation startTag", e);
            throw new JspTagException(e.getMessage() + " (Rethrown exception; see log for details)");
        }
    }

    public int doEndTag() throws JspTagException {
        return EVAL_PAGE;
    }

    protected void initializeBeans() {
        // TODO: Write bean injector for tag files
        ApplicationContext applicationContext =
                WebApplicationContextUtils.getRequiredWebApplicationContext(pageContext.getServletContext());

        if (authorizationDecisionManager == null) {
            authorizationDecisionManager = (AbstractAccessDecisionManager) applicationContext.getBean("accessDecisionManager");
        }

        if (definitionMap == null) {
            FilterSecurityInterceptor securityInterceptor =
                    (FilterSecurityInterceptor) applicationContext.getBean("filterInvocationInterceptor");

            ObjectDefinitionSource definitionSource = securityInterceptor.getObjectDefinitionSource();

            // TODO: Store security path and role information in ObjectDefinitionSource implementer w/o FilterInvocation requirements
            if(!(definitionSource instanceof PathBasedFilterInvocationDefinitionMap)) {
                throw new UnsupportedOperationException("ObjectDefinitionSource for FilterInvocationInceptor is not instance of PathBasedFilterInvocationDefinitionMap");
            }

            definitionMap = (PathBasedFilterInvocationDefinitionMap) definitionSource;
        }
    }

    protected int isAllowed(Authentication i_authentication, String i_element, ConfigAttributeDefinition elementRoles) throws Exception {
        boolean allowed = true;

        try {
            authorizationDecisionManager.decide(i_authentication, i_element, elementRoles);
        } catch (AccessDeniedException ade) {
            allowed = false;
        }
        
        int k = 0;
        if (!allowed) {
            k = SKIP_BODY;
        } else {
            k = EVAL_BODY_INCLUDE;
        }
        return k;
    }

    protected ConfigAttributeDefinition getElementRoles() {
        ConfigAttributeDefinition def = definitionMap.lookupAttributes(element);

        if (def.size() > 1) {
            log.warn("Method getElementRoles: More than one ConfigAttributeDefinitions defined for " + getElement());
        }

        return def;
    }



    public void release() {
        super.release();
        authorizationDecisionManager = null;
        definitionMap = null;
    }

    // Configuration

    public void setAuthorizationDecisionManager(AbstractAccessDecisionManager authorizationDecisionManager) {
        this.authorizationDecisionManager = authorizationDecisionManager;
    }

    public void setDefinitionMap(PathBasedFilterInvocationDefinitionMap definitionMap) {
        this.definitionMap = definitionMap;
    }
}