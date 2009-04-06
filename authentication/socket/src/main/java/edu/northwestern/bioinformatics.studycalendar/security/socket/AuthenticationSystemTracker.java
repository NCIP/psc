package edu.northwestern.bioinformatics.studycalendar.security.socket;

import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.HashSet;

/**
 * @author Rhett Sutphin
 */
public class AuthenticationSystemTracker extends ServiceTracker {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private Set<ServiceReference> authenticationSystems;

    public AuthenticationSystemTracker(BundleContext bundleContext) {
        super(bundleContext, AuthenticationSystem.class.getName(), null);
        authenticationSystems = new HashSet<ServiceReference>();
    }

    @Override
    public Object addingService(ServiceReference serviceReference) {
        log.info("Authentication plugin registered: {} from {}",
            serviceReference.getProperty(AuthenticationSystem.ServiceKeys.NAME),
            serviceReference.getBundle().getSymbolicName());
        authenticationSystems.add(serviceReference);
        return super.addingService(serviceReference);
    }
}
