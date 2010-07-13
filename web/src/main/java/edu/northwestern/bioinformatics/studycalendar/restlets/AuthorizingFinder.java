package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.restlet.Handler;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.spring.SpringBeanFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;

import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
public class AuthorizingFinder extends SpringBeanFinder {
    private Logger log = LoggerFactory.getLogger(getClass());
    @Deprecated private final boolean legacyMode;

    public AuthorizingFinder(BeanFactory beanFactory, String beanName) {
        this(beanFactory, beanName, true);
    }

    @Deprecated
    public AuthorizingFinder(BeanFactory beanFactory, String beanName, boolean legacyMode) {
        super(beanFactory, beanName);
        this.legacyMode = legacyMode;
    }

    @Override
    protected Handler findTarget(Request request, Response response) {
        Handler found = super.findTarget(request, response);
        if (found == null) {
            return null;
        } else if (authorize(found, request)) {
            return found;
        } else {
            response.setStatus(Status.CLIENT_ERROR_FORBIDDEN,
                "Authenticated account is not authorized for this resource and method");
            return null;
        }
    }

    protected boolean authorize(Handler handler, Request request) {
        log.debug("Guarding {}", handler.getClass().getSimpleName());
        if (handler instanceof AuthorizedResource) {
            Authentication token = PscGuard.getCurrentAuthenticationToken(request);
            if (token == null) {
                // this should not be possible
                throw new StudyCalendarSystemException("Cannot authorize an unauthenticated request");
            }
            if (legacyMode) {
                Collection<Role> requiredRoles = ((AuthorizedResource) handler).legacyAuthorizedRoles(request.getMethod());
                if (requiredRoles == null) {
                    log.debug("Guarded resource is open to all authenticated users for {}", request.getMethod());
                    return true;
                } else if (requiredRoles.size() == 0) {
                    log.warn("Guarded resource is not open to any role for {}", request.getMethod());
                    return false;
                } else {
                    return hasOneOfTheseRoles(token.getAuthorities(), requiredRoles);
                }
            } else {
                Collection<ResourceAuthorization> authorizations =
                    ((AuthorizedResource) handler).authorizations(request.getMethod());
                if (authorizations == null) {
                    log.debug("Guarded resource is open to all authenticated users for {}", request.getMethod());
                    return true;
                } else if (authorizations.size() == 0) {
                    log.warn("Guarded resource has no authorizations for {}", request.getMethod());
                    return false;
                } else {
                    PscUser user = (PscUser) token.getPrincipal();
                    for (ResourceAuthorization authorization : authorizations) {
                        if (authorization.permits(user)) return true;
                    }
                    return false;
                }
            }
        } else {
            log.debug("Guarded resource does not provide additional authorization information");
            return true;
        }
    }

    private boolean hasOneOfTheseRoles(GrantedAuthority[] grantedRoles, Collection<Role> requiredRoles) {
        for (GrantedAuthority grantedRole : grantedRoles) {
            if (requiredRoles.contains(grantedRole)) return true;
        }
        return false;
    }
}
