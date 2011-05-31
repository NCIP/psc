package gov.nih.nci.cabig.ctms.suite.authorization.socket;

import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteAuthorizationSource;
import gov.nih.nci.cabig.ctms.suite.authorization.socket.internal.PluginSocketCreator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import static java.lang.String.*;

/**
 * @author Rhett Sutphin
 */
public class Activator implements BundleActivator {
    private PluginSocketCreator creator;

    public void start(BundleContext bundleContext) throws Exception {
        creator = new PluginSocketCreator(bundleContext);
        creator.init();
        bundleContext.addServiceListener(
            creator,
            format("(%s=%s)", Constants.OBJECTCLASS, SuiteAuthorizationSource.class.getName()));
    }

    public void stop(BundleContext bundleContext) throws Exception {
        bundleContext.removeServiceListener(creator);
    }
}
