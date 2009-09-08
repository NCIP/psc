package edu.northwestern.bioinformatics.studycalendar.web.setup;

import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.setup.SetupStatus;

import javax.servlet.FilterChain;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.mock.web.MockFilterConfig;
import static org.easymock.EasyMock.expect;

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

        assertNotNull(response.getRedirectedUrl());
        assertEquals("/pscTest/setup/postAuthenticationSetup", response.getRedirectedUrl());
    }

    public void testFallThroughWhenNotNecessary() throws Exception {
        filterChain.doFilter(request, response);
        expect(setupStatus.isPostAuthenticationSetupNeeded()).andReturn(false);
        replayMocks();

        filter.doFilter(request, response, filterChain);
        verifyMocks();
    }
}

