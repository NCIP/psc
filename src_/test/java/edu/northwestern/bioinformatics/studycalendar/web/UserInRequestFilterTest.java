package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.SecurityContextHolderTestHelper;

import javax.servlet.FilterChain;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Rhett Sutphin
 */
public class UserInRequestFilterTest extends StudyCalendarTestCase {
    private UserInRequestFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    protected void setUp() throws Exception {
        super.setUp();
        filterChain = registerMockFor(FilterChain.class);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        filter = new UserInRequestFilter();

        filterChain.doFilter(request, response);
        replayMocks();
    }
    
    public void testAttributeSetWhenLoggedIn() throws Exception {
        SecurityContextHolderTestHelper.setSecurityContext("cab", "pass");

        filter.doFilter(request, response, filterChain);
        verifyMocks();

        assertEquals("cab", request.getAttribute("user"));
    }

    public void testAttributeNotSetWhenNotLoggedIn() throws Exception {
        ApplicationSecurityManager.removeUserSession();

        filter.doFilter(request, response, filterChain);
        verifyMocks();

        assertFalse(request.getAttributeNames().hasMoreElements());
    }
}
