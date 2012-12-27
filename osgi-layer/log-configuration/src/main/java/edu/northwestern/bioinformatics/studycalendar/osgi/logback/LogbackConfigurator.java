/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.osgi.logback;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * Manually configures logback for the OSGi layer, assuming it is the current SLF4J implementation.
 *
 * @author Rhett Sutphin
 */
public class LogbackConfigurator implements BundleActivator {
    public void start(BundleContext bundleContext) throws Exception {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(lc);
            lc.reset();
            // Hack to get around conditionals problem; see logback.xml
            lc.putProperty("debug-include", getClass().getResource(
                String.format("/debug-%s.xml", debugEnabledString())).toExternalForm());
            configurator.doConfigure(getClass().getResource("/logback.xml"));
        } catch (JoranException je) {
          // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
    }

    private String debugEnabledString() {
        String set = System.getProperty("psc.logging.debug");
        return set == null ? "false" : set;
    }

    public void stop(BundleContext bundleContext) throws Exception {
    }
}
