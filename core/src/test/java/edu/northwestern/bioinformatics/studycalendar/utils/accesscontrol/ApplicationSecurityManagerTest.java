package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockHttpServletRequest;
import org.acegisecurity.context.SecurityContextHolder;

/**
 * @author Rhett Sutphin
 */
public class ApplicationSecurityManagerTest extends StudyCalendarTestCase {
    private MockHttpSession session;
    private MockHttpServletRequest request;

    protected void setUp() throws Exception {
        super.setUp();
        session = new MockHttpSession();
        request = new MockHttpServletRequest();
        request.setSession(session);
    }

    public void testSetUser() throws Exception {
        SecurityContextHolderTestHelper.setSecurityContext("jim" , "pass");
        assertNotNull("Session attribute not set", ApplicationSecurityManager.getUser());
    }

    public void testGetUser() throws Exception {
        SecurityContextHolderTestHelper.setSecurityContext("james" , "pass");
        assertEquals("james", ApplicationSecurityManager.getUser());
    }

    public void testGetWhenNotSet() throws Exception {
        assertNull("Incorrect response for non logged-in user", ApplicationSecurityManager.getUser());
    }

    public void testGetWhenNoSessionDoesNotCreateSession() throws Exception {
        request.setSession(null);
        assertNull(ApplicationSecurityManager.getUser());
        assertNull(request.getSession(false));
    }

    public void testRemoveUser() throws Exception {
        SecurityContextHolderTestHelper.setSecurityContext("leaving" , "pass");
        ApplicationSecurityManager.removeUserSession();
        assertNull("Session attribute not cleared", SecurityContextHolder.getContext().getAuthentication());
    }
}
