/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import org.acegisecurity.context.SecurityContextHolder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory.createPscUser;

/**
 * @author Rhett Sutphin
 */
public class ApplicationSecurityManagerTest extends StudyCalendarTestCase {
    private MockHttpSession session;
    private MockHttpServletRequest request;
    private ApplicationSecurityManager applicationSecurityManager;

    protected void setUp() throws Exception {
        super.setUp();
        session = new MockHttpSession();
        request = new MockHttpServletRequest();
        request.setSession(session);
        applicationSecurityManager = new ApplicationSecurityManager();
    }

    public void testSetUser() throws Exception {
        SecurityContextHolderTestHelper.setSecurityContext(createPscUser("jim") , "pass");
        assertNotNull("Session attribute not set", applicationSecurityManager.getUserName());
    }

    public void testGetUser() throws Exception {
        SecurityContextHolderTestHelper.setSecurityContext(createPscUser("james"), "pass");
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
        SecurityContextHolderTestHelper.setSecurityContext(createPscUser("leaving"), "pass");
        applicationSecurityManager.removeUserSession();
        assertNull("Session attribute not cleared", SecurityContextHolder.getContext().getAuthentication());
    }
}
