package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import gov.nih.nci.cabig.ctms.tools.configuration.DatabaseBackedConfiguration;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import org.springframework.context.ApplicationContext;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.ui.AuthenticationEntryPoint;
import org.apache.commons.lang.StringUtils;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;

import javax.servlet.Filter;
import java.util.Collection;

/**
 * Template base class for implementors of {@link AuthenticationSystem}.
 *
 * @author Rhett Sutphin
 */
public abstract class AbstractAuthenticationSystem implements AuthenticationSystem {
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

    public final void initialize(
        ApplicationContext parent, Configuration configuration
    ) throws StudyCalendarSystemException {
        this.applicationContext = parent;
        this.configuration = configuration;
        validateRequiredConfigurationProperties();
        initBeforeCreate();
        this.authenticationManager = createAuthenticationManager();
        this.entryPoint = createEntryPoint();
        this.filter = createFilter();
        this.logoutFilter = createLogoutFilter();
        initAfterCreate();
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
