package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.security.AuthenticationSystemConfiguration;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextImpl;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.AbstractAuthenticationToken;
import org.acegisecurity.providers.TestingAuthenticationToken;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import static org.easymock.classextension.EasyMock.expect;
import org.restlet.Restlet;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import java.util.regex.Pattern;

/**
 * @author Rhett Sutphin
 */
public class PscGuardTest extends RestletTestCase {
    private static final String USERNAME = "joe";
    private static final String PASSWORD = "pass";
    private static final UsernamePasswordAuthenticationToken USERNAME_PASSWORD_AUTHENTICATION 
        = new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD);

    private static final String TOKEN = "token";
    private static final SingleTokenAuthenticationToken TOKEN_BASED_AUTHENTICATION
        = new SingleTokenAuthenticationToken(TOKEN);

    private PscGuard guard;
    private User user;
    private UsernamePasswordAuthenticationToken authenticated;

    private MockRestlet nextRestlet;
    private AuthenticationManager authenticationManager;
    private AuthenticationSystemConfiguration configuration;
    private AuthenticationSystem authenticationSystem;
    private SecurityContext securityContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        user = Fixtures.createNamedInstance(USERNAME, User.class);
        authenticated = new UsernamePasswordAuthenticationToken(
            user, PASSWORD, new GrantedAuthority[0]);

        authenticationManager = registerMockFor(AuthenticationManager.class);
        configuration = registerMockFor(AuthenticationSystemConfiguration.class);
        authenticationSystem = registerMockFor(AuthenticationSystem.class);
        expect(configuration.getAuthenticationSystem())
            .andReturn(authenticationSystem).anyTimes();
        expect(authenticationSystem.authenticationManager())
            .andReturn(authenticationManager).anyTimes();

        securityContext = new SecurityContextImpl();
        SecurityContextHolder.setContext(securityContext);

        nextRestlet = new MockRestlet();
        guard = new PscGuard();
        guard.setNext(nextRestlet);
        guard.setAuthenticationSystemConfiguration(configuration);

        request.setResourceRef(BASE_URI);

        // register token auth helper
        new PscAuthenticationHelper().afterPropertiesSet();
    }

    public void testAuthenticationSkippedForExcepts() throws Exception {
        guard.setExcept(Pattern.compile("everyone-welcome.*"));
        request.setResourceRef(ROOT_URI + "/everyone-welcome/in-here");
        doHandle();
        assertNextInvoked();
    }

    public void testExceptsMatchedFromBeginningOfUri() throws Exception {
        Pattern except = Pattern.compile("in.*");
        String uri = "everyone-welcome/in-here";
        // ensure that the pattern would match if the guard was implemented using find instead of matches
        assertTrue("Test setup failure", except.matcher(uri).find());

        guard.setExcept(except);
        request.setResourceRef(ROOT_URI + '/' + uri);
        doHandle();

        assertResponseStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
    }

    public void testAuthenticationRequiredForNotSkipped() throws Exception {
        guard.setExcept(Pattern.compile("everyone-welcome.*"));
        request.setResourceRef(ROOT_URI + "/keep-out/of-here");
        doHandle();

        assertResponseStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
    }

    public void testAuthenticationRequiredForEverythingIfNoExcept() throws Exception {
        guard.setExcept(null);
        request.setResourceRef(ROOT_URI + "/keep-out/of-here");
        doHandle();

        assertResponseStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
    }

    public void testBasicAuthenticationWorksIfAuthenticationManagerPasses() throws Exception {
        expectBasicAuthChallengeResponse();
        expectCreateUsernamePasswordRequest();

        expect(authenticationManager.authenticate(USERNAME_PASSWORD_AUTHENTICATION))
            .andReturn(authenticated);

        doHandle();
        assertNextInvoked();
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void test501ForBasicIfUsernameAndPasswordNotSupported() throws Exception {
        expectBasicAuthChallengeResponse();
        expect(authenticationSystem.createUsernamePasswordAuthenticationRequest(USERNAME, PASSWORD))
            .andReturn(null);

        doHandle();
        assertResponseStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
        String responseText = response.getEntity().getText();
        assertContains("Missing error message in response entity", responseText,
            ChallengeScheme.HTTP_BASIC.getTechnicalName() + " authentication is not supported with the configured authentication system");
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
    }

    public void testBasicAuthenticationFailsIfAuthenticationManagerThrowsException() throws Exception {
        expectBasicAuthChallengeResponse();
        expectCreateUsernamePasswordRequest();

        expect(authenticationManager.authenticate(USERNAME_PASSWORD_AUTHENTICATION))
            .andThrow(new BadCredentialsException("That's not the right thing"));

        doHandle();
        assertResponseStatus(Status.CLIENT_ERROR_UNAUTHORIZED); // Not 403 -- failure sends rechallenge
    }

    public void testSuccessfulBasicAuthenticationStoresAcegiAuthenticationInRequest() throws Exception {
        expectBasicAuthChallengeResponse();
        expectCreateUsernamePasswordRequest();

        expect(authenticationManager.authenticate(USERNAME_PASSWORD_AUTHENTICATION))
            .andReturn(authenticated);

        doHandle();

        assertNextInvoked();
        Object actualToken = request.getAttributes().get(PscGuard.AUTH_TOKEN_ATTRIBUTE_KEY);
        assertNotNull("Token missing", actualToken);
        assertSame("Wrong token", authenticated, actualToken);
    }

    public void testCustomAuthenticationWorksIfAuthenticationManagerPasses() throws Exception {
        expectPscTokenAuthChallengeResponse();
        expectCreateTokenRequest();

        expect(authenticationManager.authenticate(TOKEN_BASED_AUTHENTICATION))
            .andReturn(authenticated);

        doHandle();
        assertNextInvoked();
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void test501ForCustomIfUsernameAndPasswordNotSupported() throws Exception {
        expectPscTokenAuthChallengeResponse();
        expect(authenticationSystem.createTokenAuthenticationRequest(TOKEN))
            .andReturn(null);

        doHandle();
        assertResponseStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
        String responseText = response.getEntity().getText();
        assertContains("Missing error message in response entity", responseText,
            PscGuard.PSC_TOKEN.getTechnicalName() + " authentication is not supported with the configured authentication system");
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
    }

    public void testChallengedIfThereIsNoAcegiSecurityAuthentication() throws Exception {
        securityContext.setAuthentication(null);
        doHandle();
        assertResponseStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
    }

    public void testChallengedIfThereIsASecurityContextButItIsNotAuthenticated() throws Exception {
        securityContext.setAuthentication(new TestingAuthenticationToken("Bad", "guy", new GrantedAuthority[0]));
        doHandle();
        assertResponseStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
    }
    
    public void testNotChallengedIfThereIsAnAuthenticatedSecurityContextAvailable() throws Exception {
        securityContext.setAuthentication(authenticated);
        doHandle();
        assertResponseStatus(Status.SUCCESS_OK);
        assertNextInvoked();
    }

    private void expectBasicAuthChallengeResponse() {
        // the guard will be presented with the username/pass, already decoded
        request.setChallengeResponse(
            new ChallengeResponse(ChallengeScheme.HTTP_BASIC, USERNAME, PASSWORD));
    }
    
    private void expectCreateUsernamePasswordRequest() {
        expect(authenticationSystem.createUsernamePasswordAuthenticationRequest(USERNAME, PASSWORD))
            .andReturn(USERNAME_PASSWORD_AUTHENTICATION);
    }

    private void expectPscTokenAuthChallengeResponse() {
        request.setChallengeResponse(
            new ChallengeResponse(PscGuard.PSC_TOKEN, TOKEN));
    }

    private void expectCreateTokenRequest() {
        expect(authenticationSystem.createTokenAuthenticationRequest(TOKEN))
            .andReturn(TOKEN_BASED_AUTHENTICATION);
    }

    private void doHandle() {
        replayMocks();
        guard.handle(request, response);
        verifyMocks();
    }

    private void assertNextInvoked() {
        assertTrue("Next not invoked", nextRestlet.handleCalled());
    }

    private static final class SingleTokenAuthenticationToken extends AbstractAuthenticationToken {
        private String token;

        public SingleTokenAuthenticationToken(String token) {
            this.token = token;
        }

        public Object getCredentials() {
            return token;
        }

        public Object getPrincipal() {
            return "SOME GUY";
        }
    }

    private static final class MockRestlet extends Restlet {
        private Request lastRequest;
        private Response lastResponse;

        @Override
        public void handle(Request request, Response response) {
            this.lastRequest = request;
            this.lastResponse = response;
        }

        public boolean handleCalled() {
            return lastRequest != null;
        }
    }
}
