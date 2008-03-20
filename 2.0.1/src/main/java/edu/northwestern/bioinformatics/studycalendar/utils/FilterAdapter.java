package edu.northwestern.bioinformatics.studycalendar.utils;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.FilterChain;
import java.io.IOException;

/**
 * An all-empty adapter a la AWT's listener adapters
 *
 * @author Rhett Sutphin
 */
public abstract class FilterAdapter implements Filter {
    public void init(FilterConfig filterConfig) throws ServletException { }
    public void destroy() { }

    public void doFilter(
        ServletRequest request, ServletResponse response, FilterChain chain
    ) throws IOException, ServletException {
    }
}
