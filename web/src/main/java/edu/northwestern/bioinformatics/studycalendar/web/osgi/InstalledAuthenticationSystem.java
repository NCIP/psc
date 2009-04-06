package edu.northwestern.bioinformatics.studycalendar.web.osgi;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.security.CompleteAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.Membrane;
import gov.nih.nci.cabig.ctms.web.filters.FilterAdapter;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.annotation.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Bridge from OSGi-layer {@link CompleteAuthenticationSystem} to a filter that
 * is available in the host webapp.
 *
 * @author Rhett Sutphin
 */
public class InstalledAuthenticationSystem extends FilterAdapter {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String SERVICE_NAME = CompleteAuthenticationSystem.class.getName();

    private BundleContext bundleContext;
    private Membrane membrane;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        final CompleteAuthenticationSystem system = getCompleteAuthenticationSystem();

        system.doFilter(servletRequest, servletResponse, new FilterChain() {
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
                final SecurityContext original = SecurityContextHolder.getContext();
                SecurityContextHolder.setContext(system.getCurrentSecurityContext());
                log.debug("SecurityContext bridged from OSGi layer.  Now: {}", SecurityContextHolder.getContext());

                filterChain.doFilter(servletRequest, servletResponse);

                log.debug("Filter processing complete.  Resetting SecurityContext to {}", original);
                SecurityContextHolder.setContext(original);
            }
        });
    }

    public CompleteAuthenticationSystem getCompleteAuthenticationSystem() {
        ServiceReference ref = bundleContext.getServiceReference(SERVICE_NAME);
        if (ref == null) {
            throw new StudyCalendarSystemException(
                "There is no %s available in the OSGi layer.  PSC cannot run.", SERVICE_NAME);
        }
        return (CompleteAuthenticationSystem) membrane.farToNear(bundleContext.getService(ref));
    }

    public AuthenticationSystem getAuthenticationSystem() {
        return getCompleteAuthenticationSystem().getCurrentAuthenticationSystem();
    }

    ////// CONFIGURATION

    @Required
    public void setMembrane(Membrane membrane) {
        this.membrane = membrane;
    }

    @Required
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
