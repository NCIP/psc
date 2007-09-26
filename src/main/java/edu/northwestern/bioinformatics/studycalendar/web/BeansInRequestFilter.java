package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.utils.ContextRetainingFilterAdapter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.FilterConfig;
import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class BeansInRequestFilter extends ContextRetainingFilterAdapter {
    private String[] beanNames;

    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        String beanNamesString = filterConfig.getInitParameter("beanNames");
        if (beanNamesString != null) setBeanNames(beanNamesString.trim().split("\\s*,\\s*"));
    }

    public void setBeanNames(String[] beanNames) {
        this.beanNames = beanNames;
    }

    // Expose for testing
    String[] getBeanNames() {
        return beanNames;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        for (String beanName : beanNames) {
            request.setAttribute(beanName, getApplicationContext().getBean(beanName));
        }
        chain.doFilter(request, response);
    }
}
