/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools.osgi;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Service loader adapted from
 * http://felix.apache.org/site/apache-felix-framework-launching-and-embedding.html .
 *
 * @author Rhett Sutphin
 */
public class FrameworkFactoryFinder {
    private static final String SERVICE_NAME =
        "META-INF/services/org.osgi.framework.launch.FrameworkFactory";

    private static final Logger log = LoggerFactory.getLogger(FrameworkFactoryFinder.class);

    public static FrameworkFactory getFrameworkFactory() {
        URL url = FrameworkFactoryFinder.class.getClassLoader().getResource(SERVICE_NAME);

        if (url == null) {
            throw new StudyCalendarSystemException(
                "Could not find service location %s", SERVICE_NAME);
        }

        String className = extractClassName(url);

        try {
            return (FrameworkFactory) Class.forName(className).newInstance();
        } catch (InstantiationException e) {
            throw new StudyCalendarSystemException("Failed to instantiate %s", className, e);
        } catch (IllegalAccessException e) {
            throw new StudyCalendarSystemException("Failed to instantiate %s", className, e);
        } catch (ClassNotFoundException e) {
            throw new StudyCalendarSystemException(
                "Could not find the class (%s) specified by %s", className, SERVICE_NAME, e);
        }
    }

    private static String extractClassName(URL url) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(url.openStream()));
            for (String s = br.readLine(); s != null; s = br.readLine()) {
                s = s.trim();
                // Try to load first non-empty, non-commented line.
                if ((s.length() > 0) && (s.charAt(0) != '#')) return s;
            }
        } catch (IOException e) {
            throw new StudyCalendarSystemException(
                "Could not read service specification from %s", url, e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.warn("IOException while closing reader for {}", url);
                }
            }
        }

        throw new StudyCalendarSystemException("No service class specified in %s", SERVICE_NAME);
    }
}
