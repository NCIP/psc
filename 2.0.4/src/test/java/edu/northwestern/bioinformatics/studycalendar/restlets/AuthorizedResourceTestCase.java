package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import org.restlet.data.Method;
import org.restlet.resource.Resource;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author John Dzak
 */
public abstract class AuthorizedResourceTestCase<R extends Resource & AuthorizedResource> extends ResourceTestCase<R> {
    protected void assertRolesAllowedForMethod(Method method, Role... roles) {
        doInit();

        Collection<Role> expected = Arrays.asList(roles);
        Collection<Role> actual = getResource().authorizedRoles(method);
        // if authorizedRoles == null, that means everything is allowed
        if (actual == null) actual = Arrays.asList(Role.values());

        for (Role role : expected) {
            assertTrue(method.toString() + " for " + role.getDisplayName() + " should be allowed",
                actual.contains(role));
        }

        for (Role role : actual) {
            assertTrue(method.toString() + " for " + role.getDisplayName() + " should not be allowed", expected.contains(role));
        }
    }
}
