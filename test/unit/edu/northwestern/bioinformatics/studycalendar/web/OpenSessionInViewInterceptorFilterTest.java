package edu.northwestern.bioinformatics.studycalendar.web;

import static org.easymock.classextension.EasyMock.*;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.orm.hibernate3.support.OpenSessionInViewInterceptor;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.PermissionDeniedDataAccessException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class OpenSessionInViewInterceptorFilterTest extends ControllerTestCase {
    private OpenSessionInViewInterceptorFilter filter;
    private ApplicationContext applicationContext;
    private OpenSessionInViewInterceptor interceptor;
    private FilterChain filterChain;

    protected void setUp() throws Exception {
        super.setUp();
        applicationContext = registerMockFor(WebApplicationContext.class);
        interceptor = registerMockFor(OpenSessionInViewInterceptor.class);
        servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, applicationContext);
        expect(applicationContext.getBean("openSessionInViewInterceptor")).andReturn(interceptor);
        filterChain = registerMockFor(FilterChain.class);

        filter = new OpenSessionInViewInterceptorFilter();
        MockFilterConfig filterConfig = new MockFilterConfig(servletContext);
        filter.init(filterConfig);
    }

    public void testBasicBehavior() throws Exception {
        expect(interceptor.preHandle(request, response, null)).andReturn(true);
        filterChain.doFilter(request, response);
        interceptor.postHandle(request, response, null, null);
        interceptor.afterCompletion(request, response, null, null);

        doFilter();
    }

    public void testExceptionInFilterChainBehavior() throws Exception {
        ServletException exception = new ServletException("Uh oh");

        expect(interceptor.preHandle(request, response, null)).andReturn(true);
        filterChain.doFilter(request, response);
        expectLastCall().andThrow(exception);
        // postHandle should not be called
        interceptor.afterCompletion(request, response, null, null);

        try {
            doFilter();
            fail("Exception not propagated");
        } catch (ServletException se) {
            assertEquals(exception, se);
        }
    }

    public void testExceptionInPostHandleBehavior() throws Exception {
        DataAccessException exception = new PermissionDeniedDataAccessException("Uh oh", null);

        expect(interceptor.preHandle(request, response, null)).andReturn(true);
        filterChain.doFilter(request, response);
        interceptor.postHandle(request, response, null, null);
        expectLastCall().andThrow(exception);
        interceptor.afterCompletion(request, response, null, null);

        try {
            doFilter();
            fail("Exception not propagated");
        } catch (DataAccessException dae) {
            assertEquals(exception, dae);
        }
    }
    
    public void testIfNotHandled() throws Exception {
        expect(interceptor.preHandle(request, response, null)).andReturn(false);
        filterChain.doFilter(request, response);
        interceptor.postHandle(request, response, null, null);
        // after completion should not be called

        doFilter();
    }

    private void doFilter() throws IOException, ServletException {
        replayMocks();
        filter.doFilter(request, response, filterChain);
        verifyMocks();
    }
}
