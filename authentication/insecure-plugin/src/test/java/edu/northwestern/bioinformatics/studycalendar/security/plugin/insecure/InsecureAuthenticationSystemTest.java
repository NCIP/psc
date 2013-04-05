/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin.insecure;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationTestCase;
import org.acegisecurity.Authentication;

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
    }

    public void testTokenAuthProviderAuthenticatesAnyUsernameInTheService() throws Exception {
        userDetailsService.addUser("jojo", PscRole.STUDY_QA_MANAGER);
        replayMocks();
        Authentication result = doTokenAuthenticate("jojo");
        assertTrue(result.isAuthenticated());
        verifyMocks();
    }

    public void testTokenAuthProviderGrantsAuthoritiesBasedOnRoles() throws Exception {
        userDetailsService.addUser("jojo", PscRole.STUDY_QA_MANAGER, PscRole.SYSTEM_ADMINISTRATOR);
        replayMocks();
        Authentication result = doTokenAuthenticate("jojo");
        assertEquals("Wrong number of authorities", 2, result.getAuthorities().length);
        assertEquals("Wrong 1st authority", PscRole.STUDY_QA_MANAGER, result.getAuthorities()[0]);
        assertEquals("Wrong 2nd authority", PscRole.SYSTEM_ADMINISTRATOR, result.getAuthorities()[1]);
        verifyMocks();
    }
    
    public void testUserPassProviderAuthenticatesAnyUsername() {
        userDetailsService.addUser("jojo", PscRole.STUDY_QA_MANAGER);
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