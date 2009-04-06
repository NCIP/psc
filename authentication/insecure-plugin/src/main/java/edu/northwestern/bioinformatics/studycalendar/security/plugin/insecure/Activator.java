package edu.northwestern.bioinformatics.studycalendar.security.plugin.insecure;

import edu.northwestern.bioinformatics.studycalendar.security.plugin.PluginActivator;
import org.osgi.framework.ServiceFactory;

/**
 * @author Rhett Sutphin
 */
public class Activator extends PluginActivator {
    @Override
    protected ServiceFactory createAuthenticationSystemFactory() {
        return new InsecureAuthenticationSystem.Factory();
    }
}
