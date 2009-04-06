package edu.northwestern.bioinformatics.studycalendar.osgi.hostservices;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.internal.HostBeansImpl;

/**
 * @author Rhett Sutphin
 */
public class Activator implements BundleActivator {
    public void start(BundleContext bundleContext) throws Exception {
        HostBeansImpl impl = new HostBeansImpl();
        impl.registerServices(bundleContext);
        bundleContext.registerService(HostBeans.class.getName(), impl, null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
    }
}
