/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * A filter which wraps one or more other filters.  Differs from
 * {@link org.acegisecurity.util.FilterChainProxy} in that it receives the list of
 * filters in its constructor or as an injected property, instead of using an
 * elaborate filter definition source.
 * <p>
 * The filters of which this filter are composed will not have their {@link #init}
 * or {@link #destroy} methods invoked.
 *
 * @author Rhett Sutphin
 */
public class MultipleFilterFilter implements Filter, InitializingBean {
    private Logger log = LoggerFactory.getLogger(getClass());

    private Filter[] filters;

    public MultipleFilterFilter() { }

    public MultipleFilterFilter(Filter[] filters) {
        this.filters = filters;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        new VirtualFilterChain(filterChain).doFilter(request, response);
    }

    public void init(FilterConfig filterConfig) throws ServletException { }

    public void destroy() { }

    public void setFilters(List<? extends Filter> filters) {
        this.filters = filters.toArray(new Filter[filters.size()]);
    }

    public void afterPropertiesSet() throws Exception {
        if (filters == null) throw new IllegalStateException("No filters configured");
    }

    /**
     * This class is heavily influenced by the same-named inner class in
     * {@link org.acegisecurity.util.FilterChainProxy} *
     */
    private class VirtualFilterChain implements FilterChain {
        private FilterChain targetFilterChain;
        private int currentPosition = 0;

        public VirtualFilterChain(FilterChain targetFilterChain) {
            this.targetFilterChain = targetFilterChain;
        }

        public void doFilter(ServletRequest request, ServletResponse response)
            throws IOException, ServletException {
            if (currentPosition == filters.length) {
                targetFilterChain.doFilter(request, response);
            } else {
                currentPosition++;

                if (log.isDebugEnabled()) {
                    log.debug(" at position " + currentPosition + " of "
                        + filters.length + " in additional filter chain; firing Filter: '"
                        + filters[currentPosition - 1] + '\'');
                }

                filters[currentPosition - 1].doFilter(request, response, this);
            }
        }
    }
}
