package edu.northwestern.bioinformatics.studycalendar.tools.spring;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Performs the same function as {@link ServletWebContextPathAwareFilter}, except for that it
 * operates on the post processor for a particular dispatcher servlet, rather than the one
 * defined in the global web application context.
 *
 * @author Jalpa Patel
 * @author Rhett Sutphin
 */
public class ServletWebContextPathAwareHandlerInterceptor extends HandlerInterceptorAdapter {
    private ServletWebContextPathPostProcessor servletWebContextPathPostProcessor;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        servletWebContextPathPostProcessor.registerRequest(request);
        return true;
    }

    ////// CONFIGURATION

    @Required
    public void setServletWebContextPathPostProcessor(ServletWebContextPathPostProcessor servletWebContextPathPostProcessor) {
        this.servletWebContextPathPostProcessor = servletWebContextPathPostProcessor;
    }
}
