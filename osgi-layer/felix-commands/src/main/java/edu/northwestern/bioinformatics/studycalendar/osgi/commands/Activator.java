package edu.northwestern.bioinformatics.studycalendar.osgi.commands;

import org.apache.felix.shell.Command;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author Rhett Sutphin
 */
public class Activator implements BundleActivator {
    public void start(BundleContext bundleContext) throws Exception {
        bundleContext.registerService(Command.class.getName(), new SystemPropertyCommand(), null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
    }
}
