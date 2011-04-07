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
