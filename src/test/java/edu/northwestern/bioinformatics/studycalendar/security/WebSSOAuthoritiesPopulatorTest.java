package edu.northwestern.bioinformatics.studycalendar.security;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class WebSSOAuthoritiesPopulatorTest extends StudyCalendarTestCase {
    private WebSSOAuthoritiesPopulator populator;
    private UserDetailsService userDetailsService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        userDetailsService = registerMockFor(UserDetailsService.class);
        populator = new WebSSOAuthoritiesPopulator();
        populator.setUserDetailsService(userDetailsService);
    }

    public void testPopulation() throws Exception {
        String expectedSsoUserId = "CAGRID_SSO_EMAIL_ID^jo@org.org$CAGRID_SSO_FIRST_NAME^Josephine$CAGRID_SSO_LAST_NAME^Miller$CAGRID_SSO_GRID_IDENTITY^/CN=ablablab";
        String expectedAddress = "ablablab";    //CAGRID_SSO_GRID_IDENTITY has /CN=user_name format. For ex : /C=US/O=NCI/OU=CBIIT/OU=CCTS/OU=DEV/OU=Dorian IdP/CN=ccts@nih.gov
        expect(userDetailsService.loadUserByUsername(expectedAddress)).andReturn(
                new User(expectedAddress, "ignored", true, true, true, true, new Role[]{Role.STUDY_ADMIN}));
        replayMocks();

        UserDetails details = populator.getUserDetails(expectedSsoUserId);
        assertTrue("Returned details not an instance of WebSSOUser", details instanceof WebSSOUser);
        WebSSOUser ssoDetails = (WebSSOUser) details;
        assertEquals("First name not preserved", "Josephine", ssoDetails.getFirstName());
        assertEquals("Last name not preserved", "Miller", ssoDetails.getLastName());
        assertEquals("User has wrong number of authorities", 1, ssoDetails.getAuthorities().length);
        assertEquals("User has wrong authority", Role.STUDY_ADMIN, ssoDetails.getAuthorities()[0]);
    }
}
