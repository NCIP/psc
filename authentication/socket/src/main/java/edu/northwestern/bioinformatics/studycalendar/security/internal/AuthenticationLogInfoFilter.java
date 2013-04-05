/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.internal;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.slf4j.MDC;

import javax.servlet.*;
import java.io.IOException;

public class AuthenticationLogInfoFilter implements Filter {
    public static final String USER_NAME_KEY = "userName";

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
        throws IOException, ServletException {

        Authentication current = SecurityContextHolder.getContext().getAuthentication();
        if (current != null) {
            MDC.put(USER_NAME_KEY, current.getName());
        }

        chain.doFilter(req, resp);

        MDC.remove(USER_NAME_KEY);
    }

    public void init(FilterConfig filterConfig) throws ServletException {}
    public void destroy() {}
}
