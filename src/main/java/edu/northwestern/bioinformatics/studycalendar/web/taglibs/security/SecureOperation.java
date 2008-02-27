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

    @Override
    public int doStartTag() throws JspTagException {
        initializeBeans();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        ConfigAttributeDefinition elementRoles = getElementRoles(getElement());

        try {
            return isAllowed(authentication, getElement(), elementRoles);
        } catch (AuthenticationException e) {
            throw new JspTagException(e);
        }
    }

    @Override
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

    protected int isAllowed(Authentication i_authentication, String i_element, ConfigAttributeDefinition elementRoles) {
        boolean allowed = true;

        try {
            authorizationDecisionManager.decide(i_authentication, i_element, elementRoles);
        } catch (AccessDeniedException ade) {
            allowed = false;
        }
        
        int k;
        if (allowed) {
            k = EVAL_BODY_INCLUDE;
        } else {
            k = SKIP_BODY;
        }
        return k;
    }

    protected ConfigAttributeDefinition getElementRoles(String url) {
        ConfigAttributeDefinition def = definitionMap.lookupAttributes(url);
        log.debug("Roles for {} are {}", url, def);

        if (def == null) {
            log.warn("No matching roles found for " + getElement());
            def = new ConfigAttributeDefinition();
        } 

        return def;
    }

    @Override
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