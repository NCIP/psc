/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools.osgi;

import java.util.Collection;
import java.util.Map;

/**
 * Defines the parameters of a strategy
 *
 * @author Rhett Sutphin
 */
public interface EmbedderConfiguration {
    /**
     * Returns a set of properties to pass to
     * {@link org.osgi.framework.launch.FrameworkFactory#newFramework}
     */
    Map<String, Object> getFrameworkProperties();

    /**
     * Returns all the bundles which the embedder should install.
     */
    Collection<InstallableBundle> getBundlesToInstall();

}
