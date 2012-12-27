/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools.spring;

import gov.nih.nci.cabig.ctms.testing.MockRegistry;
import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.FilterChain;

import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class ServletWebContextPathAwareFilterTest extends TestCase {
    private ServletWebContextPathAwareFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterConfig config;
    private FilterChain chain;
    private MockServletContext servletContext;

    private MockRegistry mocks;
    private ApplicationContext applicationContext;
    private ServletWebContextPathPostProcessor processor;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mocks = new MockRegistry();

        servletContext = new MockServletContext();
        request = new MockHttpServletRequest(servletContext);
        response = new MockHttpServletResponse();
        config = new MockFilterConfig(servletContext);
        chain = mocks.registerMockFor(FilterChain.class);

        applicationContext = mocks.registerMockFor(WebApplicationContext.class);
        processor = mocks.registerMockFor(ServletWebContextPathPostProcessor.class);
        servletContext.setAttribute(
            WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, applicationContext);
        expect(applicationContext.getBean("servletWebContextPathPostProcessor")).
            andStubReturn(processor);

        filter = new ServletWebContextPathAwareFilter();
    }

    public void testBeanNameInitializedFromConfig() throws Exception {
        mocks.resetMocks();

        config.addInitParameter("processorBeanName", "joe");
        expect(applicationContext.getBean("joe")).andReturn(processor);

        mocks.replayMocks();
        filter.init(config);
        mocks.verifyMocks();

        assertSame(processor, filter.getTargetPostProcessor());
    }

    public void testDefaultBeanNameIsPostProcessorClassName() throws Exception {
        expect(applicationContext.getBean("servletWebContextPathPostProcessor")).andReturn(processor);

        mocks.replayMocks();
        filter.init(config);
        mocks.verifyMocks();

        assertSame(processor, filter.getTargetPostProcessor());
    }

    public void testRequestRegisteredAndFilterContinued() throws Exception {
        /* expect */ processor.registerRequest(request);
        /* expect */ chain.doFilter(request, response);

        mocks.replayMocks();
        filter.init(config);
        filter.doFilter(request, response, chain);
        mocks.verifyMocks();
    }
}
