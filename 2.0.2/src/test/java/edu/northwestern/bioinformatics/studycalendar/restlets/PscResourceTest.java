package edu.northwestern.bioinformatics.studycalendar.restlets;

import static edu.northwestern.bioinformatics.studycalendar.domain.Role.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import static org.restlet.data.Method.*;

import java.util.Collection;

/**
 * Tests for functionality implemented in {@link AbstractPscResource}
 *
 * @author Rhett Sutphin
 */
public class PscResourceTest extends ResourceTestCase<PscResourceTest.TestResource> {
    @Override
    protected TestResource createResource() {
        return new TestResource();
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

    public static class TestResource extends AbstractPscResource {
        public TestResource() {
            setAllAuthorizedFor(GET);
            setAuthorizedFor(PUT, SYSTEM_ADMINISTRATOR);
            setAuthorizedFor(POST, STUDY_ADMIN, STUDY_COORDINATOR);
        }
    }
}
