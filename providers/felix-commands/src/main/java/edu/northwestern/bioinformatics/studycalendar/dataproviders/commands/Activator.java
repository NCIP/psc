/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.commands;

import org.apache.felix.shell.Command;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author Rhett Sutphin
 */
public class Activator implements BundleActivator {
    public void start(BundleContext bundleContext) throws Exception {
        bundleContext.registerService(Command.class.getName(), new SiteCommand(bundleContext), null);
        bundleContext.registerService(Command.class.getName(), new StudyCommand(bundleContext), null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
    }
}
