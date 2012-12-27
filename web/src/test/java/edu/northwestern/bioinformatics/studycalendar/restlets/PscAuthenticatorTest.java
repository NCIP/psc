/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.TestingAuthenticationToken;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Status;

import java.util.regex.Pattern;

/**
 * @author Rhett Sutphin
 */
public class PscAuthenticatorTest extends RestletTestCase {
    private PscAuthenticator filter;

    private Authentication authenticated;

    private MockRestlet nextRestlet;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SecurityContextHolderTestHelper.setUserAndReturnMembership("jo", PscRole.DATA_READER);
        authenticated = SecurityContextHolder.getContext().getAuthentication();

        nextRestlet = new MockRestlet();
        filter = new PscAuthenticator();
        filter.setNext(nextRestlet);

        request.setResourceRef(BASE_URI);
    }

    ////// AUTHENTICATION EXCEPTIONS

    public void testAuthenticationSkippedForExcepts() throws Exception {
        SecurityContextHolder.clearContext();

        filter.setExcept(Pattern.compile("everyone-welcome.*"));
        request.setResourceRef(ROOT_URI + "/everyone-welcome/in-here");
        doHandle();
        assertNextInvoked();
    }

    public void testExceptsMatchedFromBeginningOfUri() throws Exception {
        SecurityContextHolder.clearContext();

        Pattern except = Pattern.compile("in.*");
        String uri = "everyone-welcome/in-here";
        // ensure that the pattern would match if the guard was implemented using find instead of matches
        assertTrue("Test setup failure", except.matcher(uri).find());

        filter.setExcept(except);
        request.setResourceRef(ROOT_URI + '/' + uri);
        doHandle();

        assertChallenged();
    }

    public void testAuthenticationRequiredForNotSkipped() throws Exception {
        SecurityContextHolder.clearContext();

        filter.setExcept(Pattern.compile("everyone-welcome.*"));
        request.setResourceRef(ROOT_URI + "/keep-out/of-here");
        doHandle();

        assertChallenged();
    }

    public void testAuthenticationRequiredForEverythingIfNoExcept() throws Exception {
        SecurityContextHolder.clearContext();

        filter.setExcept(null);
        request.setResourceRef(ROOT_URI + "/keep-out/of-here");
        doHandle();

        assertChallenged();
    }

    ////// MAKING AUTH AVAILABLE IN REQUEST

    public void testStoresAcegiAuthenticationInRequestWhenPresent() throws Exception {
        doHandle();
        assertSame("Wrong token",
            authenticated, request.getAttributes().get(PscAuthenticator.AUTH_TOKEN_ATTRIBUTE_KEY));
    }

    public void testStoresNoAcegiAuthenticationInRequestWhenNotPresent() throws Exception {
        SecurityContextHolder.clearContext();
        doHandle();
        assertNull("Should be no token",
            request.getAttributes().get(PscAuthenticator.AUTH_TOKEN_ATTRIBUTE_KEY));
    }

    ////// AUTHENTICATION

    public void testNotChallengedIfThereIsAnAuthenticatedSecurityContext() throws Exception {
        doHandle();
        assertNextInvoked();
    }

    public void testChallengedIfThereIsNoAcegiSecurityAuthentication() throws Exception {
        SecurityContextHolder.clearContext();
        doHandle();
        assertChallenged();
    }

    public void testChallengedIfThereIsASecurityContextButItIsNotAuthenticated() throws Exception {
        SecurityContextHolder.getContext().
            setAuthentication(new TestingAuthenticationToken("Bad", "guy", new GrantedAuthority[0]));
        doHandle();
        assertChallenged();
    }
    
    ////// HELPERS

    private void doHandle() {
        replayMocks();
        filter.handle(request, response);
        verifyMocks();
    }

    private void assertChallenged() {
        assertResponseStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        assertEquals("Wrong number of challenges: " + response.getChallengeRequests(),
            2, response.getChallengeRequests().size());
        assertChallenge("Wrong first challenge",
            ChallengeScheme.HTTP_BASIC, "PSC", response.getChallengeRequests().get(0));
        assertChallenge("Wrong first challenge",
            PscAuthenticator.HTTP_PSC_TOKEN, "PSC", response.getChallengeRequests().get(1));
        assertFalse("Next invoked", nextRestlet.handleCalled());
    }

    private void assertChallenge(
        String msg, ChallengeScheme expectedScheme, String expectedRealm, ChallengeRequest actual
    ) {
        assertEquals(msg + ": wrong scheme", expectedScheme, actual.getScheme());
        assertEquals(msg + ": wrong realm", expectedRealm, actual.getRealm());
    }

    private void assertNextInvoked() {
        assertTrue("Next not invoked", nextRestlet.handleCalled());
    }
}
