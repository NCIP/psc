package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.FilterChain;
import java.io.IOException;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;

/**
 * @author Rhett Sutphin
 */
public class MultipleFilterFilterTest extends StudyCalendarTestCase {
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain chain;

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
            InvocationRecordingFilter filter = filters[i];
            assertTrue("Filter " + i + " not invoked", filter.isInvoked());
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
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
            super.doFilter(servletRequest, servletResponse, filterChain);
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private static class StoppingFilter extends InvocationRecordingFilter { }
}
