package edu.northwestern.bioinformatics.studycalendar.security.csm;

import edu.northwestern.bioinformatics.studycalendar.security.csm.internal.DefaultCsmAuthorizationManagerRegisterer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author Rhett Sutphin
 */
public class Activator implements BundleActivator {
    public void start(BundleContext bundleContext) throws Exception {
        new DefaultCsmAuthorizationManagerRegisterer(bundleContext).start();
    }

    public void stop(BundleContext bundleContext) throws Exception {
    }
}
