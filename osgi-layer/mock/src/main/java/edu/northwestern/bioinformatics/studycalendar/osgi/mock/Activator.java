/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.osgi.mock;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({"unchecked"})
public class Activator implements BundleActivator {
    public void start(BundleContext bundleContext) throws Exception {
        new EchoService().register(bundleContext, "A");
        new EchoService().register(bundleContext, "B");
        new EchoService().register(bundleContext, "C");
    }

    public void stop(BundleContext bundleContext) throws Exception {
    }
}
