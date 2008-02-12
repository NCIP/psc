package edu.northwestern.bioinformatics.studycalendar.security;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.KnownAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystemLoadingFailure;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationEvent;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationListener;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ClassPathResource;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class AuthenticationSystemConfiguration implements Configuration, ConfigurationListener, ApplicationContextAware {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Configuration delegate;
    private ApplicationContext applicationContext;
    private ConfigurationProperties currentProperties;
    private AuthenticationSystem currentSystem, newSystem;
    private boolean propertiesReady;
    private boolean systemReady;

    public static final ConfigurationProperties UNIVERSAL_PROPERTIES
        = new ConfigurationProperties(new ClassPathResource(
            "authentication-system-universal.properties", AuthenticationSystemConfiguration.class));
    public static final ConfigurationProperty<String> AUTHENTICATION_SYSTEM
        = UNIVERSAL_PROPERTIES.add(new ConfigurationProperty.Text("authenticationSystem"));

    public synchronized ConfigurationProperties getProperties() {
        initProperties();
        return currentProperties;
    }

    public synchronized AuthenticationSystem getAuthenticationSystem() {
        initSystem();
        return currentSystem;
    }

    public boolean isCustomAuthenticationSystem() {
        String systemName = get(AUTHENTICATION_SYSTEM);
        return (KnownAuthenticationSystem.safeValueOf(systemName) == null);
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

        Class<? extends AuthenticationSystem> authSysClass = determineSystemClass();
        try {
            newSystem = authSysClass.newInstance();
            log.debug("Successfully instantiated {} as {}", authSysClass, newSystem);
            log.debug("Newly instantiated authentication system has these configuration properties: {}", newSystem.configurationProperties().getAll());
            currentProperties = ConfigurationProperties.union(UNIVERSAL_PROPERTIES, newSystem.configurationProperties());
            propertiesReady = true;
        } catch (InstantiationException e) {
            throw new StudyCalendarSystemException(
                "Could not create an instance of authentication system %s", e, authSysClass.getName());
        } catch (IllegalAccessException e) {
            throw new StudyCalendarSystemException(
                "Could not create an instance of authentication system %s", e, authSysClass.getName());
        }
    }

    // Initializes the system using the parameters determined by initProperties
    private synchronized void initSystem() {
        if (systemReady) return;
        initProperties();
        newSystem.initialize(applicationContext, this);
        log.debug("Successfully initialized new authentication system {}.  Replacing.", newSystem);
        // no errors, so:
        currentSystem = newSystem;
        systemReady = true;
    }

    private Class<? extends AuthenticationSystem> determineSystemClass() {
        String request = get(AUTHENTICATION_SYSTEM);
        log.debug("Determining authentication system class corresponding to \"{}\"", request);
        KnownAuthenticationSystem known = KnownAuthenticationSystem.safeValueOf(request);
        if (known != null) {
            Class<? extends AuthenticationSystem> klass = known.getAuthenticationSystemClass();
            log.debug("\"{}\" corresponds to known authentication system {}", request, klass);
            return klass;
        }
        if (StringUtils.isBlank(request)) {
            throw new AuthenticationSystemLoadingFailure("AuthenticationSystem implementation not specified");
        }
        try {
            log.debug("Attempting to turn \"{}\" into a class", request);
            Class<?> klass = Class.forName(request);
            if (AuthenticationSystem.class.isAssignableFrom(klass)) {
                return (Class<? extends AuthenticationSystem>) klass;
            } else {
                throw new AuthenticationSystemLoadingFailure("%s does not implement %s",
                    klass, AuthenticationSystem.class.getName());
            }
        } catch (ClassNotFoundException e) {
            throw new AuthenticationSystemLoadingFailure("Could not load class %s", request, e);
        }
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

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setDelegate(Configuration delegate) {
        this.delegate = delegate;
        delegate.addConfigurationListener(this);
    }
}
