/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools.spring;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class ServletWebContextPathAwareFilter implements Filter {
    private ServletWebContextPathPostProcessor targetPostProcessor;

    public void init(FilterConfig config) throws ServletException {
        WebApplicationContext applicationContext =
            WebApplicationContextUtils.getRequiredWebApplicationContext(config.getServletContext());
        targetPostProcessor = (ServletWebContextPathPostProcessor)
            applicationContext.getBean(determinePostProcessorBeanName(config));
    }

    private String determinePostProcessorBeanName(FilterConfig config) {
        String name = config.getInitParameter("processorBeanName");
        return name == null ? "servletWebContextPathPostProcessor" : name;
    }

    public void doFilter(
        ServletRequest request, ServletResponse response, FilterChain chain
    ) throws IOException, ServletException {
        getTargetPostProcessor().registerRequest((HttpServletRequest) request);
        chain.doFilter(request, response);
    }

    public void destroy() { }

    // exposed for testing
    ServletWebContextPathPostProcessor getTargetPostProcessor() {
        return targetPostProcessor;
    }
}
