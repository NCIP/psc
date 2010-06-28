package edu.northwestern.bioinformatics.studycalendar.web.taglibs.security;

import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.Arrays;
import java.util.Map;


/**
 * @author Padmaja Vedula
 * @author John Dzak
 * @author Rhett Sutphin
 */
public class SecureOperation extends TagSupport {
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
        init();
        if (secureUrls == null) {
            log.error("No mapping of secure URLs available.  secureOperation body will never be displayed.");
            return SKIP_BODY;
        } else if (element == null) {
            log.error("Required attribute \"element\" not included on secureOperation tag.  Body will never be displayed.");
            return SKIP_BODY;
        }

        log.trace("Evaluating secureOperation tag for protected element {}", element);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.trace(" - No user authenticated");
            return SKIP_BODY;
        }

        GrantedAuthority[] allowedRoles = findAllowedRoles();
        if (log.isTraceEnabled()) {
            log.trace(" - {} is open to {}", element, Arrays.asList(allowedRoles));
            log.trace(" - user is {}", Arrays.asList(authentication.getAuthorities()));
        }

        return inAuthorizedRole(authentication, allowedRoles) ? EVAL_BODY_INCLUDE : SKIP_BODY;
    }

    @Override
    public int doEndTag() throws JspTagException {
        return EVAL_PAGE;
    }

    @SuppressWarnings({ "unchecked" })
    private void init() {
        if (secureUrls == null) {
            if (getApplicationContext().containsBean("secureUrls")) {
                secureUrls = (Map<String, GrantedAuthority[]>) getApplicationContext().getBean("secureUrls");
            }
        }
    }

    private GrantedAuthority[] findAllowedRoles() {
        AntPathMatcher matcher = new AntPathMatcher();
        for (String path : secureUrls.keySet()) {
            if (matcher.matchStart(path, element)) return secureUrls.get(path);
        }
        return NO_ROLES;
    }

    private boolean inAuthorizedRole(Authentication authentication, GrantedAuthority[] allowed) {
        for (GrantedAuthority role : allowed) {
            for (GrantedAuthority a : authentication.getAuthorities()) {
                if (role.getAuthority().equals(a.getAuthority())) return true;
            }
        }
        return false;
    }

    private WebApplicationContext getApplicationContext() {
        return (WebApplicationContext) pageContext.getRequest().
            getAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE);
    }
}