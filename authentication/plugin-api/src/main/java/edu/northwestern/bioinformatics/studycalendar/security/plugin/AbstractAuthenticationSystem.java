package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarUserException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.ui.AuthenticationEntryPoint;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    protected ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    protected Configuration getConfiguration() {
        return configuration;
    }

    public String name() {
        return getClass().getSimpleName().replaceAll("AuthenticationSystem", "");
    }

    public final void initialize(
        ApplicationContext parent, Configuration config
    ) throws AuthenticationSystemInitializationFailure, StudyCalendarValidationException {
        try {
            this.applicationContext = parent;
            this.configuration = config;
            validateRequiredConfigurationProperties();
            initBeforeCreate();
            this.authenticationManager = createAuthenticationManager();
            this.entryPoint = createEntryPoint();
            this.filter = createFilter();
            this.logoutFilter = createLogoutFilter();
            initAfterCreate();
        } catch (StudyCalendarUserException scue) {
            throw scue; // don't wrap properly typed exceptions
        } catch (RuntimeException re) {
            log.info("Initialization failed with runtime exception", re);
            throw new AuthenticationSystemInitializationFailure(re.getMessage(), re);
        }
        validateRequiredElementsCreated();
    }

    private void validateRequiredElementsCreated() throws AuthenticationSystemInitializationFailure {
        List<String> missing = new ArrayList<String>(3);
        if (authenticationManager() == null) {
            missing.add("authenticationManager()");
        }
        if (entryPoint() == null) {
            missing.add("entryPoint()");
        }
        if (logoutFilter() == null) {
            missing.add("logoutFilter()");
        }
        if (!missing.isEmpty()) {
            String list = missing.get(missing.size() - 1);
            if (missing.size() >= 2) {
                list = String.format(
                    "%s or %s", 
                    StringUtils.join(missing.subList(0, missing.size() - 1).iterator(), ", "),
                    list);
            }
            throw new AuthenticationSystemInitializationFailure(
                "%s must not return null from %s", getClass().getSimpleName(), list);
        }
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
     * Template method for initialization.  {@link #getApplicationContext()} and
     * {@link #getConfiguration()} will be available.  All the <code>create*</code>
     * template methods will be called after this one.
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
     * <p>
     * Defaults to the bean <code>defaultLogoutFilter</code> from the application context
     * provided to {@link #initialize}.
     */
    protected Filter createLogoutFilter() {
        return (Filter) getApplicationContext().getBean("defaultLogoutFilter");
    }

    /**
     * Template method for initialization.  {@link #getApplicationContext()} and
     * {@link #getConfiguration()} will be available.  All the <code>create*</code>
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

    private void validateRequiredConfigurationProperties() {
        if (requiredConfigurationProperties() != null) {
            for (ConfigurationProperty<?> prop : requiredConfigurationProperties()) {
                Object value = getConfiguration().get(prop);
                boolean isNull = value == null;
                boolean isBlank = (value instanceof String) && StringUtils.isBlank((String) value);
                if (isNull || isBlank) {
                    throw new StudyCalendarValidationException("%s is required for the selected authentication system",
                        prop.getName());
                }
            }
        }
    }
}
