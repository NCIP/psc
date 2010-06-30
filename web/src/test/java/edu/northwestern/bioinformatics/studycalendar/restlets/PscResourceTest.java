package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.restlet.data.Parameter;
import org.restlet.util.Series;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collection;

import static edu.northwestern.bioinformatics.studycalendar.domain.Role.*;
import static edu.northwestern.bioinformatics.studycalendar.restlets.AbstractPscResource.getApiDateFormat;
import static org.restlet.data.Method.*;

/**
 * Tests for functionality implemented in {@link AbstractPscResource}
 *
 * @author Rhett Sutphin
 */
public class PscResourceTest extends AuthorizedResourceTestCase<PscResourceTest.TestResource> {
    @Override
    protected TestResource createAuthorizedResource() {
        return new TestResource();
    }

    public void testAllAuthorizedMethodReturnsNull() throws Exception {
        assertNull(getResource().legacyAuthorizedRoles(GET));
    }

    public void testProperLegacyRolesReturnedForLimitedAuthorizationResources() throws Exception {
        Collection<Role> putRoles = getResource().legacyAuthorizedRoles(PUT);
        assertEquals(1, putRoles.size());
        assertEquals(SYSTEM_ADMINISTRATOR, putRoles.iterator().next());

        Collection<Role> postRoles = getResource().legacyAuthorizedRoles(POST);
        assertEquals(2, postRoles.size());
        assertContains(postRoles, STUDY_ADMIN);
        assertContains(postRoles, STUDY_COORDINATOR);
    }
    
    public void testNoLegacyRolesReturnedForUnmentionedMethods() throws Exception {
        Collection<Role> lockRoles = getResource().legacyAuthorizedRoles(LOCK);
        assertNotNull(lockRoles);
        assertEquals(0, lockRoles.size());
    }

    public void testCurrentUserCanBeLoadedWhenThereIsAnAuthentication() throws Exception {
        assertNotNull("Test setup failure", PscGuard.getCurrentAuthenticationToken(request));

        replayMocks();
        assertSame(getLegacyCurrentUser(), getResource().getLegacyCurrentUser());
        verifyMocks();
    }

    public void testCurrentUserIsNullWhenThereIsNoAuthentication() throws Exception {
        PscGuard.setCurrentAuthenticationToken(request, null);

        replayMocks();
        assertNull(getResource().getLegacyCurrentUser());
        verifyMocks();
    }

    public void testCurrentUserThrowsExceptionForUnexpectedPrincipalType() throws Exception {
        PscGuard.setCurrentAuthenticationToken(request, new UsernamePasswordAuthenticationToken("fred", "frob"));

        replayMocks();
        try {
            getResource().getLegacyCurrentUser();
            fail("Exception not thrown");
        } catch (ClassCastException cce) {
            assertEquals("Wrong message",
                "PSC's Principal is expected to always be a edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser.  Right now it is a java.lang.String.",
                cce.getMessage());
        }
        verifyMocks();
    }

    public void testApiDateFormatIsCorrect() throws Exception {
        assertEquals("2009-02-04",
            getApiDateFormat().format(DateTools.createDate(2009, Calendar.FEBRUARY, 4)));
    }

    public void testApiDateFormatIsCached() throws Exception {
        assertSame(getApiDateFormat(), getApiDateFormat());
    }

    public void testApiDateFormatIsThreadLocal() throws Exception {
        final DateFormat fromThisThread = getApiDateFormat();
        final DateFormat[] fromOtherThread = new DateFormat[1];
        Thread t = new Thread(new Runnable() {
            public void run() { fromOtherThread[0] = getApiDateFormat(); }
        });
        t.join();

        assertNotSame(fromThisThread, fromOtherThread[0]);
    }

    public void testCachingIsDisabled() throws Exception {
        doInitOnly();
        Series<Parameter> actualHeaders = response.getHttpCall().getResponseHeaders();
        assertEquals("no-store, no-cache, must-revalidate, post-check=0, pre-check=0",
            actualHeaders.getValues("Cache-Control"));
        assertEquals("no-cache", actualHeaders.getFirstValue("Pragma"));
    }

    public static class TestResource extends AbstractPscResource {
        public TestResource() {
            setAllAuthorizedFor(GET);
            setAuthorizedFor(PUT, SYSTEM_ADMINISTRATOR);
            setAuthorizedFor(POST, STUDY_ADMIN, STUDY_COORDINATOR);
        }
    }
}
