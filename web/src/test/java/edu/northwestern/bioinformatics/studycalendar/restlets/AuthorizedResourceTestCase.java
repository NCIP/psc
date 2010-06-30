package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.restlet.data.Method;
import org.restlet.resource.Resource;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author John Dzak
 */
public abstract class AuthorizedResourceTestCase<R extends Resource & AuthorizedResource> extends ResourceTestCase<R> {
    private User user;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        user = Fixtures.createUser("josephine");
        setCurrentUser(user);
    }

    protected void setCurrentUser(User u) {
        PscGuard.setCurrentAuthenticationToken(request, new UsernamePasswordAuthenticationToken(
            u, "dc", u.getAuthorities()));
    }

    protected User getCurrentUser() {
        return (User) PscGuard.getCurrentAuthenticationToken(request).getPrincipal();
    }

    @Override
    protected final R createResource() {
        return createAuthorizedResource();
    }

    protected abstract R createAuthorizedResource();

    protected void assertLegacyRolesAllowedForMethod(Method method, Role... roles) {
        doInitOnly();

        Collection<Role> expected = Arrays.asList(roles);
        Collection<Role> actual = getResource().legacyAuthorizedRoles(method);
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

    protected void assertAllLegacyRolesAllowedForMethod(Method method) {
        assertTrue("All roles should be allowed", getResource().legacyAuthorizedRoles(method).isEmpty());
    }
}
