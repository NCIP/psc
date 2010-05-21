package edu.northwestern.bioinformatics.studycalendar.osgi.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

/**
 * Manually configures logback, assuming it is the current SLF4J implementation.
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
          configurator.doConfigure(getClass().getResource("/logback.xml"));
        } catch (JoranException je) {
          // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
    }

    public void stop(BundleContext bundleContext) throws Exception {
    }
}
