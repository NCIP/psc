package edu.northwestern.bioinformatics.studycalendar.web.setup;

import edu.northwestern.bioinformatics.studycalendar.core.setup.SetupStatus;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.FilterChain;

import static org.easymock.EasyMock.*;

/**
 * @author Jalpa Patel
 */
public class PostAuthenticationSetupFilterTest extends WebTestCase {
    private FilterChain filterChain;
    private PostAuthenticationSetupFilter filter;
    private SetupStatus setupStatus;

    public void setUp() throws Exception {
        super.setUp();
        WebApplicationContext applicationContext = registerMockFor(WebApplicationContext.class);
        servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, applicationContext);
        filterChain = registerMockFor(FilterChain.class);

        filter = new PostAuthenticationSetupFilter();
        setupStatus = registerMockFor(SetupStatus.class);

        expect(applicationContext.getBean("setupStatus")).andReturn(setupStatus);
        replayMocks();
        filter.init(new MockFilterConfig(servletContext));
        verifyMocks();
        resetMocks();

        request.setContextPath("/pscTest");
    }

    public void testRedirectsToPostAuthenticationSetupWhenNecessary() throws Exception {
        expect(setupStatus.isPostAuthenticationSetupNeeded()).andReturn(true);
        replayMocks();

        filter.doFilter(request, response, filterChain);
        verifyMocks();

        assertRedirectedAppropriately(response);
    }

    public void testFallThroughWhenNotNecessary() throws Exception {
        filterChain.doFilter(request, response);
        expect(setupStatus.isPostAuthenticationSetupNeeded()).andReturn(false);
        replayMocks();

        filter.doFilter(request, response, filterChain);
        verifyMocks();
    }

    public void testDoesRecheckAfterOneSetupNeeded() throws Exception {
        expect(setupStatus.isPostAuthenticationSetupNeeded()).andReturn(true).times(2);
        replayMocks();

        filter.doFilter(request, response, filterChain);
        assertRedirectedAppropriately(response);

        MockHttpServletResponse response2 = new MockHttpServletResponse();
        filter.doFilter(request, response2, filterChain);
        assertRedirectedAppropriately(response2);

        verifyMocks();
    }

    public void testDoesNotRecheckAfterOneNoSetupNeeded() throws Exception {
        expect(setupStatus.isPostAuthenticationSetupNeeded()).andReturn(false).once();
        filterChain.doFilter(request, response);
        expectLastCall().times(2);
        replayMocks();

        filter.doFilter(request, response, filterChain);
        filter.doFilter(request, response, filterChain);
        verifyMocks();
    }

    private void assertRedirectedAppropriately(MockHttpServletResponse resp) {
        assertNotNull(resp.getRedirectedUrl());
        assertEquals("/pscTest/setup/postAuthenticationSetup", resp.getRedirectedUrl());
    }
}

