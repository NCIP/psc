package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createUser;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import org.acegisecurity.context.SecurityContextHolder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class ApplicationSecurityManagerTest extends StudyCalendarTestCase {
    private MockHttpSession session;
    private MockHttpServletRequest request;
    private ApplicationSecurityManager applicationSecurityManager;
    private UserService userService;

    protected void setUp() throws Exception {
        super.setUp();
        session = new MockHttpSession();
        request = new MockHttpServletRequest();
        request.setSession(session);
        userService = registerMockFor(UserService.class);
        applicationSecurityManager = new ApplicationSecurityManager();
        applicationSecurityManager.setUserService(userService);
    }

    public void testSetUser() throws Exception {
        SecurityContextHolderTestHelper.setSecurityContext(createUser("jim") , "pass");
        assertNotNull("Session attribute not set", applicationSecurityManager.getUserName());
    }

    public void testGetUser() throws Exception {
        SecurityContextHolderTestHelper.setSecurityContext(createUser("james"), "pass");
        assertEquals("james", applicationSecurityManager.getUserName());
    }

    public void testGetWhenNotSet() throws Exception {
        assertNull("Incorrect response for non logged-in user", applicationSecurityManager.getUserName());
    }

    public void testGetWhenNoSessionDoesNotCreateSession() throws Exception {
        request.setSession(null);
        assertNull(applicationSecurityManager.getUserName());
        assertNull(request.getSession(false));
    }

    public void testRemoveUser() throws Exception {
        SecurityContextHolderTestHelper.setSecurityContext(createUser("leaving"), "pass");
        applicationSecurityManager.removeUserSession();
        assertNull("Session attribute not cleared", SecurityContextHolder.getContext().getAuthentication());
    }

    public void testGetFreshUserReloadsUser() throws Exception {
        SecurityContextHolderTestHelper.setSecurityContext(createUser("jim") , "pass");
        User expectedUser = createUser("jim");
        expect(userService.getUserByName("jim", false)).andReturn(expectedUser);

        replayMocks();
        User actualUser = applicationSecurityManager.getFreshUser();
        verifyMocks();
        assertSame(expectedUser, actualUser);
    }

    public void testFreshUserIsNullWhenNotLoggedIn() throws Exception {
        replayMocks();
        assertNull(applicationSecurityManager.getFreshUser());
        verifyMocks();
    }

    public void testGetFreshUserWithAssignmentsReloadsUser() throws Exception {
        SecurityContextHolderTestHelper.setSecurityContext(createUser("jim") , "pass");
        User expectedUser = createUser("jim");
        expect(userService.getUserByName("jim", true)).andReturn(expectedUser);

        replayMocks();
        User actualUser = applicationSecurityManager.getFreshUser(true);
        verifyMocks();
        assertSame(expectedUser, actualUser);
    }

    public void testFreshUserWithAssignmentIsNullWhenNotLoggedIn() throws Exception {
        replayMocks();
        assertNull(applicationSecurityManager.getFreshUser(true));
        verifyMocks();
    }
}
