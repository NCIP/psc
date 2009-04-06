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
        ServiceFactory factory = createAuthenticationSystemFactory(context);
        systemFactoryRegistration = context.registerService(
            AuthenticationSystem.class.getName(),
            factory,
            serviceProperties(factory, context.getBundle())
        );
    }

    protected abstract Class<? extends AuthenticationSystem> authenticationSystemClass();

    protected ServiceFactory createAuthenticationSystemFactory(final BundleContext context) {
        return new ServiceFactory() {
            public Object getService(Bundle bundle, ServiceRegistration serviceRegistration) {
                try {
                    AuthenticationSystem authenticationSystem = authenticationSystemClass().newInstance();
                    if (authenticationSystem instanceof AbstractAuthenticationSystem) {
                        ((AbstractAuthenticationSystem) authenticationSystem).setBundleContext(context);
                    }
                    return authenticationSystem;
                } catch (InstantiationException e) {
                    throw new IllegalStateException(e);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }

            public void ungetService(Bundle bundle, ServiceRegistration serviceRegistration, Object o) {
                // Nothing to clean up
            }
        };
    }

    protected Dictionary<String, Object> serviceProperties(ServiceFactory factory, Bundle bundle) {
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
