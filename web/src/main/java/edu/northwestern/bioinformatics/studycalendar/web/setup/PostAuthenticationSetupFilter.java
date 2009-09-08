package edu.northwestern.bioinformatics.studycalendar.web.setup;

import gov.nih.nci.cabig.ctms.web.filters.ContextRetainingFilterAdapter;
import edu.northwestern.bioinformatics.studycalendar.core.setup.SetupStatus;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.springframework.web.servlet.view.RedirectView;

/**
 * @author Jalpa Patel
 */
public class PostAuthenticationSetupFilter extends ContextRetainingFilterAdapter {
    private SetupStatus status;

    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        status = (SetupStatus) getApplicationContext().getBean("setupStatus");
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (status.isPostAuthenticationSetupNeeded()) {
            log.debug("Initial setup for site is required.  Redirecting.");
            try {
                new RedirectView("/setup/postAuthenticationSetup", true).render(null, (HttpServletRequest) request, (HttpServletResponse) response);
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
            log.debug("Initial setup complete.  Proceeding.");
            chain.doFilter(request, response);
        }
    }
}
