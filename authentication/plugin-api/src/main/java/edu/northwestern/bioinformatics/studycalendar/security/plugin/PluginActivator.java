package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import java.util.Dictionary;

/**
 * @author Rhett Sutphin
 */
public abstract class PluginActivator implements BundleActivator {
    private ServiceRegistration systemFactoryRegistration;

    public void start(BundleContext context) throws Exception {
        ServiceFactory factory = createAuthenticationSystemFactory();
        systemFactoryRegistration = context.registerService(
            AuthenticationSystem.class.getName(),
            factory,
            serviceProperties(factory, context.getBundle())
        );
    }

    protected abstract ServiceFactory createAuthenticationSystemFactory();

    private Dictionary<String, Object> serviceProperties(ServiceFactory factory, Bundle bundle) {
        AuthenticationSystem system =
            (AuthenticationSystem) factory.getService(bundle, null);
        Dictionary<String, Object> props = new MapBuilder<String, Object>().
            put(AuthenticationSystem.ServiceKeys.NAME, system.name()).
            put(AuthenticationSystem.ServiceKeys.BEHAVIOR_DESCRIPTION, system.behaviorDescription()).
            put(AuthenticationSystem.ServiceKeys.CONFIGURATION_PROPERTIES, system.configurationProperties()).
            toDictionary();
        factory.ungetService(bundle, null, system);
        return props;
    }

    public void stop(BundleContext bundleContext) throws Exception {
        systemFactoryRegistration.unregister();
    }
}
