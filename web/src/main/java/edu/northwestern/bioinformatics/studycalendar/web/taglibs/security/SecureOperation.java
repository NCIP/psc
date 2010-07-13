package edu.northwestern.bioinformatics.studycalendar.web.taglibs.security;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.LegacyModeSwitch;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;


/**
 * @author Padmaja Vedula
 * @author John Dzak
 * @author Rhett Sutphin
 */
public class SecureOperation extends TagSupport {
    private static final String SECURE_SERVLET_PREFIX = "^/pages";

    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final GrantedAuthority[] NO_ROLES = new GrantedAuthority[0];

    private String element;
    private Map<String, GrantedAuthority[]> secureUrls;

    /////// ATTRIBUTES

    public void setElement(String val) {
        this.element = val;
    }

    ////// Tag IMPLEMENTATION

    @Override
    public int doStartTag() throws JspTagException {
        log.trace("Evaluating secureOperation tag for protected element {}", element);

        if (element == null) {
            log.error("Required attribute \"element\" not included on secureOperation tag.  Body will never be displayed.");
            return SKIP_BODY;
        }

        if (isLegacyMode()) {
            return legacyStartTag();
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.trace(" - No user authenticated");
            return SKIP_BODY;
        }
        if (!(authentication.getPrincipal() instanceof PscUser)) {
            log.error("Authenticated principal is wrong kind ({}).  SecureOperation will not show body.", authentication.getPrincipal().getClass().getName());
            return SKIP_BODY;
        }
        PscUser user = (PscUser) authentication.getPrincipal();

        Collection<ResourceAuthorization> authorizations = findAuthorizations();
        if (log.isTraceEnabled()) {
            log.trace(" - {} is open to {}", element, authorizations);
            log.trace(" - user is {}", Arrays.asList(authentication.getAuthorities()));
        }

        for (ResourceAuthorization authorization : authorizations) {
            if (authorization.permits(user)) return EVAL_BODY_INCLUDE;
        }
        return SKIP_BODY;
    }

    @Deprecated
    private int legacyStartTag() {
        init();
        if (secureUrls == null) {
            log.error("No mapping of secure URLs available.  secureOperation body will never be displayed.");
            return SKIP_BODY;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.trace(" - No user authenticated");
            return SKIP_BODY;
        }

        GrantedAuthority[] allowedRoles = findAllowedRoles();
        if (log.isTraceEnabled()) {
            log.trace(" - {} is authorized for {}", element, Arrays.asList(allowedRoles));
            log.trace(" - user is {}", Arrays.asList(authentication.getAuthorities()));
        }

        return inAuthorizedRole(authentication, allowedRoles) ? EVAL_BODY_INCLUDE : SKIP_BODY;
    }

    @Override
    public int doEndTag() throws JspTagException {
        return EVAL_PAGE;
    }

    @SuppressWarnings({ "unchecked" })
    private Map<String, Object> getUrlHandlerMap() {
        AbstractUrlHandlerMapping mapping = getUrlHandlerMapping();
        if (mapping == null) {
            return Collections.emptyMap();
        } else {
            return mapping.getHandlerMap();
        }
    }

    private AbstractUrlHandlerMapping getUrlHandlerMapping() {
        Object mapping = getApplicationContext().getBean("urlMapping");
        if (mapping instanceof AbstractUrlHandlerMapping) {
            return (AbstractUrlHandlerMapping) mapping;
        } else {
            log.error("Handler mapping ({}) is not URL-based.  SecureOperation tag can't work.", mapping);
            return null;
        }
    }

    private Collection<ResourceAuthorization> findAuthorizations() {
        AntPathMatcher matcher = new AntPathMatcher();
        Map<String, Object> map = getUrlHandlerMap();
        String relPath = getServletRelativePath();
        log.trace(" - Looking for a servlet-relative handler for {}", relPath);
        for (String handledPath : map.keySet()) {
            if (matcher.matchStart(handledPath, relPath)) {
                Object handler = map.get(handledPath);
                if (handler instanceof PscAuthorizedHandler) {
                    return ((PscAuthorizedHandler) handler).authorizations(
                        "GET", Collections.<String, String[]>emptyMap());
                } else {
                    log.error("Handler for {} ({}) does not implement PscAuthorizedHandler.  SecureOperation will never show its body.", handledPath, handler);
                    return PscAuthorizedHandler.NONE_AUTHORIZED;
                }
            }
        }
        log.error("No handler found for {}.  SecureOperation will never show its body.", element);
        return PscAuthorizedHandler.NONE_AUTHORIZED;
    }

    private String getServletRelativePath() {
        return element.replaceFirst(SECURE_SERVLET_PREFIX, "");
    }

    @Deprecated
    @SuppressWarnings({ "unchecked" })
    private void init() {
        if (secureUrls == null) {
            if (getApplicationContext().containsBean("secureUrls")) {
                secureUrls = (Map<String, GrantedAuthority[]>) getApplicationContext().getBean("secureUrls");
            }
        }
    }

    @Deprecated
    private GrantedAuthority[] findAllowedRoles() {
        AntPathMatcher matcher = new AntPathMatcher();
        for (String path : secureUrls.keySet()) {
            if (matcher.matchStart(path, element)) return secureUrls.get(path);
        }
        return NO_ROLES;
    }

    @Deprecated
    private boolean inAuthorizedRole(Authentication authentication, GrantedAuthority[] allowed) {
        for (GrantedAuthority role : allowed) {
            for (GrantedAuthority a : authentication.getAuthorities()) {
                if (role.getAuthority().equals(a.getAuthority())) return true;
            }
        }
        return false;
    }

    @Deprecated
    private boolean isLegacyMode() {
        return ((LegacyModeSwitch) getApplicationContext().getBean("authorizationLegacyModeSwitch")).isOn();
    }

    private WebApplicationContext getApplicationContext() {
        return (WebApplicationContext) pageContext.getRequest().
            getAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE);
    }
}