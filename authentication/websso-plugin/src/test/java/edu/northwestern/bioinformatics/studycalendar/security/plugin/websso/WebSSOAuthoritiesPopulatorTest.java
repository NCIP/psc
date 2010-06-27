package edu.northwestern.bioinformatics.studycalendar.security.plugin.websso;

import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationTestCase;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.userdetails.UserDetails;
import static org.easymock.EasyMock.expect;
import org.globus.gsi.GlobusCredential;

import java.security.PrivateKey;

/**
 * @author Rhett Sutphin
 */
public class WebSSOAuthoritiesPopulatorTest extends AuthenticationTestCase {
    private static final String WEBSSO_RESPONSE =
        "CAGRID_SSO_GRID_IDENTITY^/C=US/O=NU/OU=NUBIC/OU=Dorian/CN=jo$CAGRID_SSO_FIRST_NAME^Josephine$CAGRID_SSO_LAST_NAME^Miller$CAGRID_SSO_DELEGATION_SERVICE_EPR^<ns1:DelegatedCredentialReference xmlns:ns1=\"http://cds.gaards.cagrid.org/CredentialDelegationService/DelegatedCredential/types\">\n" +
            " <ns2:EndpointReference xmlns:ns2=\"http://schemas.xmlsoap.org/ws/2004/03/addressing\">\n" +
            "  <ns2:Address>https://localhost:8443/wsrf/services/cagrid/DelegatedCredential</ns2:Address>\n" +
            "  <ns2:ReferenceProperties>\n" +
            "   <ns2:DelegatedCredentialKey xmlns:ns2=\"http://cds.gaards.cagrid.org/CredentialDelegationService/DelegatedCredential\">\n" +
            "    <ns3:delegationId xmlns:ns3=\"http://gaards.cagrid.org/cds\">3</ns3:delegationId>\n" +
            "   </ns2:DelegatedCredentialKey>\n" +
            "  </ns2:ReferenceProperties>\n" +
            "  <ns2:ReferenceParameters/>\n" +
            " </ns2:EndpointReference>\n" +
            "</ns1:DelegatedCredentialReference>\n" +
            "$CAGRID_SSO_EMAIL_ID^jo@example.com";
    private static final String USERNAME = "jo";
    private static final GlobusCredential EXPECTED_CREDENTIAL =
        new GlobusCredential((PrivateKey) null, null);

    private WebSSOAuthoritiesPopulator populator;
    private PscUserDetailsService pscUserDetailsService;
    
    private User user;
    private WebSSOAuthoritiesPopulatorTest.NoopDelegatedCredentialAcquirer lastDelegatedCredentialAcquirer;
    private Exception expectedCredentialException;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        pscUserDetailsService = registerMockFor(PscUserDetailsService.class);
        populator = new TestPopulator();
        populator.setPscUserDetailsService(pscUserDetailsService);
        
        user = Fixtures.createUser(USERNAME, Role.STUDY_COORDINATOR, Role.SYSTEM_ADMINISTRATOR);

        expect(pscUserDetailsService.loadUserByUsername(USERNAME)).andStubReturn(user);
    }

    public void testThrowsAuthenticationExceptionForNoGridIdentity() throws Exception {
        String expectedResponse = "CAGRID_SSO_FIRST_NAME^Josephine";
        try {
            populator.getUserDetails(expectedResponse);
            fail("Exception not thrown");
        } catch (BadCredentialsException bce) {
            assertEquals("No grid identity in \"" + expectedResponse + '"', bce.getMessage());
        }
    }

    public void testThrowsAuthenticationExceptionForGridIdentityWithoutCN() throws Exception {
        String expectedResponse = "CAGRID_SSO_GRID_IDENTITY^/C=US/O=NU/OU=NUBIC/OU=Dorian";
        try {
            populator.getUserDetails(expectedResponse);
            fail("Exception not thrown");
        } catch (BadCredentialsException bce) {
            assertEquals(
                "Unable to extract username from grid identity /C=US/O=NU/OU=NUBIC/OU=Dorian; no CN",
                bce.getMessage());
        }
    }

    public void testPopulatedDetailsIsPscUser() throws Exception {
        replayMocks();
        UserDetails actual = populator.getUserDetails(WEBSSO_RESPONSE);
        verifyMocks();
        
        assertTrue("Returned details not a PSC User", actual instanceof User);
    }

    public void testUsernamePopulated() throws Exception {
        assertEquals("Wrong username", "jo", doPopulate().getUsername());
    }
    
    public void testRolesPopulated() throws Exception {
        User actual = doPopulate();

        assertEquals("User has wrong number of authorities", 2, actual.getAuthorities().length);
        assertEquals("User has wrong authority 0", Role.STUDY_COORDINATOR, actual.getAuthorities()[0]);
        assertEquals("User has wrong authority 1", Role.SYSTEM_ADMINISTRATOR, actual.getAuthorities()[1]);
    }

    public void testGridPersonalNamePopulated() throws Exception {
        User actual = doPopulate();
        assertEquals("Wrong first name", "Josephine", actual.getAttribute("cagrid.sso.name.first"));
        assertEquals("Wrong last name", "Miller", actual.getAttribute("cagrid.sso.name.last"));
    }

    public void testGridIdentityPopulated() throws Exception {
        User actual = doPopulate();
        assertEquals("Wrong grid identity",
            "/C=US/O=NU/OU=NUBIC/OU=Dorian/CN=jo", actual.getAttribute("cagrid.grid-identity"));
    }

    public void testGridEprXmlPopulated() throws Exception {
        User actual = doPopulate();
        assertEquals("Wrong EPR XML",
            "<ns1:DelegatedCredentialReference xmlns:ns1=\"http://cds.gaards.cagrid.org/CredentialDelegationService/DelegatedCredential/types\">\n" +
            " <ns2:EndpointReference xmlns:ns2=\"http://schemas.xmlsoap.org/ws/2004/03/addressing\">\n" +
            "  <ns2:Address>https://localhost:8443/wsrf/services/cagrid/DelegatedCredential</ns2:Address>\n" +
            "  <ns2:ReferenceProperties>\n" +
            "   <ns2:DelegatedCredentialKey xmlns:ns2=\"http://cds.gaards.cagrid.org/CredentialDelegationService/DelegatedCredential\">\n" +
            "    <ns3:delegationId xmlns:ns3=\"http://gaards.cagrid.org/cds\">3</ns3:delegationId>\n" +
            "   </ns2:DelegatedCredentialKey>\n" +
            "  </ns2:ReferenceProperties>\n" +
            "  <ns2:ReferenceParameters/>\n" +
            " </ns2:EndpointReference>\n" +
            "</ns1:DelegatedCredentialReference>\n",
            actual.getAttribute("cagrid.delegated-credential.xml")
        );
    }

    public void testDelegatedCredentialAcquired() throws Exception {
        User actual = doPopulate();
        assertSame(EXPECTED_CREDENTIAL,
            actual.getAttribute("cagrid.delegated-credential.value"));
    }

    public void testAuthenticationFailsWithoutDelegatedCredential() throws Exception {
        expectedCredentialException = new Exception("Fail");
        try {
            doPopulate();
            fail("Exception not thrown");
        } catch (AuthenticationException ae) {
            assertTrue(ae.getMessage().startsWith("Failed to resolve delegated credential"));
        }
    }

    private User doPopulate() {
        replayMocks();
        User actual = (User) populator.getUserDetails(WEBSSO_RESPONSE);
        verifyMocks();
        return actual;
    }

    private class NoopDelegatedCredentialAcquirer extends DelegatedCredentialAcquirer {
        private Exception expectedException;
        private String actualXml;

        public NoopDelegatedCredentialAcquirer(String xml, Exception expected) {
            super(null, null, null);
            actualXml = xml;
            this.expectedException = expected;
        }

        @Override
        protected GlobusCredential acquire() throws Exception {
            if (expectedException != null) throw expectedException;
            return EXPECTED_CREDENTIAL;
        }

        public String getActualXml() {
            return actualXml;
        }
    }

    private class TestPopulator extends WebSSOAuthoritiesPopulator {
        @Override
        protected DelegatedCredentialAcquirer createDelegatedCredentialAcquirer(String xml) {
            lastDelegatedCredentialAcquirer = new NoopDelegatedCredentialAcquirer(
                xml, expectedCredentialException);
            return lastDelegatedCredentialAcquirer;
        }
    }
}
