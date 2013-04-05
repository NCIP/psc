/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package gov.nih.nci.cabig.ctms.suite.authorization.plugins.mock;

import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteAuthorizationSource;
import gov.nih.nci.cabig.ctms.suite.authorization.plugins.mock.internal.StaticSuiteAuthorizationSource;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

/**
 * @author Rhett Sutphin
 */
public class Activator implements BundleActivator {
    public void start(BundleContext bundleContext) throws Exception {
        bundleContext.registerService(
            SuiteAuthorizationSource.class.getName(), new StaticSuiteAuthorizationSource(),
            new MapBuilder<String, String>().
                put(Constants.SERVICE_PID, createPid(bundleContext.getBundle().getSymbolicName())).
                toDictionary());
    }

    private String createPid(String symbolicName) {
        return symbolicName + ".SOURCE";
    }

    public void stop(BundleContext bundleContext) throws Exception {
    }
}
