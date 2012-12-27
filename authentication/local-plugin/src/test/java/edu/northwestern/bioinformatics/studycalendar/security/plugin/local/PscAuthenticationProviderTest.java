/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin.local;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationTestCase;
import org.acegisecurity.Authentication;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;

import static org.easymock.EasyMock.expect;

/**
 * @author Jalpa Patel
 */
public class PscAuthenticationProviderTest extends AuthenticationTestCase {
    private static final String USERNAME = "bill";

    private PscAuthenticationProvider provider;
    private Authentication authentication;
    private PscAuthenticationHelper pscAuthenticationHelper;
    private PscUserDetailsService userDetailsService;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        provider =  new PscAuthenticationProvider();
        userDetailsService =   registerMockFor(PscUserDetailsService.class);
        pscAuthenticationHelper = registerMockFor(PscAuthenticationHelper.class);
        provider.setPscAuthenticationHelper(pscAuthenticationHelper);
        provider.setUserDetailsService(userDetailsService);
        authentication = new UsernamePasswordAuthenticationToken(USERNAME, "dc");
    }

    public void testAuthenticatewithValidUserCredentials() throws Exception {
        PscUser expectedUser = AuthorizationObjectFactory.
            createPscUser(authentication.getName(), PscRole.AE_REPORTER);
        expect(pscAuthenticationHelper.authenticate(authentication)).andReturn(true);
        expect(userDetailsService.loadUserByUsername(USERNAME)).andReturn(expectedUser);
        replayMocks();
        Authentication result =  provider.authenticate(authentication);
        verifyMocks();

        assertEquals("Wrong authority", PscRole.AE_REPORTER, result.getAuthorities()[0]);
        assertEquals("Wrong name", authentication.getName(), result.getName());
    }

    public void testAuthenticateWithWrongUserCredentials() throws Exception {
        expect(pscAuthenticationHelper.authenticate(authentication)).andReturn(false);
        replayMocks();
        try {
            provider.authenticate(authentication);
            fail("Exception not thrown");
        } catch (Exception e) {
            assertEquals("Invalid username or password", e.getMessage());
        }
        verifyMocks();
    }
}
