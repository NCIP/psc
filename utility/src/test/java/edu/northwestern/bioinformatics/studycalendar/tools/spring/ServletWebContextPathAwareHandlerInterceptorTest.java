/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools.spring;

import gov.nih.nci.cabig.ctms.testing.MockRegistry;
import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Jalpa Patel
 * @author Rhett Sutphin
 */
public class ServletWebContextPathAwareHandlerInterceptorTest extends TestCase {
    private ServletWebContextPathAwareHandlerInterceptor interceptor;

    private MockRegistry mocks;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private ServletWebContextPathPostProcessor processor;

    public void setUp() throws Exception {
        super.setUp();
        mocks = new MockRegistry();

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        processor = mocks.registerMockFor(ServletWebContextPathPostProcessor.class);

        interceptor = new ServletWebContextPathAwareHandlerInterceptor();
        interceptor.setServletWebContextPathPostProcessor(processor);
    }

    public void testRegistersRequestAndContinues() throws Exception {
        /* expect */ processor.registerRequest(request);

        mocks.replayMocks();
        assertTrue(interceptor.preHandle(request, response, null /* dc */));
        mocks.verifyMocks();
    }
}
