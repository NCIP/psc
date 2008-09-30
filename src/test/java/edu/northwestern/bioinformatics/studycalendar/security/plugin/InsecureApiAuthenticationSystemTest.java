package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.tools.configuration.MockConfiguration;
import org.acegisecurity.Authentication;
import org.acegisecurity.userdetails.UserDetailsService;
import static org.easymock.classextension.EasyMock.*;
import org.springframework.context.ApplicationContext;
import gov.nih.nci.security.acegi.csm.authentication.CSMAuthenticationProvider;

/**
 * @author Rhett Sutphin
 */
public class InsecureApiAuthenticationSystemTest extends StudyCalendarTestCase {
    private InsecureApiAuthenticationSystem system;

    private UserDetailsService userDetailsService;
    private ApplicationContext applicationContext;
    private CSMAuthenticationProvider csmAuthenticationProvider;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        system = new InsecureApiAuthenticationSystem();

        applicationContext = registerNiceMockFor(ApplicationContext.class);
        userDetailsService = registerMockFor(UserDetailsService.class);
        csmAuthenticationProvider = new CSMAuthenticationProvider();

        expect(applicationContext.getBean("pscUserDetailsService")).andReturn(userDetailsService).anyTimes();
        expect(applicationContext.getBean("csmAuthenticationProvider")).andReturn(csmAuthenticationProvider).anyTimes();
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

    private Authentication doTokenAuthenticate(String username) {
        system.initialize(applicationContext, new MockConfiguration());
        return system.authenticationManager().authenticate(system.createTokenAuthenticationRequest(username));
    }
}
