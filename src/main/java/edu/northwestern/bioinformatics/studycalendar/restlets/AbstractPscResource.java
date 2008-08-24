package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;
import org.restlet.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class AbstractPscResource extends Resource implements AuthorizedResource {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final Collection<Role> NO_AUTH = Collections.emptySet();

    private Map<Method, Collection<Role>> roleAuthorizations;
    private String clientErrorReason;

    public AbstractPscResource() { }
    public AbstractPscResource(Context context, Request request, Response response) { super(context, request, response); }

    public Collection<Role> authorizedRoles(Method method) {
        if (getRoleAuthorizations().containsKey(method)) {
            return getRoleAuthorizations().get(method);
        } else {
            return NO_AUTH;
        }
    }

    protected void setAllAuthorizedFor(Method method) {
        getRoleAuthorizations().put(method, null);
    }

    protected void setAuthorizedFor(Method method, Role... roles) {
        getRoleAuthorizations().put(method, Arrays.asList(roles));
    }

    private Map<Method, Collection<Role>> getRoleAuthorizations() {
        if (roleAuthorizations == null) {
            roleAuthorizations = new HashMap<Method, Collection<Role>>();
        }
        return roleAuthorizations;
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
}
