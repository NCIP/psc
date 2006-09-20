package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockHttpServletRequest;

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
        ApplicationSecurityManager.setUser(request, "jim");
        assertNotNull("Session attribute not set", session.getAttribute(ApplicationSecurityManager.USER));
    }

    public void testGetUser() throws Exception {
        session.setAttribute(ApplicationSecurityManager.USER, "james");
        assertEquals("james", ApplicationSecurityManager.getUser(request));
    }

    public void testGetWhenNotSet() throws Exception {
        assertNull("Incorrect response for non logged-in user", ApplicationSecurityManager.getUser(request));
    }

    public void testGetWhenNoSessionDoesNotCreateSession() throws Exception {
        request.setSession(null);
        assertNull(ApplicationSecurityManager.getUser(request));
        assertNull(request.getSession(false));
    }

    public void testRemoveUser() throws Exception {
        session.setAttribute(ApplicationSecurityManager.USER, "leaving");
        ApplicationSecurityManager.removeUserSession(request);
        assertNull("Session attribute not cleared", session.getAttribute(ApplicationSecurityManager.USER));
    }
}
