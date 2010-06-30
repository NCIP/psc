package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.easymock.IExpectationSetters;
import org.restlet.data.Method;
import org.restlet.resource.Resource;

import java.util.Arrays;
import java.util.Collection;

import static org.easymock.EasyMock.expect;

/**
 * @author John Dzak
 */
public abstract class AuthorizedResourceTestCase<R extends Resource & AuthorizedResource> extends ResourceTestCase<R> {
    private User user;
    private UserService userService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        user = Fixtures.createUser("josephine");
        PscGuard.setCurrentAuthenticationToken(request, new UsernamePasswordAuthenticationToken(user, "dc", new Role[] { Role.SUBJECT_COORDINATOR }));
        userService = registerMockFor(UserService.class);
    }

    protected User getCurrentUser() {
        return (User) PscGuard.getCurrentAuthenticationToken(request).getPrincipal();
    }

    @Override
    protected final R createResource() {
        R resource = createAuthorizedResource();
        if (resource instanceof AbstractPscResource) {
            ((AbstractPscResource) resource).setUserService(userService);
        }
        return resource;
    }

    protected abstract R createAuthorizedResource();

    protected IExpectationSetters<User> expectGetCurrentUser() {
        return expect(userService.getUserByName(user.getName())).andReturn(user);
    }

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
