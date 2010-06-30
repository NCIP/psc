package edu.northwestern.bioinformatics.studycalendar.restlets;

import com.noelios.restlet.http.HttpResponse;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import org.acegisecurity.Authentication;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;
import org.restlet.util.Series;
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
public class AbstractPscResource extends Resource implements AuthorizedResource {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final ThreadLocal<DateFormat> API_DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override protected DateFormat initialValue() { return new SimpleDateFormat("yyyy-MM-dd"); }
    };

    private static final Collection<Role> NO_AUTH = Collections.emptySet();

    private Map<Method, Collection<Role>> legacyRoleAuthorizations;
    private String clientErrorReason;

    public AbstractPscResource() { }
    public AbstractPscResource(Context context, Request request, Response response) { super(context, request, response); }

    public Collection<Role> legacyAuthorizedRoles(Method method) {
        if (getLegacyRoleAuthorizations().containsKey(method)) {
            return getLegacyRoleAuthorizations().get(method);
        } else {
            return NO_AUTH;
        }
    }

    protected void setAllAuthorizedFor(Method method) {
        getLegacyRoleAuthorizations().put(method, null);
    }

    protected void setAuthorizedFor(Method method, Role... roles) {
        getLegacyRoleAuthorizations().put(method, Arrays.asList(roles));
    }

    private Map<Method, Collection<Role>> getLegacyRoleAuthorizations() {
        if (legacyRoleAuthorizations == null) {
            legacyRoleAuthorizations = new HashMap<Method, Collection<Role>>();
        }
        return legacyRoleAuthorizations;
    }

    protected PscUser getCurrentUser() {
        Authentication token = PscGuard.getCurrentAuthenticationToken(getRequest());
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

    @Deprecated
    protected User getLegacyCurrentUser() {
        PscUser current = getCurrentUser();
        return current == null ? null : current.getLegacyUser();
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        // default to no caching
        Series<Parameter> headers = ((HttpResponse) response).getHttpCall().getResponseHeaders();
        headers.add("Cache-Control", "no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
        headers.add("Pragma", "no-cache");
    }

    @Override
    public void handleGet() {
        super.handleGet();
        defaultErrorResponse();
    }

    @Override
    public void handlePost() {
        super.handlePost();
        defaultErrorResponse();
    }

    @Override
    public void handlePut() {
        super.handlePut();
        defaultErrorResponse();
    }

    @Override
    public void handleDelete() {
        super.handleDelete();
        defaultErrorResponse();
    }

    // TODO: maybe there's an Application-level way to do this instead.
    private void defaultErrorResponse() {
        if (getResponse().getStatus().isClientError() && getResponse().getEntity() == null) {
            getResponse().setEntity(new StringRepresentation(
                createDefaultClientErrorEntity(getResponse().getStatus()), MediaType.TEXT_PLAIN));
        }
    }

    private StringBuilder createDefaultClientErrorEntity(Status status) {
        StringBuilder message = new StringBuilder().
            append(status.getCode()).append(' ').append(status.getName());
        if (status.getDescription() != null) {
            message.append("\n\n").append(status.getDescription());
        }
        if (clientErrorReason != null) {
            message.append("\n\n").append(clientErrorReason);
        }
        message.append('\n');
        return message;
    }

    /**
     * Allows subclasses to expand upon the reason why the request failed.  E.g., "no amendment with
     * that key," etc.
     *
     * @param reason
     */
    protected void setClientErrorReason(String reason, String... params) {
        if (reason != null) {
            clientErrorReason = String.format(reason, (Object[]) params);
            log.debug("client error: {}", clientErrorReason);
        } else {
            clientErrorReason = null;
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
