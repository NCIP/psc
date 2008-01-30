package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.springframework.beans.factory.BeanFactory;
import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;

import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
public class AuthorizingFinder extends SpringBeanFinder {
    private Logger log = LoggerFactory.getLogger(getClass());

    public AuthorizingFinder(BeanFactory beanFactory, String beanName) {
        super(beanFactory, beanName);
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
        if (handler instanceof AuthorizedResource) {
            Authentication token = (Authentication) request.getAttributes().get(PscGuard.AUTH_TOKEN_ATTRIBUTE_KEY);
            Collection<Role> requiredRoles = ((AuthorizedResource) handler).authorizedRoles(request.getMethod());
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
