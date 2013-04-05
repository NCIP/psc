/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools.osgi;

/**
 * @see EmbedderConfiguration
 * @author Rhett Sutphin
 */
public interface InstallableBundle {
    /**
     * The initial start level for the bundle, as defined by the OSGi Start Level service.
     */
    int getStartLevel();

    /**
     * The location for the bundle, suitable for passing to
     * {@link org.osgi.framework.BundleContext#installBundle(String)}.
     */
    String getLocation();

    /**
     * Whether the bundle should be started after being installed.
     */
    boolean getShouldStart();
}
