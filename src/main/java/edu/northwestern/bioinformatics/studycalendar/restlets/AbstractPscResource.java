package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;
import org.restlet.Context;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class AbstractPscResource extends Resource implements AuthorizedResource {
    private static final Collection<Role> NO_AUTH = Collections.emptySet();

    private Map<Method, Collection<Role>> roleAuthorizations;

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
}
