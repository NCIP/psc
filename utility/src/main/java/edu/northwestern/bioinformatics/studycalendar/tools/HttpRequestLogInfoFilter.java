/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools;

import org.slf4j.MDC;

import javax.servlet.*;
import java.io.IOException;

public class HttpRequestLogInfoFilter implements Filter {
    private static final String USER_IP_ADDRESS_KEY = "userIpAddress";

    public void doFilter(
        final ServletRequest req, final ServletResponse resp, final FilterChain chain
    ) throws IOException, ServletException {

        MDC.put(USER_IP_ADDRESS_KEY, req.getRemoteAddr());

        chain.doFilter(req, resp);

        MDC.remove(USER_IP_ADDRESS_KEY);
    }

    public void init(FilterConfig filterConfig) throws ServletException {}
    public void destroy() {}
}
