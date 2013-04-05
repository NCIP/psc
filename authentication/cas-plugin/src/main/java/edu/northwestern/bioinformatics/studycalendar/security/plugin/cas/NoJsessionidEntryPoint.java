/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin.cas;

import org.acegisecurity.AuthenticationException;
import org.acegisecurity.ui.cas.CasProcessingFilterEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * This class wraps {@link CasProcessingFilterEntryPoint} to prevent it from asking the container to
 * append JSESSIONID to the service URL.  This behavior is configurable in Spring Security 2 so
 * we should be able to remove this class when we upgrade.
 *
 * @see http://jira.springframework.org/browse/SEC-306
 * @author Rhett Sutphin
 */
public class NoJsessionidEntryPoint extends CasProcessingFilterEntryPoint {
    @Override
    public void commence(
        ServletRequest servletRequest, ServletResponse servletResponse, AuthenticationException e
    ) throws IOException, ServletException {
        super.commence(servletRequest, wrap((HttpServletResponse) servletResponse), e);
    }

    private ServletResponse wrap(HttpServletResponse originalResponse) {
        return (ServletResponse) Proxy.newProxyInstance(
            originalResponse.getClass().getClassLoader(),
            new Class[] { HttpServletResponse.class },
            new NoEncodeHandler(originalResponse));
    }

    private class NoEncodeHandler implements InvocationHandler {
        private HttpServletResponse originalResponse;

        public NoEncodeHandler(HttpServletResponse originalResponse) {
            this.originalResponse = originalResponse;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("encodeURL".equals(method.getName()) && args.length == 1) {
                return args[0];
            } else {
                return method.invoke(originalResponse, args);
            }
        }
    }
}
