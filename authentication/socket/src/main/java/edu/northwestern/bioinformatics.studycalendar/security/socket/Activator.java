package edu.northwestern.bioinformatics.studycalendar.security.socket;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author Rhett Sutphin
 */
public class Activator implements BundleActivator {
    private AuthenticationSystemTracker tracker;

    public void start(BundleContext bundleContext) throws Exception {
        tracker = new AuthenticationSystemTracker(bundleContext);
        tracker.open();
    }

    public void stop(BundleContext bundleContext) throws Exception {
        tracker.close();
    }
}