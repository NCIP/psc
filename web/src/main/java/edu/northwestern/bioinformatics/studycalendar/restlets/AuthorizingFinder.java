package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.acegisecurity.Authentication;
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
                PscUser user = (PscUser) token.getPrincipal();
                for (ResourceAuthorization authorization : authorizations) {
                    if (authorization.permits(user)) return true;
                }
                return false;
            }
        } else {
            log.debug("Guarded resource does not provide additional authorization information");
            return true;
        }
    }
}
