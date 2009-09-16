package edu.northwestern.bioinformatics.studycalendar.security.internal;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;

/**
 * @author Jalpa Patel
 */
public class RequestAuthenticationSocketTest extends StudyCalendarTestCase {
    private RequestAuthenticationSocket filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = registerMockFor(FilterChain.class);
        filter = new RequestAuthenticationSocket();
    }

    public void testAddAuthenticationToRequest() throws Exception {
        SecurityContextHolderTestHelper.setSecurityContext(Fixtures.createUser("joe") , "user");
        filter.doFilter(request, response, filterChain);
        assertNotNull(request.getAttribute("authentication"));
    }
}
