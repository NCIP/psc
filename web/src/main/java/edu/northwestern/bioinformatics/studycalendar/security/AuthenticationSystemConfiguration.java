package edu.northwestern.bioinformatics.studycalendar.security;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystemLoadingFailure;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationEvent;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationListener;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationMap;
import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationProperty;
import org.acegisecurity.userdetails.UserDetailsService;
import org.apache.commons.lang.StringUtils;
import org.dynamicjava.api_bridge.ApiBridge;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class AuthenticationSystemConfiguration implements Configuration, ConfigurationListener {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Configuration delegate;
    private DataSource dataSource;
    private UserDetailsService userDetailsService;
    private BundleContext bundleContext;
    private ConfigurationProperties currentProperties;
    private AuthenticationSystem currentSystem, newSystem;
    private ServiceReference currentSystemReference, newSystemReference;
    private boolean propertiesReady;
    private boolean systemReady;

    public static final DefaultConfigurationProperties UNIVERSAL_PROPERTIES
        = new DefaultConfigurationProperties(new ClassPathResource(
            "authentication-system-universal.properties", AuthenticationSystemConfiguration.class));
    public static final ConfigurationProperty<String> AUTHENTICATION_SYSTEM
        = UNIVERSAL_PROPERTIES.add(new DefaultConfigurationProperty.Text("authenticationSystem"));
    private static final String SERVICE_NAME = AuthenticationSystem.class.getName();

    public synchronized ConfigurationProperties getProperties() {
        initProperties();
        return currentProperties;
    }

    public synchronized AuthenticationSystem getAuthenticationSystem() {
        initSystem();
        return currentSystem;
    }

    @Deprecated
    public boolean isCustomAuthenticationSystem() {
        return false;
    }

    private synchronized void signalRebuildNeeded() {
        propertiesReady = false;
        systemReady = false;
    }

    // determines system class & instantiates it to update in the properties.
    // Does not initialize the system.
    private synchronized void initProperties() {
        if (propertiesReady) return;
        systemReady = false;

        newSystemReference = retrieveAuthenticationSystemReference();
        newSystem = acquireAuthenticationSystem();
        log.debug("Successfully retrieved plugin instance {} of class {}",
            newSystem, newSystem.getClass().getName());
        log.debug("Retrieved authentication system has these configuration properties: {}",
            newSystem.configurationProperties().getAll());
        currentProperties = DefaultConfigurationProperties.union(
            UNIVERSAL_PROPERTIES, newSystem.configurationProperties());
        propertiesReady = true;
    }

    // Initializes the system using the parameters determined by initProperties
    private synchronized void initSystem() {
        if (systemReady) return;
        initProperties();
        newSystem.initialize(this, userDetailsService, dataSource);
        log.debug("Successfully initialized new authentication system {}.  Replacing.", newSystem);
        // no errors, so:
        if (currentSystemReference != null) getBundleContext().ungetService(currentSystemReference);
        currentSystemReference = newSystemReference;
        currentSystem = newSystem;
        systemReady = true;
    }

    private ServiceReference retrieveAuthenticationSystemReference() {
        String desired = get(AUTHENTICATION_SYSTEM);
        ServiceReference ref = null;
        if (!StringUtils.isBlank(desired)) {
            ref = findServiceFromBundle(desired);
            if (ref == null) {
                log.error("The configured authentication system \"{}\" is no longer available.  " +
                    "Will use the default system instead.  This may be the cause of any " +
                    "subsequent AuthenticationLoadingFailure.", desired);
            }
        } else {
            log.debug("No specific authentication selected.  Will use the OSGi-selected implementation.");
        }
        if (ref == null) {
            // Get the default, either because nothing is explicitly configured or because
            // the explicitly configured plugin is not available.
            ref = getBundleContext().getServiceReference(SERVICE_NAME);
        }
        if (ref == null) {
            // Still null?  No auth systems available.
            throw new AuthenticationSystemLoadingFailure(
                "No authentication system plugins available from the OSGi layer.  Plugins must be both installed and activated to be used.");
        }
        return ref;
    }

    // TODO: might be able to do this with an OSGi Filter instead
    private ServiceReference findServiceFromBundle(String desiredBundle) {
        log.debug("Searching for selected authentication system bundle \"{}\"", desiredBundle);
        ServiceReference[] refs;
        try {
            refs = getBundleContext().getServiceReferences(SERVICE_NAME, null);
        } catch (InvalidSyntaxException e) {
            throw new StudyCalendarSystemException("Unexpected exception when retrieving list of authentication systems", e);
        }
        for (ServiceReference serviceReference : refs) {
            if (serviceReference.getBundle().getSymbolicName().equals(desiredBundle)) {
                log.debug("Found desired service \"{}\" in bundle {}",
                    desiredBundle, serviceReference.getBundle());
                return serviceReference;
            }
        }
        return null;
    }

    private AuthenticationSystem acquireAuthenticationSystem() {
        log.info("Thread classloader: {} parent: {}", Thread.currentThread().getContextClassLoader(),
            Thread.currentThread().getContextClassLoader().getParent());
        log.info("Bundle classloader: {} parent: {}", getBundleContext().getClass().getClassLoader(),
            getBundleContext().getClass().getClassLoader().getParent());
        ApiBridge bridge = ApiBridge.getApiBridge(
            Thread.currentThread().getContextClassLoader(),
            "edu.northwestern.bioinformatics.studycalendar",
            "edu.northwestern.bioinformatics.studycalendar.security.plugin",
            "org.acegisecurity",
            "org.acegisecurity.userdetails",
            "gov.nih.nci.cabig.ctms.tools.configuration",
            "javax.servlet",
            "javax.servlet.http"
        );
        Object rawSystem = getBundleContext().getService(newSystemReference);
        log.info("System instance classloader: {} parent: {}", rawSystem.getClass().getClassLoader(),
            rawSystem.getClass().getClassLoader().getParent());
        return (AuthenticationSystem) bridge.bridge(rawSystem);
    }

    public void configurationUpdated(ConfigurationEvent update) {
        signalRebuildNeeded();
    }

    ////// Delegating implementation of Configuration

    public <V> V get(ConfigurationProperty<V> property) {
        return delegate.get(property);
    }

    public <V> void set(ConfigurationProperty<V> property, V value) {
        delegate.set(property, value);
    }

    public boolean isSet(ConfigurationProperty<?> property) {
        return delegate.isSet(property);
    }

    public <V> void reset(ConfigurationProperty<V> property) {
        delegate.reset(property);
    }

    public Map<String, Object> getMap() {
        return new DefaultConfigurationMap(this);
    }

    public void addConfigurationListener(ConfigurationListener listener) {
        delegate.addConfigurationListener(listener);
    }

    ////// CONFIGURATION

    public void setDelegate(Configuration delegate) {
        this.delegate = delegate;
        delegate.addConfigurationListener(this);
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    private BundleContext getBundleContext() {
        if (bundleContext == null) {
            throw new StudyCalendarSystemException(
                "No bundle context available.  Authentication system cannot be configured.");
        }
        return bundleContext;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
}
