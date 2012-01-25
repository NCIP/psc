package edu.northwestern.bioinformatics.studycalendar.osgi.plugininstaller;

import edu.northwestern.bioinformatics.studycalendar.osgi.plugininstaller.internal.PluginInstallerPreqFinder;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author Rhett Sutphin
 */
public class Activator implements BundleActivator {
    private PluginInstallerPreqFinder preqFinder;

    public void start(BundleContext context) throws Exception {
        preqFinder = new PluginInstallerPreqFinder(context);
        context.addServiceListener(preqFinder);
    }

    public void stop(BundleContext context) throws Exception {
        context.removeServiceListener(preqFinder);
    }
}
