/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.osgi;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.tools.osgi.Embedder;
import edu.northwestern.bioinformatics.studycalendar.tools.osgi.EmbedderFilesystemConfiguration;
import edu.northwestern.bioinformatics.studycalendar.tools.osgi.FrameworkFactoryFinder;

import javax.servlet.ServletContext;
import java.io.File;

/**
 * @author Rhett Sutphin
 */
public class OsgiLayerEmbedderCreator {
    private static final String OSGI_LAYER_PATH = "/WEB-INF/osgi-layer";
    private final ServletContext servletContext;

    public OsgiLayerEmbedderCreator(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public Embedder create() {
        Embedder embedder = new Embedder();
        embedder.setFrameworkFactory(FrameworkFactoryFinder.getFrameworkFactory());

        String locUrl = servletContext.getRealPath(OSGI_LAYER_PATH);
        if (locUrl == null) {
            throw new StudyCalendarSystemException(
                "Could not locate the osgi-layer content directory %s", OSGI_LAYER_PATH);
        }
        embedder.setConfiguration(new EmbedderFilesystemConfiguration(new File(locUrl)));

        return embedder;
    }
}
