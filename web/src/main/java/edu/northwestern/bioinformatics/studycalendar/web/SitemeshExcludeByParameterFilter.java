/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import com.opensymphony.module.sitemesh.RequestConstants;

import javax.servlet.*;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * This filter blocks the execution of sitemesh for requests where a particular parameter is set.
 *
 * @author rsutphin
 */
public class SitemeshExcludeByParameterFilter implements Filter {
    private Pattern pattern;

    public void init(FilterConfig filterConfig) throws ServletException {
        String patternParam = filterConfig.getInitParameter("pattern");
        if (patternParam == null) {
            throw new IllegalArgumentException("pattern init-param required for " + getClass().getName() + " filter");
        }
        setPattern(Pattern.compile(patternParam));
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        for (String paramName : (Iterable<String>) request.getParameterMap().keySet()) {
            if (pattern.matcher(paramName).matches()) {
                // this is based on inspecting sitemesh's PageFilter,
                // and so might break in the future
                request.setAttribute(RequestConstants.FILTER_APPLIED, Boolean.TRUE);
                break;
            }
        }

        chain.doFilter(request, response);
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    // for testing
    Pattern getPattern() {
        return pattern;
    }

    public void destroy() { }
}
