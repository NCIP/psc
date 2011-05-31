package gov.nih.nci.cabig.ctms.suite.authorization.plugins.mock;

import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteAuthorizationSource;
import gov.nih.nci.cabig.ctms.suite.authorization.plugins.mock.internal.StaticSuiteAuthorizationSource;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author Rhett Sutphin
 */
public class Activator implements BundleActivator {
    public void start(BundleContext bundleContext) throws Exception {
        bundleContext.registerService(
            SuiteAuthorizationSource.class.getName(), new StaticSuiteAuthorizationSource(), null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
    }
}
