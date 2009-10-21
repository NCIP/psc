package edu.northwestern.bioinformatics.studycalendar.security.internal;

import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationTestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.context.SecurityContextHolder;

import javax.servlet.FilterChain;

/**
 * @author Jalpa Patel
 */
public class RequestAuthenticationSocketTest extends AuthenticationTestCase {
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
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(Fixtures.createUser("joe"), "secret"));
        filter.doFilter(request, response, filterChain);
        assertNotNull(request.getAttribute("authentication"));
    }
}
