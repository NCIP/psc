/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.internal;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class SecurityContextReloadFilter implements Filter {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private PscUserDetailsService pscUserDetailsService;

    public void doFilter(
        ServletRequest request, ServletResponse response, FilterChain chain
    ) throws IOException, ServletException {
        Authentication current = SecurityContextHolder.getContext().getAuthentication();
        if (current != null) {
            if (current.getPrincipal() instanceof PscUser) {
                PscUser user = (PscUser) current.getPrincipal();
                if (user.isStale()) {
                    log.debug("Replacing stale authorization information for {}",
                        user.getUsername());
                    SecurityContextHolder.getContext().setAuthentication(
                        createReplacement(user));
                }
            }
        }
        chain.doFilter(request, response);
    }

    private Authentication createReplacement(PscUser user) {
        PscUser replacement = pscUserDetailsService.loadUserByUsername(user.getUsername());
        user.copyAttributesInto(replacement);
        return new UsernamePasswordAuthenticationToken(
            replacement, null, replacement.getAuthorities());
    }

    public void init(FilterConfig config) throws ServletException { }
    public void destroy() { }

    @Required
    public void setPscUserDetailsService(PscUserDetailsService pscUserDetailsService) {
        this.pscUserDetailsService = pscUserDetailsService;
    }
}
