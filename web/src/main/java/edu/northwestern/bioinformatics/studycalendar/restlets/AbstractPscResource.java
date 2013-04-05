/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.acegisecurity.Authentication;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class AbstractPscResource extends ServerResource implements AuthorizedResource {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /** For error messages and the like */
    protected static final String API_DATE_FORMAT_STRING = "yyyy-MM-dd";
    private static final ThreadLocal<DateFormat> API_DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() { 
            return new SimpleDateFormat(API_DATE_FORMAT_STRING);
        }
    };

    private static final Collection<ResourceAuthorization> NONE_AUTHORIZED = Collections.emptySet();

    private Map<Method, Collection<ResourceAuthorization>> authorizations;

    public AbstractPscResource() { }

    public Collection<ResourceAuthorization> authorizations(Method method) {
        if (getAuthorizationsByMethod().containsKey(method)) {
            return getAuthorizationsByMethod().get(method);
        } else {
            return NONE_AUTHORIZED;
        }
    }

    protected void setAllAuthorizedFor(Method method) {
        getAllowedMethods().add(method);
        getAuthorizationsByMethod().put(method, null);
    }

    protected void addAuthorizationsFor(Method method, PscRole... roles) {
        addAuthorizationsFor(method, ResourceAuthorization.createSeveral(roles));
    }

    protected void addAuthorizationsFor(Method method, Site site, PscRole... roles) {
        addAuthorizationsFor(method, ResourceAuthorization.createSeveral(site, roles));
    }

    protected void addAuthorizationsFor(Method method, Site site, Study study, PscRole... roles) {
        addAuthorizationsFor(method, ResourceAuthorization.createSeveral(site, study, roles));
    }

    protected void addAuthorizationsFor(Method method, ResourceAuthorization... authorizations) {
        addAuthorizationsFor(method, Arrays.asList(authorizations));
    }

    protected void addAuthorizationsFor(Method method, Collection<ResourceAuthorization> authorizations) {
        getAllowedMethods().add(method);
        getAuthorizationsByMethod().put(method, authorizations);
    }

    private Map<Method, Collection<ResourceAuthorization>> getAuthorizationsByMethod() {
        if (authorizations == null) {
            authorizations = new HashMap<Method, Collection<ResourceAuthorization>>();
        }
        return authorizations;
    }

    protected PscUser getCurrentUser() {
        Authentication token = PscAuthenticator.getCurrentAuthenticationToken(getRequest());
        if (token == null) {
            return null;
        } else if (token.getPrincipal() instanceof PscUser) {
            return (PscUser) token.getPrincipal();
        } else {
            throw new ClassCastException(
                "PSC's Principal is expected to always be a " + PscUser.class.getName() +
                    ".  Right now it is a " + token.getPrincipal().getClass().getName() + '.');
        }
    }

    @Override
    public Representation handle() {
        if (isAuthorized()) {
            return super.handle();
        } else {
            getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,
                "Authenticated account is not authorized for this resource and method");
            return null;
        }
    }

    protected boolean isAuthorized() {
        log.debug("Authorizing {}", getClass().getSimpleName());
        Authentication token = PscAuthenticator.getCurrentAuthenticationToken(getRequest());
        if (token == null) {
            // this should not be possible
            throw new StudyCalendarSystemException("Cannot authorize an unauthenticated request");
        }
        Collection<ResourceAuthorization> authList = authorizations(getRequest().getMethod());
        if (authList == null) {
            log.debug("Resource is open to all authenticated users for {}",
                getRequest().getMethod());
            return true;
        } else if (authList.size() == 0) {
            log.warn("Resource {} has no authorizations for {}",
                getClass().getSimpleName(), getRequest().getMethod());
            return false;
        } else {
            log.debug("Authorizations for this resource are {}", authList);
            PscUser user = (PscUser) token.getPrincipal();
            for (ResourceAuthorization authorization : authList) {
                if (authorization.permits(user)) {
                    log.debug("User {} permitted under {}", user, authorization);
                    return true;
                }
            }
            log.debug("No resource authorizations fit {} ({})",
                user, user.getMemberships().values());
            return false;
        }
    }

    @Override
    public void updateAllowedMethods() {
        // Do nothing.  The default impl resets the methods from the annotations,
        // but we're not using annotations.
    }

    @Override
    // The default one does too much stuff.  The parts of that stuff PSC needs are in
    // PscStatusService.
    protected void doCatch(Throwable throwable) {
        Status status = getStatusService().getStatus(throwable, this);

        if (getResponse() != null) {
            getResponse().setStatus(status);
        }
    }

    /**
     * Allows subclasses to expand upon the reason why the request failed.  E.g., "no amendment with
     * that key," etc.
     *
     * @param reason
     */
    protected void setClientErrorReason(String reason, String... params) {
        if (reason != null) {
            String clientErrorReason = String.format(reason, (Object[]) params);
            getRequest().getAttributes().put(
                PscStatusService.CLIENT_ERROR_REASON_KEY, clientErrorReason);
            log.debug("client error: {}", clientErrorReason);
        } else {
            getRequest().getAttributes().remove(PscStatusService.CLIENT_ERROR_REASON_KEY);
        }
    }

    public static DateFormat getApiDateFormat() {
        return API_DATE_FORMAT.get();
    }

    public String getApplicationBaseUrl() {
        String baseURL = (getRequest().getRootRef().toString().split("/api/v1"))[0];
        return baseURL;
    }
}
