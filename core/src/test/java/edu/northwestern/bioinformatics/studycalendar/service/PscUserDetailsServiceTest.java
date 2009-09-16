package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.acegisecurity.DisabledException;
import static org.easymock.EasyMock.expect;

public class PscUserDetailsServiceTest extends StudyCalendarTestCase {
    private User user;
    private UserService userService;
    private PscUserDetailsServiceImpl service;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        userService = registerMockFor(UserService.class);

        service = new PscUserDetailsServiceImpl();
        service.setUserService(userService);

        user = Fixtures.createUser(1, "John", 1L, true);
    }

    public void testLoadUserGivesActualPscUser() throws Exception {
        expect(userService.getUserByName(user.getName())).andReturn(user);
        replayMocks();

        UserDetails actualUserDetails = service.loadUserByUsername(user.getName());
        verifyMocks();

        assertSame(user, actualUserDetails);
    }

    public void testLoadUnknownUserThrowsException() throws Exception {
        expect(userService.getUserByName(user.getName())).andReturn(null);
        replayMocks();

        try {
            service.loadUserByUsername(user.getName());
            fail("Exception not thrown");
        } catch (UsernameNotFoundException unfe) {
            // good
        }
        verifyMocks();
    }

    public void testLoadDisabledUserThrowsException() throws Exception {
        user.setActiveFlag(false);
        expect(userService.getUserByName(user.getName())).andReturn(user);
        replayMocks();

        try {
            service.loadUserByUsername(user.getName());
            fail("Exception not thrown");
        } catch (DisabledException de) {
            // good
        }
        verifyMocks();
    }
}
