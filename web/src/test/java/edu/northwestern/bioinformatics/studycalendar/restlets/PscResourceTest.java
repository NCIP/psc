package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import gov.nih.nci.cabig.ctms.suite.authorization.ScopeType;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.restlet.data.Parameter;
import org.restlet.util.Series;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

import static edu.northwestern.bioinformatics.studycalendar.domain.Role.*;
import static edu.northwestern.bioinformatics.studycalendar.restlets.AbstractPscResource.getApiDateFormat;
import static org.restlet.data.Method.*;

/**
 * Tests for functionality implemented in {@link AbstractPscResource}
 *
 * @author Rhett Sutphin
 */
public class PscResourceTest extends AuthorizedResourceTestCase<PscResourceTest.TestResource> {
    private Site siteA;
    private Study studyB;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        siteA = Fixtures.createSite("A", "a?");
        studyB = Fixtures.createReleasedTemplate("B");
    }

    @Override
    protected TestResource createAuthorizedResource() {
        return new TestResource();
    }

    public void testAllAuthorizedMethodReturnsNull() throws Exception {
        assertNull(getResource().legacyAuthorizedRoles(GET));
        assertNull(getResource().authorizations(GET));
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

    public void testProperAuthorizationReturnedForRoleAuthorizedMethod() throws Exception {
        Collection<ResourceAuthorization> putAuth = getResource().authorizations(PUT);
        assertEquals(1, putAuth.size());
        assertAuthorization("Wrong sole authorization", PscRole.SYSTEM_ADMINISTRATOR, putAuth.iterator().next());
    }

    protected void assertAuthorization(String message, PscRole expectedRole, ResourceAuthorization actual) {
        assertEquals(message + ": wrong role", expectedRole, actual.getRole());
        assertNull(message + ": should not be site scoped", actual.getScope(ScopeType.SITE));
        assertNull(message + ": should not be study scoped", actual.getScope(ScopeType.STUDY));
    }

    protected void assertAuthorization(
        String message, PscRole expectedRole, String expectedSiteIdent, ResourceAuthorization actual
    ) {
        assertEquals(message + ": wrong role", expectedRole, actual.getRole());
        assertEquals(message + ": wrong site scope", expectedSiteIdent, actual.getScope(ScopeType.SITE));
        assertNull(message + ": should not be study scoped", actual.getScope(ScopeType.STUDY));
    }

    protected void assertAuthorization(
        String message, PscRole expectedRole,
        String expectedSiteIdent, String expectedStudyIdent, ResourceAuthorization actual
    ) {
        assertEquals(message + ": wrong role", expectedRole, actual.getRole());
        assertEquals(message + ": wrong site scope", expectedSiteIdent, actual.getScope(ScopeType.SITE));
        assertEquals(message + ": wrong study scope", expectedStudyIdent, actual.getScope(ScopeType.STUDY));
    }

    public void testProperAuthorizationReturnedForAuthorizationAuthorizedMethod() throws Exception {
        Collection<ResourceAuthorization> postAuth = getResource().authorizations(POST);
        assertEquals(2, postAuth.size());
        Iterator<ResourceAuthorization> it = postAuth.iterator();
        assertAuthorization("Wrong 1st auth", PscRole.STUDY_QA_MANAGER, "a?", it.next());
        assertAuthorization("Wrong 2nd auth", PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER, "a?", "B", it.next());
    }

    public void testProperAuthorizationReturnedForSiteRoleAuthorizedMethod() throws Exception {
        Collection<ResourceAuthorization> deleteAuth = getResource().authorizations(DELETE);
        assertEquals(2, deleteAuth.size());
        Iterator<ResourceAuthorization> it = deleteAuth.iterator();
        assertAuthorization("Wrong 1st auth", PscRole.USER_ADMINISTRATOR, "a?", it.next());
        assertAuthorization("Wrong 2nd auth", PscRole.REGISTRAR, "a?", it.next());
    }

    public void testProperAuthorizationReturnedForSiteStudyRoleAuthorizedMethod() throws Exception {
        Collection<ResourceAuthorization> moveAuth = getResource().authorizations(MOVE);
        assertEquals(1, moveAuth.size());
        Iterator<ResourceAuthorization> it = moveAuth.iterator();
        assertAuthorization("Wrong 1st auth", PscRole.LAB_DATA_USER, "a?", "B", it.next());
    }

    public void testNoAuthorizationsReturnedForUnmentionedMethods() throws Exception {
        Collection<ResourceAuthorization> lockRoles = getResource().authorizations(LOCK);
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

    public class TestResource extends AbstractPscResource {
        public TestResource() {
            setAllAuthorizedFor(GET);
            setAuthorizedFor(PUT, SYSTEM_ADMINISTRATOR);
            setAuthorizedFor(POST, STUDY_ADMIN, STUDY_COORDINATOR);

            addAuthorizationsFor(PUT, PscRole.SYSTEM_ADMINISTRATOR);
            addAuthorizationsFor(POST,
                ResourceAuthorization.create(PscRole.STUDY_QA_MANAGER, siteA),
                ResourceAuthorization.create(PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER, siteA, studyB));
            addAuthorizationsFor(DELETE, siteA, PscRole.USER_ADMINISTRATOR, PscRole.REGISTRAR);
            addAuthorizationsFor(MOVE, siteA, studyB, PscRole.LAB_DATA_USER);
        }
    }
}
