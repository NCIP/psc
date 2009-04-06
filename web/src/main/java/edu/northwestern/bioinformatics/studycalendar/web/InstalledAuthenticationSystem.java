package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.security.CompleteAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.Membrane;
import gov.nih.nci.cabig.ctms.web.filters.ContextRetainingFilterAdapter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.annotation.Required;

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
public class InstalledAuthenticationSystem extends ContextRetainingFilterAdapter {
    private BundleContext bundleContext;
    private Membrane membrane;
    private static final String SERVICE_NAME = CompleteAuthenticationSystem.class.getName();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        getCompleteAuthenticationSystem().doFilter(servletRequest, servletResponse, filterChain);
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
