package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
import edu.northwestern.bioinformatics.studycalendar.tools.DeferredBeanInvoker;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import edu.northwestern.bioinformatics.studycalendar.tools.spring.ConcreteStaticApplicationContext;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.ui.AuthenticationEntryPoint;
import org.acegisecurity.userdetails.UserDetailsService;
import org.apache.commons.lang.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;

import javax.servlet.Filter;
import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.util.Collection;

/**
 * Template base class for implementors of {@link AuthenticationSystem}.
 *
 * @author Rhett Sutphin
 */
public abstract class AbstractAuthenticationSystem implements AuthenticationSystem {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private ApplicationContext applicationContext;
    private Configuration configuration;

    private AuthenticationManager authenticationManager;
    private AuthenticationEntryPoint entryPoint;
    private Filter filter, logoutFilter;
    private BundleContext bundleContext;

    /**
     * Provides a base Spring {@link ApplicationContext} which contains
     * singleton beans for a {@link DataSource} and an {@link UserDetailsService}
     * sourced from the OSGi {@link BundleContext}.
     * <p>
     * This is intended to be used as a parent application context when a
     * subclass configures its dependent beans outside of the <code>create*</code>
     * and <code>init*</code> methods.
     */
    protected ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    private ApplicationContext createApplicationContext() {
        return ConcreteStaticApplicationContext.create(
            new MapBuilder<String, Object>().
                put("dataSource", getDataSource()).
                put("pscUserDetailsService", getUserDetailsService()).
                toMap()
        );
    }

    /**
     * Utility method for implementors for loading an application context relative to the
     * implementing class.  Handles using the proper ClassLoader so that the beans in the
     * context are visible to Spring as it instantiates them.
     */
    protected ApplicationContext loadClassRelativeXmlApplicationContext(ApplicationContext parent, String... contextResourcePaths) {
        return new ClassRelativeOsgiXmlApplicationContext(parent, contextResourcePaths, getClass());
    }

    protected Configuration getConfiguration() {
        return configuration;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    protected PscUserDetailsService getUserDetailsService() {
        return getServiceInstance(PscUserDetailsService.class);
    }

    protected DataSource getDataSource() {
        return getServiceInstance(DataSource.class);
    }

    @SuppressWarnings({ "unchecked" })
    private <T> T getServiceInstance(Class<T> klass) {
        if (bundleContext == null) {
            log.warn("No bundle context is available; therefore no {}", klass.getName());
            return null;
        }
        return new DeferredServiceProxyCreator<T>(klass, bundleContext).getProxy();
    }

    public String name() {
        return getClass().getSimpleName().replaceAll("AuthenticationSystem", "");
    }

    /**
     * Validates using the result of the template method {@link #requiredConfigurationProperties()}.
     * Override to provide more elaborate checks if required.
     *
     * @param config The candidate configuration
     * @throws StudyCalendarValidationException
     */
    public void validate(Configuration config) throws StudyCalendarValidationException {
        validateRequiredConfigurationProperties(config);
    }

    /**
     * Initializes this authentication system using the values provided by the template methods.
     * <p>
     * When using this base class, you should generally <em>not</em> override this method, but
     * rather the individual template methods.
     *
     * @throws AuthenticationSystemInitializationFailure
     */
    public void initialize(Configuration config) throws AuthenticationSystemInitializationFailure {
        try {
            this.configuration = config;
            this.applicationContext = createApplicationContext();
            initBeforeCreate();
            this.authenticationManager = createAuthenticationManager();
            this.entryPoint = createEntryPoint();
            this.filter = createFilter();
            this.logoutFilter = createLogoutFilter();
            initAfterCreate();
        } catch (AuthenticationSystemInitializationFailure asif) {
            throw asif; // don't wrap properly typed exceptions
        } catch (RuntimeException re) {
            log.info("Initialization failed with runtime exception", re);
            throw new AuthenticationSystemInitializationFailure(re.getMessage(), re);
        }
        AuthenticationSystemValidator.validateRequiredElementsCreated(this);
    }

    /**
     * Template method to specify some configuration properties as required.
     * If any of the specified properties are null, initialization will stop before
     * any of the other initialization template methods are called.
     */
    protected Collection<ConfigurationProperty<?>> requiredConfigurationProperties() {
        return null;
    }

    /**
     * Template method for initialization. {@link #getConfiguration()},
     * {@link #getUserDetailsService()}, {@link #getDataSource()}
     * and {@link #getApplicationContext()} will be available.
     * All the <code>create*</code> template methods will be called after this one.
     */
    protected void initBeforeCreate() { }

    /**
     * Template method for initializing the value returned by {@link #authenticationManager()}.
     * Alternatively, you could use {@link #initAfterCreate()}.  If you take the latter
     * route, be sure to override {@link #authenticationManager()}, too.
     *
     * @see AuthenticationSystemTools#createProviderManager
     */
    protected AuthenticationManager createAuthenticationManager() {
        return null;
    }

    /**
     * Template method for initializing the value returned by {@link #entryPoint()}.
     * Alternatively, you could use {@link #initAfterCreate()}.  If you take the latter
     * route, be sure to override {@link #entryPoint()}, too.
     */
    protected AuthenticationEntryPoint createEntryPoint() {
        return null;
    }

    /**
     * Template method for initializing the value returned by {@link #filter()}.
     * Alternatively, you could use {@link #initAfterCreate()}.  If you take the latter
     * route, be sure to override {@link #filter()}, too.
     */
    protected Filter createFilter() {
        return null;
    }

    /**
     * Template method for initializing the value returned by {@link #filter()}.
     * Alternatively, you could use {@link #initAfterCreate()}.  If you take the latter
     * route, be sure to override {@link #logoutFilter()}, too.
     */
    protected Filter createLogoutFilter() {
        return null;
    }

    /**
     * Template method for initialization.  All the <code>create*</code>
     * template methods will be called before this one.
     */
    protected void initAfterCreate() { }

    public AuthenticationManager authenticationManager() { return authenticationManager; }
    public AuthenticationEntryPoint entryPoint() { return entryPoint; }
    public Filter filter() { return filter; }
    public Filter logoutFilter() { return logoutFilter; }

    public boolean usesLocalPasswords() {
        return false;
    }

    public boolean usesLocalLoginScreen() {
        return false;
    }

    private void validateRequiredConfigurationProperties(Configuration config) {
        if (requiredConfigurationProperties() != null) {
            for (ConfigurationProperty<?> prop : requiredConfigurationProperties()) {
                Object value = config.get(prop);
                boolean isNull = value == null;
                boolean isBlank = (value instanceof String) && StringUtils.isBlank((String) value);
                if (isNull || isBlank) {
                    throw new StudyCalendarValidationException("%s is required for the selected authentication system",
                        prop.getName());
                }
            }
        }
    }

    private class ClassRelativeOsgiXmlApplicationContext extends OsgiBundleXmlApplicationContext {
        private String[] contextResourcePaths;
        private Class<?> klass;

        public ClassRelativeOsgiXmlApplicationContext(ApplicationContext parent, String[] contextResourcePaths, Class<?> klass) {
            super(parent);
            this.contextResourcePaths = contextResourcePaths;
            this.klass = klass;
            this.setClassLoader(klass.getClassLoader());
            this.setBundleContext(bundleContext);
            this.refresh();
        }

        @Override
        protected void initBeanDefinitionReader(XmlBeanDefinitionReader xmlReader) {
            xmlReader.setBeanClassLoader(klass.getClassLoader());
        }

        @Override
        protected void loadBeanDefinitions(XmlBeanDefinitionReader xmlReader) {
            for (String path : contextResourcePaths) {
                xmlReader.loadBeanDefinitions(new ClassPathResource(path, klass));
            }
        }
    }

    private static class DeferredServiceProxyCreator<T> {
        private final Logger log = LoggerFactory.getLogger(getClass());

        private BundleContext bundleContext;
        private Class<T> serviceInterface;
        private T proxy;
        private DeferredBeanInvoker invoker;

        @SuppressWarnings({ "unchecked" })
        public DeferredServiceProxyCreator(Class<T> serviceInterface, BundleContext bundleContext) {
            this.serviceInterface = serviceInterface;
            this.bundleContext = bundleContext;

            this.invoker = new DeferredBeanInvoker(serviceInterface.getName());
            this.proxy = (T) Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class[] { serviceInterface },
                invoker);

            associateServiceOrRegisterListener();
        }

        private void associateServiceOrRegisterListener() {
            ServiceReference ref = bundleContext.getServiceReference(serviceInterface.getName());
            if (ref == null) {
                log.debug("Deferred service {} not immediately available.", serviceInterface.getName());
                ServiceListener deferredSetter = new ServiceListener() {
                    public void serviceChanged(ServiceEvent event) {
                        if (event.getType() == ServiceEvent.REGISTERED) {
                            log.debug("Deferred service {} is now available.", serviceInterface.getName());
                            setBeanToService(event.getServiceReference());
                        }
                    }
                };
                try {
                    bundleContext.addServiceListener(deferredSetter,
                        String.format("(%s=%s)", Constants.OBJECTCLASS, serviceInterface.getName()));
                } catch (InvalidSyntaxException e) {
                    throw new StudyCalendarError("The syntax is not invalid", e);
                }
            } else {
                log.debug("Deferrable service {} immediately available.", serviceInterface.getName());
                setBeanToService(ref);
            }
        }

        private void setBeanToService(ServiceReference ref) {
            invoker.setBean(bundleContext.getService(ref));
        }

        public T getProxy() {
            return proxy;
        }
    }
}
