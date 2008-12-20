package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import static edu.northwestern.bioinformatics.studycalendar.domain.Role.*;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import static org.easymock.EasyMock.expect;
import static org.restlet.data.Method.*;

import java.util.Collection;

/**
 * Tests for functionality implemented in {@link AbstractPscResource}
 *
 * @author Rhett Sutphin
 */
public class PscResourceTest extends AuthorizedResourceTestCase<PscResourceTest.TestResource> {
    private UserService userService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        userService = registerMockFor(UserService.class);
    }

    @Override
    protected TestResource createAuthorizedResource() {
        TestResource testResource = new TestResource();
        testResource.setUserService(userService);
        return testResource;
    }

    public void testAllAuthorizedRoleReturnsNull() throws Exception {
        assertNull(getResource().authorizedRoles(GET));
    }

    public void testProperRolesReturnedForLimitedAuthorizationResources() throws Exception {
        Collection<Role> putRoles = getResource().authorizedRoles(PUT);
        assertEquals(1, putRoles.size());
        assertEquals(SYSTEM_ADMINISTRATOR, putRoles.iterator().next());

        Collection<Role> postRoles = getResource().authorizedRoles(POST);
        assertEquals(2, postRoles.size());
        assertContains(postRoles, STUDY_ADMIN);
        assertContains(postRoles, STUDY_COORDINATOR);
    }
    
    public void testNoRolesReturnedForUnmentionedMethods() throws Exception {
        Collection<Role> lockRoles = getResource().authorizedRoles(LOCK);
        assertNotNull(lockRoles);
        assertEquals(0, lockRoles.size());
    }

    public void testCurrentUserCanBeLoadedWhenThereIsAnAuthentication() throws Exception {
        User expected = new User();
        assertNotNull("Test setup failure", PscGuard.getCurrentAuthenticationToken(request));
        expect(userService.getUserByName(getCurrentUser().getName())).andReturn(expected);

        replayMocks();
        assertSame(expected, getResource().getCurrentUser());
        verifyMocks();
    }

    public void testCurrentUserIsOnlyLoadedOnce() throws Exception {
        User expected = new User();
        assertNotNull("Test setup failure", PscGuard.getCurrentAuthenticationToken(request));
        expect(userService.getUserByName(getCurrentUser().getName())).andReturn(expected).once();

        replayMocks();
        assertSame(expected, getResource().getCurrentUser());
        assertSame(expected, getResource().getCurrentUser());
        assertSame(expected, getResource().getCurrentUser());
        assertSame(expected, getResource().getCurrentUser());
        // expect no failures on verify
        verifyMocks();
    }

    public static class TestResource extends AbstractPscResource {
        public TestResource() {
            setAllAuthorizedFor(GET);
            setAuthorizedFor(PUT, SYSTEM_ADMINISTRATOR);
            setAuthorizedFor(POST, STUDY_ADMIN, STUDY_COORDINATOR);
        }
    }
}
