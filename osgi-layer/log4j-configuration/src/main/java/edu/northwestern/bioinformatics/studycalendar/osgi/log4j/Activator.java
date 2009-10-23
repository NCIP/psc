package edu.northwestern.bioinformatics.studycalendar.osgi.log4j;

import org.apache.log4j.LogManager;
import org.apache.log4j.xml.DOMConfigurator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.io.InputStream;

/**
 * @author Rhett Sutphin
 */
public class Activator implements BundleActivator {
    public void start(BundleContext bundleContext) throws Exception {
        ClassLoader originalCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        InputStream stream = getClass().getResourceAsStream("/log4j.xml");
        new DOMConfigurator().doConfigure(stream, LogManager.getLoggerRepository());

        Thread.currentThread().setContextClassLoader(originalCL);
    }

    public void stop(BundleContext bundleContext) throws Exception { }
}
