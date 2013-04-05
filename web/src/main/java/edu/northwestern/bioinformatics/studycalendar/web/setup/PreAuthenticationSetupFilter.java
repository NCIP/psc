/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.setup;

import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class PreAuthenticationSetupFilter extends AbstractSetupStatusFilter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!setupAlreadyChecked() && getSetupStatus().isPreAuthenticationSetupNeeded()) {
            log.debug("Initial setup for administrator is required.  Redirecting.");
            try {
                new RedirectView("/setup/preAuthenticationSetup", true).render(null, (HttpServletRequest) request, (HttpServletResponse) response);
            } catch (RuntimeException e) {
                throw e;
            } catch (ServletException e) {
                throw e;
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new ServletException("Redirect view rending failed", e);
            }
        } else {
            log.debug("Initial setup of user is complete.  Proceeding.");
            if (!setupAlreadyChecked()) noteSetupAlreadyChecked();
            chain.doFilter(request, response);
        }
    }
}
