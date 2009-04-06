package edu.northwestern.bioinformatics.studycalendar.security.plugin.insecure;

import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationTestCase;
import org.acegisecurity.Authentication;
import org.acegisecurity.userdetails.UserDetailsService;
import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class InsecureAuthenticationSystemTest extends AuthenticationTestCase {
    private InsecureAuthenticationSystem system;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        system = new InsecureAuthenticationSystem();
        system.setBundleContext(bundleContext);
        userDetailsService = registerMockFor(UserDetailsService.class);
        bundleContext.addService(UserDetailsService.class, userDetailsService);
    }

    public void testTokenAuthProviderAuthenticatesAnyUsernameInTheService() throws Exception {
        expect(userDetailsService.loadUserByUsername("jojo")).andReturn(Fixtures.createUser("jojo", Role.STUDY_ADMIN));
        replayMocks();
        Authentication result = doTokenAuthenticate("jojo");
        assertTrue(result.isAuthenticated());
        verifyMocks();
    }

    public void testTokenAuthProviderGrantsAuthoritiesBasedOnRoles() throws Exception {
        expect(userDetailsService.loadUserByUsername("jojo")).andReturn(Fixtures.createUser("jojo", Role.STUDY_ADMIN, Role.SYSTEM_ADMINISTRATOR));
        replayMocks();
        Authentication result = doTokenAuthenticate("jojo");
        assertEquals("Wrong number of authorities", 2, result.getAuthorities().length);
        assertEquals("Wrong first authority", Role.STUDY_ADMIN, result.getAuthorities()[0]);
        assertEquals("Wrong first authority", Role.SYSTEM_ADMINISTRATOR, result.getAuthorities()[1]);
        verifyMocks();
    }
    
    public void testUserPassProviderAuthenticatesAnyUsername() {
        expect(userDetailsService.loadUserByUsername("jojo")).andReturn(Fixtures.createUser("jojo", Role.STUDY_ADMIN));
        replayMocks();
        Authentication result = doUsernamePasswordAuthenticate("jojo");
        assertTrue(result.isAuthenticated());
        verifyMocks();
    }

    public void testSystemHasValidEntryPoint() throws Exception {
        replayMocks();
        doInitialize();
        assertNotNull("System has no entryPoint", system.entryPoint());
    }

    private Authentication doTokenAuthenticate(String username) {
        doInitialize();
        return system.authenticationManager().authenticate(system.createTokenAuthenticationRequest(username));
    }

    private Authentication doUsernamePasswordAuthenticate(String username) {
        doInitialize();
        return system.authenticationManager().authenticate(system.createUsernamePasswordAuthenticationRequest(username, null));
    }

    private void doInitialize() {
        system.initialize(blankConfiguration());
    }
}