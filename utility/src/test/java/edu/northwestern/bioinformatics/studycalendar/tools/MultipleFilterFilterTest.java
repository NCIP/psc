/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools;

import junit.framework.TestCase;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class MultipleFilterFilterTest extends TestCase {
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain chain;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain = new MockFilterChain();
    }

    public void testAllFiltersInvokedWhenAllContinue() throws Exception {
        InvocationRecordingFilter[] filters = new InvocationRecordingFilter[] {
            new ContinuingFilter(), new ContinuingFilter(), new ContinuingFilter()
        };

        doFilter(filters);
        for (int i = 0; i < filters.length; i++) {
            assertTrue("Filter " + i + " not invoked", filters[i].isInvoked());
        }
        assertChainContinued();
    }

    public void testSetFiltersWorks() throws Exception {
        List<ContinuingFilter> filters = Arrays.asList(
            new ContinuingFilter(), new ContinuingFilter(), new ContinuingFilter()
        );

        MultipleFilterFilter filter = new MultipleFilterFilter();
        filter.setFilters(filters);
        filter.doFilter(request, response, chain);

        for (int i = 0; i < filters.size(); i++) {
            assertTrue("Filter " + i + " not invoked", filters.get(i).isInvoked());
        }
        assertChainContinued();
    }
    
    public void testFiltersStopAtLastInvoked() throws Exception {
        InvocationRecordingFilter[] filters = new InvocationRecordingFilter[] {
            new ContinuingFilter(), new StoppingFilter(), new ContinuingFilter()
        };

        doFilter(filters);
        assertTrue("First not invoked", filters[0].isInvoked());
        assertTrue("Second not invoked", filters[1].isInvoked());
        assertFalse("Third incorrectly invoked", filters[2].isInvoked());
        assertChainNotContinued();
    }

    public void testFiltersStopOnException() throws Exception {
        InvocationRecordingFilter[] filters = new InvocationRecordingFilter[] {
            new ContinuingFilter(), new ErroringFilter(), new ContinuingFilter()
        };

        try {
            doFilter(filters);
            fail("Exception not propagated");
        } catch (ServletException se) {
            assertEquals(se.getMessage(), ErroringFilter.MESSAGE);
            assertTrue("First not invoked", filters[0].isInvoked());
            assertTrue("Second not invoked", filters[1].isInvoked());
            assertFalse("Third incorrectly invoked", filters[2].isInvoked());
            assertChainNotContinued();
        }
    }

    public void testFiltersStopOnRuntimeException() throws Exception {
        InvocationRecordingFilter[] filters = new InvocationRecordingFilter[] {
            new ContinuingFilter(), new RuntimeErroringFilter(), new ContinuingFilter()
        };

        try {
            doFilter(filters);
            fail("Exception not propagated");
        } catch (IllegalStateException ise) {
            assertEquals(ise.getMessage(), RuntimeErroringFilter.MESSAGE);
            assertTrue("First not invoked", filters[0].isInvoked());
            assertTrue("Second not invoked", filters[1].isInvoked());
            assertFalse("Third incorrectly invoked", filters[2].isInvoked());
            assertChainNotContinued();
        }
    }

    public void testExceptionIfNoFiltersProvided() throws Exception {
        try {
            new MultipleFilterFilter().afterPropertiesSet();
            fail("Exception not thrown");
        } catch (IllegalStateException ise) {
            assertEquals("No filters configured", ise.getMessage());
        }
    }

    private void doFilter(Filter[] filters) throws IOException, ServletException {
        new MultipleFilterFilter(filters).doFilter(request, response, chain);
    }

    private void assertChainContinued() {
        assertSame("Chain not continued", request, chain.getRequest());
    }

    private void assertChainNotContinued() {
        assertNull("Chain incorrectly continued", chain.getRequest());
    }

    private static abstract class InvocationRecordingFilter implements Filter {
        private boolean invoked;

        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
            invoked = true;
        }

        public boolean isInvoked() {
            return invoked;
        }

        public void init(FilterConfig filterConfig) throws ServletException { }
        public void destroy() { }
    }

    private static class ContinuingFilter extends InvocationRecordingFilter {
        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
            super.doFilter(servletRequest, servletResponse, filterChain);
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private static class StoppingFilter extends InvocationRecordingFilter { }

    private static class ErroringFilter extends InvocationRecordingFilter {
        public static final String MESSAGE = "This is the end";

        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
            super.doFilter(servletRequest, servletResponse, filterChain);
            throw new ServletException(MESSAGE);
        }
    }

    private static class RuntimeErroringFilter extends InvocationRecordingFilter {
        public static final String MESSAGE = "This is the unexpected end";

        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
            super.doFilter(servletRequest, servletResponse, filterChain);
            throw new IllegalStateException(MESSAGE);
        }
    }
}
