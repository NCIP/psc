/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import gov.nih.nci.cabig.ctms.web.filters.ContextRetainingFilterAdapter;
import org.springframework.orm.hibernate3.support.OpenSessionInViewInterceptor;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
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
    private String interceptorBeanName;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        setInterceptorBeanName(filterConfig.getInitParameter("interceptorBeanName"));
    }

    /**
     * @see OpenSessionInViewInterceptor
     * @see org.springframework.web.servlet.HandlerInterceptor#afterCompletion
     */
    @Override
    public void doFilter(
        ServletRequest request, ServletResponse response, FilterChain chain
    ) throws IOException, ServletException {
        log.debug("Opening session for request");
        OpenSessionInViewInterceptor interceptor
            = (OpenSessionInViewInterceptor) getApplicationContext().getBean(getInterceptorBeanName());
        WebRequest webRequest = new ServletWebRequest((HttpServletRequest) request);
        interceptor.preHandle(webRequest);
        try {
            chain.doFilter(request, response);
            interceptor.postHandle(webRequest, null);
        } finally {
            interceptor.afterCompletion(webRequest, null);
            log.debug("Session closed");
        }
    }


    public String getInterceptorBeanName() {
        return interceptorBeanName;
    }

    public void setInterceptorBeanName(String interceptorBeanName) {
        this.interceptorBeanName = interceptorBeanName;
    }
}
