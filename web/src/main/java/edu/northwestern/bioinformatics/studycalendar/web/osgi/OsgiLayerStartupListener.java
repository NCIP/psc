/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.osgi;

import edu.northwestern.bioinformatics.studycalendar.tools.osgi.Embedder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Rhett Sutphin
 */
public class OsgiLayerStartupListener implements ServletContextListener {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public static final String BUNDLE_CONTEXT_ATTRIBUTE =
        OsgiLayerStartupListener.class.getName() + ".bundleContext";
    public static final String EMBEDDER_ATTRIBUTE =
        OsgiLayerStartupListener.class.getName() + ".embedder";

    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();

        Embedder created = createEmbedder(servletContext);
        servletContext.setAttribute(EMBEDDER_ATTRIBUTE, created);

        servletContext.setAttribute(BUNDLE_CONTEXT_ATTRIBUTE, created.start());
    }

    protected Embedder createEmbedder(ServletContext servletContext) {
        return new OsgiLayerEmbedderCreator(servletContext).create();
    }

    public void contextDestroyed(ServletContextEvent event) {
        Embedder actual =
            (Embedder) event.getServletContext().getAttribute(EMBEDDER_ATTRIBUTE);
        if (actual != null) {
            log.info("Stopping embedded OSGi layer");
            actual.stop();
        } else {
            log.info("Embedded OSGi layer was not started so it won't be stopped");
        }
    }
}
