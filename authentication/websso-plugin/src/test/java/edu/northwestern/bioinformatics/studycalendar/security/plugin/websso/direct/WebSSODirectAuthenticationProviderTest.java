package edu.northwestern.bioinformatics.studycalendar.security.plugin.websso.direct;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationTestCase;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.direct.CasDirectException;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.direct.CasDirectUsernamePasswordAuthenticationToken;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.BadCredentialsException;

import java.io.IOException;

import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.assertContains;
import static org.easymock.EasyMock.expect;

/**
 * Note that this class only includes tests for differences between
 * {@link edu.northwestern.bioinformatics.studycalendar.security.plugin.websso.direct.WebSSODirectAuthenticationProvider}
 * and {@link
 *
 * @author Rhett Sutphin
 */
public class WebSSODirectAuthenticationProviderTest extends AuthenticationTestCase {
    private static final String CREDENTIAL_PROVIDER = ">Foo Provider";
    private static final String ORGANIZATION = "Dorian";
    private static final String USERNAME = "joe";
    private static final String QUALIFIED_USERNAME =
        CREDENTIAL_PROVIDER + '\\' + ORGANIZATION + '\\' + USERNAME;
    private static final String PASSWORD = "eoj";
    private static final String LT = "the-ticket";

    private WebSSODirectAuthenticationProvider provider;

    private DirectLoginHttpFacade loginFacade;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        loginFacade = registerMockFor(DirectLoginHttpFacade.class);

        provider = new WebSSODirectAuthenticationProvider() {
            @Override
            protected DirectLoginHttpFacade createLoginFacade() {
                return loginFacade;
            }
        };
        provider.setUserDetailsService(userDetailsService);

        userDetailsService.addUser(USERNAME, PscRole.STUDY_SUBJECT_CALENDAR_MANAGER);
    }

    public void testUsernameOnlyPrincipalResultsInNoAuthentication() throws Exception {
        try {
            doAuthenticate(USERNAME);
            fail("Exception not thrown");
        } catch (BadCredentialsException bce) {
            assertEquals("The principal must be of the form credential-provider\\authentication-service-name\\username", bce.getMessage());
        }
    }

    public void testTooManyEntriesInPrincipalResultsInNoAuthentication() throws Exception {
        try {
            doAuthenticate("something\\else\\again\\and-again");
            fail("Exception not thrown");
        } catch (BadCredentialsException bce) {
            assertEquals("The principal must be of the form credential-provider\\authentication-service-name\\username", bce.getMessage());
        }
    }

    public void testBadLoginFormResultsInNoAuthentication() throws Exception {
        /* expect */ loginFacade.start();
        expect(loginFacade.selectCredentialProvider(CREDENTIAL_PROVIDER)).andThrow(new CasDirectException("Bad form"));

        try {
            doAuthenticate();
            fail("Exception not thrown");
        } catch (AuthenticationException ae) {
            assertContains("Wrong exception message", ae.getMessage(), "Direct CAS login failed");
            assertContains("Wrong exception message", ae.getMessage(), "Bad form");
        }
    }

    public void testUnavailableLoginFormResultsInNoAuthentication() throws Exception {
        /* expect */ loginFacade.start();
        expect(loginFacade.selectCredentialProvider(CREDENTIAL_PROVIDER)).andThrow(new IOException("Bad connection"));

        try {
            doAuthenticate();
            fail("Exception not thrown");
        } catch (AuthenticationException ae) {
            assertContains("Wrong exception message", ae.getMessage(), "Direct CAS login failed");
            assertContains("Wrong exception message", ae.getMessage(), "Bad connection");
        }
    }

    public void testFailedPostResultsInNoAuthentication() throws Exception {
        /* expect */ loginFacade.start();
        expect(loginFacade.selectCredentialProvider(CREDENTIAL_PROVIDER)).
            andReturn("<input name='lt' value='the-ticket'/><select id='authenticationServiceURL'><option value='http://dorian!/'>Dorian</select>");
        /* expect */ loginFacade.selectAuthenticationService("http://dorian!/");
        expect(loginFacade.postCredentials(USERNAME, PASSWORD)).andReturn(false);

        try {
            doAuthenticate();
            fail("Exception not thrown");
        } catch (BadCredentialsException bce) {
            assertEquals("Credentials are invalid according to direct CAS login", bce.getMessage());
        }
    }

    public void testUnknownAuthenticationServiceFailsAuthentication() throws Exception {
        /* expect */ loginFacade.start();
        expect(loginFacade.selectCredentialProvider(CREDENTIAL_PROVIDER)).
            andReturn("<input name='lt' value='the-ticket'/><select id='authenticationServiceURL'><option value='http://dorian!/'>Not Dorian</select>");

        try {
            doAuthenticate();
            fail("Exception not thrown");
        } catch (BadCredentialsException bce) {
            assertEquals("The WebSSO server does not know of an authentication service named \"Dorian\"", bce.getMessage());
        }
    }
    
    public void testSuccessfulPostResultsInAuthentication() throws Exception {
        /* expect */ loginFacade.start();
        expect(loginFacade.selectCredentialProvider(CREDENTIAL_PROVIDER)).
            andReturn("<input name='lt' value='the-ticket'/><select id='authenticationServiceURL'><option value='http://dorian!/'>Dorian</select>");
        /* expect */ loginFacade.selectAuthenticationService("http://dorian!/");

        expect(loginFacade.postCredentials(USERNAME, PASSWORD)).andReturn(true);

        Authentication actual = doAuthenticate();
        assertNotNull(actual);
        assertTrue(actual.isAuthenticated());
    }

    private Authentication doAuthenticate() {
        return doAuthenticate(QUALIFIED_USERNAME);
    }

    private Authentication doAuthenticate(String qualifiedUsername) {
        replayMocks();
        Authentication actual = provider.authenticate(
            new CasDirectUsernamePasswordAuthenticationToken(qualifiedUsername, PASSWORD));
        verifyMocks();
        return actual;
    }
}
