package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.acegisecurity.Authentication;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.ext.spring.SpringBeanFinder;
import org.restlet.resource.Handler;
import org.restlet.routing.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;

import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
public class AuthorizingFinder extends SpringBeanFinder {
    private Logger log = LoggerFactory.getLogger(getClass());

    public AuthorizingFinder(Router router, BeanFactory beanFactory, String beanName) {
        super(router, beanFactory, beanName);
    }

    @Override
    public Handler findTarget(Request request, Response response) {
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
            Collection<ResourceAuthorization> authorizations =
                ((AuthorizedResource) handler).authorizations(request.getMethod());
            if (authorizations == null) {
                log.debug("Guarded resource is open to all authenticated users for {}", request.getMethod());
                return true;
            } else if (authorizations.size() == 0) {
                log.warn("Guarded resource has no authorizations for {}", request.getMethod());
                return false;
            } else {
                log.debug("Authorizations for this resource are {}", authorizations);
                PscUser user = (PscUser) token.getPrincipal();
                for (ResourceAuthorization authorization : authorizations) {
                    if (authorization.permits(user)) {
                        log.debug("User {} permitted under {}", user, authorization);
                        return true;
                    }
                }
                log.debug("No resource authorizations fit {} ({})",
                    user, user.getMemberships().values());
                return false;
            }
        } else {
            log.debug("Guarded resource does not provide additional authorization information");
            return true;
        }
    }

    /*
    @Override
    public ServerResource find(Request request, Response response) {
        // No PSC resources are ServerResources yet
        return null;
    }
    */
}
