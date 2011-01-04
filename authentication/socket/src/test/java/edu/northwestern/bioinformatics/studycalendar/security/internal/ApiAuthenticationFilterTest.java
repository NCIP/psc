package edu.northwestern.bioinformatics.studycalendar.security.internal;

import edu.northwestern.bioinformatics.studycalendar.security.AuthenticationSystemConfiguration;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationTestCase;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.context.SecurityContextImpl;
import org.acegisecurity.providers.AbstractAuthenticationToken;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class ApiAuthenticationFilterTest extends AuthenticationTestCase {
    private static final String USERNAME = "quux";
    private static final String PASSWORD = "barbaz";
    private static final String BASIC_FOR_USERNAME_AND_PASSWORD = "Basic cXV1eDpiYXJiYXo=";
    private static final UsernamePasswordAuthenticationToken USERNAME_PASSWORD_AUTH_REQUEST_TOKEN =
        new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD);

    private static final String TOKEN = "barbarrabrab";
    private static final String PSC_TOKEN_HEADER_VALUE_FOR_TOKEN = "psc_token " + TOKEN;
    private static final Authentication TOKEN_AUTH_REQUEST_TOKEN =
        new SingleTokenAuthenticationToken(TOKEN);

    private static final Authentication AUTHENTICATED_TOKEN =
        new UsernamePasswordAuthenticationToken(
            USERNAME, null, new GrantedAuthority[] { PscRole.DATA_READER });

    private ApiAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;
    private AuthenticationSystem authenticationSystem;
    private AuthenticationManager authenticationManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();

        AuthenticationSystemConfiguration configuration =
            registerMockFor(AuthenticationSystemConfiguration.class);
        authenticationSystem = registerMockFor(AuthenticationSystem.class);
        authenticationManager = registerMockFor(AuthenticationManager.class);
        expect(configuration.getAuthenticationSystem()).andStubReturn(authenticationSystem);
        expect(authenticationSystem.authenticationManager()).andStubReturn(authenticationManager);

        expect(authenticationSystem.createUsernamePasswordAuthenticationRequest(USERNAME, PASSWORD)).
            andStubReturn(USERNAME_PASSWORD_AUTH_REQUEST_TOKEN);
        expect(authenticationSystem.createTokenAuthenticationRequest(TOKEN)).
            andStubReturn(TOKEN_AUTH_REQUEST_TOKEN);

        filter = new ApiAuthenticationFilter();
        filter.setAuthenticationSystemConfiguration(configuration);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        SecurityContextHolder.clearContext();
    }

    public void testDoesNothingWithNoAuthorizationHeader() throws Exception {
        doFilter();
    }

    ////// HTTP BASIC

    public void testDoesUsernamePasswordAuthForBasic() throws Exception {
        request.addHeader("Authorization", BASIC_FOR_USERNAME_AND_PASSWORD);

        expect(authenticationManager.authenticate(USERNAME_PASSWORD_AUTH_REQUEST_TOKEN)).
            andReturn(AUTHENTICATED_TOKEN);

        doFilter();

        assertSame(AUTHENTICATED_TOKEN, SecurityContextHolder.getContext().getAuthentication());
    }

    public void testReturns501WithBasicIfAuthenticationSystemDoesNotSupportUsernames() throws Exception {
        request.addHeader("Authorization", BASIC_FOR_USERNAME_AND_PASSWORD);

        expect(authenticationSystem.createUsernamePasswordAuthenticationRequest(USERNAME, PASSWORD)).
            andReturn(null);

        doFilter(false);

        assertEquals("Should be 501", HttpServletResponse.SC_NOT_IMPLEMENTED, response.getStatus());
        assertEquals("Wrong message",
            "Basic credentials are not supported with the configured authentication system",
            response.getErrorMessage());
    }

    public void testDoesNothingForBadCredentialsBasic() throws Exception {
        request.addHeader("Authorization", BASIC_FOR_USERNAME_AND_PASSWORD);

        expect(authenticationManager.authenticate(USERNAME_PASSWORD_AUTH_REQUEST_TOKEN)).
            andThrow(new BadCredentialsException("Nope"));

        doFilter();

        assertNoOneLoggedIn();
    }

    public void testDoesNothingForInvalidBasic() throws Exception {
        request.addHeader("Authorization", "Basic notbase64atall");

        doFilter();

        assertNoOneLoggedIn();
    }

    public void testDoesNothingForIncompleteBasic() throws Exception {
        request.addHeader("Authorization", "Basic");

        doFilter();

        assertNoOneLoggedIn();
    }

    ////// PSC_TOKEN

    public void testDoesTokenAuthForPscToken() throws Exception {
        request.addHeader("Authorization", PSC_TOKEN_HEADER_VALUE_FOR_TOKEN);

        expect(authenticationManager.authenticate(TOKEN_AUTH_REQUEST_TOKEN)).
            andReturn(AUTHENTICATED_TOKEN);

        doFilter();

        assertSame(AUTHENTICATED_TOKEN, SecurityContextHolder.getContext().getAuthentication());
    }

    public void testReturns501WithBasicIfAuthenticationSystemDoesNotSupportTokens() throws Exception {
        request.addHeader("Authorization", PSC_TOKEN_HEADER_VALUE_FOR_TOKEN);

        expect(authenticationSystem.createTokenAuthenticationRequest(TOKEN)).
            andReturn(null);

        doFilter(false);

        assertEquals("Should be 501", HttpServletResponse.SC_NOT_IMPLEMENTED, response.getStatus());
        assertEquals("Wrong message",
            "psc_token credentials are not supported with the configured authentication system",
            response.getErrorMessage());
    }

    public void testDoesNothingForBadToken() throws Exception {
        request.addHeader("Authorization", PSC_TOKEN_HEADER_VALUE_FOR_TOKEN);

        expect(authenticationManager.authenticate(TOKEN_AUTH_REQUEST_TOKEN)).
            andThrow(new BadCredentialsException("Nope"));

        doFilter();

        assertNoOneLoggedIn();
    }

    public void testDoesNothingForIncompleteToken() throws Exception {
        request.addHeader("Authorization", "psc_token");

        doFilter();

        assertNoOneLoggedIn();
    }

    ////// REPLACE EXISTING

    public void testExistingSecurityContextReplacedWithGoodAuthorization() throws Exception {
        expectOtherUserLoggedIn();

        request.addHeader("Authorization", BASIC_FOR_USERNAME_AND_PASSWORD);

        expect(authenticationManager.authenticate(USERNAME_PASSWORD_AUTH_REQUEST_TOKEN)).
            andReturn(AUTHENTICATED_TOKEN);

        doFilter();

        assertSame(AUTHENTICATED_TOKEN, SecurityContextHolder.getContext().getAuthentication());
    }

    public void testExistingSecurityContextClearedWithBadAuthorization() throws Exception {
        expectOtherUserLoggedIn();

        request.addHeader("Authorization", BASIC_FOR_USERNAME_AND_PASSWORD);

        expect(authenticationManager.authenticate(USERNAME_PASSWORD_AUTH_REQUEST_TOKEN)).
            andThrow(new BadCredentialsException("Nope"));

        doFilter();

        assertNoOneLoggedIn();
    }

    public void testExistingSecurityContextRemainsWithNoAuthorization() throws Exception {
        Authentication otherUser = expectOtherUserLoggedIn();

        doFilter();

        assertSame(otherUser, SecurityContextHolder.getContext().getAuthentication());
    }

    public void testDoesNothingForOtherScheme() throws Exception {
        request.addHeader("Authorization", "Digest-or-something 3456");

        doFilter();

        assertNoOneLoggedIn();
    }

    ////// HELPERS

    private void doFilter() throws IOException, ServletException {
        doFilter(true);
    }

    private void doFilter(boolean expectContinued) throws IOException, ServletException {
        replayMocks();
        filter.doFilter(request, response, filterChain);
        verifyMocks();

        if (expectContinued) {
            assertSame("Filter chain not continued", request, filterChain.getRequest());
        } else {
            assertNull("Filter chain incorrectly continued", filterChain.getRequest());
        }
    }

    private Authentication expectOtherUserLoggedIn() {
        UsernamePasswordAuthenticationToken someguy = new UsernamePasswordAuthenticationToken(
            "someguy", null, new GrantedAuthority[] { PscRole.SYSTEM_ADMINISTRATOR });

        SecurityContextHolder.setContext(new SecurityContextImpl());
        SecurityContextHolder.getContext().setAuthentication(someguy);
        return someguy;
    }

    private void assertNoOneLoggedIn() {
        assertNull("No one should be logged in", SecurityContextHolder.getContext().getAuthentication());
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
            return "SOMEONE";
        }
    }
}
