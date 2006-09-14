package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.utils.ContextRetainingFilterAdapter;
import org.springframework.orm.hibernate3.support.OpenSessionInViewInterceptor;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A filter which implements the Open Session In View pattern.  Different
 * from the one built into Spring because this one delegates to an instance of
 * {@link OpenSessionInViewInterceptor} configured in the application context.
 * This permits the use of the same interceptor for deployed code & unit tests.
 *
 * @see org.springframework.orm.hibernate3.support.OpenSessionInViewFilter
 * @author Rhett Sutphin
 */
public class OpenSessionInViewInterceptorFilter extends ContextRetainingFilterAdapter {
    /**
     * @see OpenSessionInViewInterceptor
     * @see org.springframework.web.servlet.HandlerInterceptor#afterCompletion
     * @param request the request
     * @param response the response
     * @param chain the chain
     * @throws IOException
     * @throws ServletException
     */
    public void doFilter(
        ServletRequest request, ServletResponse response, FilterChain chain
    ) throws IOException, ServletException {
        OpenSessionInViewInterceptor interceptor
            = (OpenSessionInViewInterceptor) getApplicationContext().getBean("openSessionInViewInterceptor");
        WebRequest webRequest = new ServletWebRequest((HttpServletRequest) request);
        interceptor.preHandle(webRequest);
        try {
            chain.doFilter(request, response);
            interceptor.postHandle(webRequest, null);
        } finally {
            interceptor.afterCompletion(webRequest, null);
        }
    }
}
