package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.mvc.Controller;

/**
 * @author Rhett Sutphin
 */
public class ControllerToolsTest extends StudyCalendarTestCase {
    private MockHttpServletRequest request;
    private ControllerTools tools;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        request = new MockHttpServletRequest();
        tools = new ControllerTools();
    }

    public void testAjaxRequestWhenTrue() throws Exception {
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        assertTrue(tools.isAjaxRequest(request));
    }

    public void testAjaxRequestWithoutHeader() throws Exception {
        assertFalse(tools.isAjaxRequest(request));
    }
    
    public void testAjaxRequestWithOtherValue() throws Exception {
        request.addHeader("X-Requested-With", "Firefox");
        assertFalse(tools.isAjaxRequest(request));
    }

    public void testGetCurrentUser() throws Exception {
        assertNull(tools.getCurrentUser(request));
        User user = Fixtures.createUser("jimbo");
        request.setAttribute("currentUser", user);
        assertSame(user, tools.getCurrentUser(request));
    }
}
