package edu.northwestern.bioinformatics.studycalendar.tools.osgi;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class StaticEmbedderConfiguration implements EmbedderConfiguration {
    private Map<String, Object> frameworkProperties = new HashMap<String, Object>();
    private Collection<InstallableBundle> installableBundles = new LinkedList<InstallableBundle>();

    public Map<String, Object> getFrameworkProperties() {
        return frameworkProperties;
    }

    public Collection<InstallableBundle> getBundlesToInstall() {
        return installableBundles;
    }
}
