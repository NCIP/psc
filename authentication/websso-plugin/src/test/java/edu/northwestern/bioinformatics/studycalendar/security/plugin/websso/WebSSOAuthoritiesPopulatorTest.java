package edu.northwestern.bioinformatics.studycalendar.security.plugin.websso;

import java.util.HashSet;
import java.util.Set;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.security.acegi.PscUserDetailsService;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationTestCase;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class WebSSOAuthoritiesPopulatorTest extends AuthenticationTestCase {
    private WebSSOAuthoritiesPopulator populator;
    private PscUserDetailsService pscUserDetailsService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        pscUserDetailsService = registerMockFor(PscUserDetailsService.class);
        populator = new WebSSOAuthoritiesPopulator();
        populator.setPscUserDetailsService(pscUserDetailsService);
    }

    public void testPopulation() throws Exception {
        String expectedSsoUserId = "CAGRID_SSO_EMAIL_ID^jo@org.org$CAGRID_SSO_FIRST_NAME^Josephine$CAGRID_SSO_LAST_NAME^Miller$CAGRID_SSO_GRID_IDENTITY^/CN=ablablab";
        String expectedAddress = "ablablab";    //CAGRID_SSO_GRID_IDENTITY has /CN=user_name format. For ex : /C=US/O=NCI/OU=CBIIT/OU=CCTS/OU=DEV/OU=Dorian IdP/CN=ccts@nih.gov
        User user = new User();
        Set<UserRole> userRoles = new HashSet<UserRole>();
        userRoles.add(new UserRole(user , Role.STUDY_ADMIN));
        user.setUserRoles(userRoles);
        user.setActiveFlag(true);
        user.setFirstName("Rupert");
        user.setLastName("Murdoch");
        expect(pscUserDetailsService.loadUserByUsername(expectedAddress)).andReturn(
                user);
        replayMocks();

        UserDetails details = populator.getUserDetails(expectedSsoUserId);
        assertTrue("Returned details not an instance of  PSC User", details instanceof User);
        User ssoDetails = (User) details;
        assertEquals("First name not preserved", "Josephine", ssoDetails.getAttribute(WebSSOAuthoritiesPopulator.CAGRID_SSO_FIRST_NAME));
        assertEquals("Last name not preserved", "Miller", ssoDetails.getAttribute(WebSSOAuthoritiesPopulator.CAGRID_SSO_LAST_NAME));
        assertEquals("User has wrong number of authorities", 1, ssoDetails.getAuthorities().length);
        assertEquals("User has wrong authority", Role.STUDY_ADMIN, ssoDetails.getAuthorities()[0]);
    }
}
