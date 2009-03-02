package edu.northwestern.bioinformatics.studycalendar.security.plugin.local;

import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import org.acegisecurity.Authentication;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UserDetails;
import static org.easymock.EasyMock.expect;

/**
 * @author Jalpa Patel
 */
public class PscAuthenticationProviderTest extends AuthenticationTestCase {
    private PscAuthenticationProvider provider;
    private Authentication authentication;
    private PscAuthenticationHelper pscAuthenticationHelper;
    private UserDetailsService userDetailsService;
    private UserDetails user;
    public void setUp() throws Exception {
        super.setUp();
        provider =  new PscAuthenticationProvider();
        userDetailsService =   registerMockFor(UserDetailsService.class);
        pscAuthenticationHelper = registerMockFor(PscAuthenticationHelper.class);
        provider.setPscAuthenticationHelper(pscAuthenticationHelper);
        provider.setUserDetailsService(userDetailsService);
        user  = Fixtures.createUser("user",Role.STUDY_ADMIN);
        authentication = new UsernamePasswordAuthenticationToken("user","user");


    }

    public void testAuthenticatewithValidUserCredentials() throws Exception {
        expect(pscAuthenticationHelper.authenticate(authentication)).andReturn(true);
        expect(userDetailsService.loadUserByUsername("user")).andReturn(user);
        replayMocks();
        Authentication result =  provider.authenticate(authentication);
        verifyMocks();

        assertEquals("Wrong authority",Role.STUDY_ADMIN,result.getAuthorities()[0]);
        assertEquals("Wrong name",authentication.getName(),result.getName());
    }

    public void testAuthenticateWithWrongUserCredentials() throws Exception {
        expect(pscAuthenticationHelper.authenticate(authentication)).andReturn(false);
        replayMocks();
         try {
               provider.authenticate(authentication);
        } catch (Exception e) {
               assertEquals("Invalid username or password", e.getMessage());

        }
        verifyMocks();
    }
}
