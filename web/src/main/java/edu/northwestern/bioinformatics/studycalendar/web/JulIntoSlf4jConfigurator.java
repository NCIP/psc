/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Install SLF4J as a handler for java.util.logging.
 * TODO: doesn't seem to work for debug-level (fine & below) messages.
 *
 * @author Rhett Sutphin
 */
public class JulIntoSlf4jConfigurator implements ServletContextListener {
    private Logger log = LoggerFactory.getLogger(getClass());

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        // log.debug("Redirected java.util.logging into SLF4J");
        // SLF4JBridgeHandler.install();
        /*  // For future investigation
            java.util.logging.Logger jul = java.util.logging.Logger.getLogger(getClass().getName());
            jul.finest("java.util.logging: finest");
            jul.finer("java.util.logging: finer");
            jul.fine("java.util.logging: fine");
            jul.info("java.util.logging: info");
            jul.warning("java.util.logging: warning");
            jul.severe("java.util.logging: severe");
        */
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) { }
}
