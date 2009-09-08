package edu.northwestern.bioinformatics.studycalendar.web.setup;

import edu.northwestern.bioinformatics.studycalendar.core.setup.SetupStatus;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import static org.easymock.classextension.EasyMock.expect;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.FilterChain;

/**
 * @author Rhett Sutphin
 */
public class PreAuthenticationSetupFilterTest extends WebTestCase {
    private FilterChain filterChain;
    private PreAuthenticationSetupFilter filter;
    private SetupStatus setupStatus;

    public void setUp() throws Exception {
        super.setUp();
        WebApplicationContext applicationContext = registerMockFor(WebApplicationContext.class);
        servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, applicationContext);
        filterChain = registerMockFor(FilterChain.class);

        filter = new PreAuthenticationSetupFilter();
        setupStatus = registerMockFor(SetupStatus.class);

        expect(applicationContext.getBean("setupStatus")).andReturn(setupStatus);
        replayMocks();
        filter.init(new MockFilterConfig(servletContext));
        verifyMocks();
        resetMocks();

        request.setContextPath("/psc7");
    }

    public void testRedirectsToPreAuthenticationSetupWhenNecessary() throws Exception {
        expect(setupStatus.isPreAuthenticationSetupNeeded()).andReturn(true);
        replayMocks();

        filter.doFilter(request, response, filterChain);
        verifyMocks();

        assertNotNull(response.getRedirectedUrl());
        assertEquals("/psc7/setup/preAuthenticationSetup", response.getRedirectedUrl());
    }

    public void testFallThroughWhenNotNecessary() throws Exception {
        filterChain.doFilter(request, response);
        expect(setupStatus.isPreAuthenticationSetupNeeded()).andReturn(false);
        replayMocks();

        filter.doFilter(request, response, filterChain);
        verifyMocks();
    }
}
