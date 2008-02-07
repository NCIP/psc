package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import org.restlet.data.Method;
import org.restlet.resource.Resource;

import java.util.Collection;

/**
 * @author John Dzak
 */
public abstract class AuthorizedResourceTestCase<R extends Resource & AuthorizedResource> extends ResourceTestCase<R> {
    protected void assertRoleIsAllowedForMethod(Role role, Method method) {
        Collection<Role> roles = getResource().authorizedRoles(method);
        assertTrue("Role is not authorized for method", roles == null || roles.contains(role));
    }
}
