/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.setup;

import edu.northwestern.bioinformatics.studycalendar.core.setup.SetupStatus;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.FilterChain;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.expect;

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

        assertRedirectedAppropriately(response);
    }

    public void testFallThroughWhenNotNecessary() throws Exception {
        filterChain.doFilter(request, response);
        expect(setupStatus.isPreAuthenticationSetupNeeded()).andReturn(false);
        replayMocks();

        filter.doFilter(request, response, filterChain);
        verifyMocks();
    }

    public void testDoesRecheckAfterOneSetupNeeded() throws Exception {
        expect(setupStatus.isPreAuthenticationSetupNeeded()).andReturn(true).times(2);
        replayMocks();

        filter.doFilter(request, response, filterChain);
        assertRedirectedAppropriately(response);

        MockHttpServletResponse response2 = new MockHttpServletResponse();
        filter.doFilter(request, response2, filterChain);
        assertRedirectedAppropriately(response2);

        verifyMocks();
    }

    public void testDoesNotRecheckAfterOneNoSetupNeeded() throws Exception {
        expect(setupStatus.isPreAuthenticationSetupNeeded()).andReturn(false).once();
        filterChain.doFilter(request, response);
        expectLastCall().times(2);
        replayMocks();

        filter.doFilter(request, response, filterChain);
        filter.doFilter(request, response, filterChain);
        verifyMocks();
    }

    private void assertRedirectedAppropriately(MockHttpServletResponse resp) {
        assertNotNull(resp.getRedirectedUrl());
        assertEquals("/psc7/setup/preAuthenticationSetup", resp.getRedirectedUrl());
    }
}
