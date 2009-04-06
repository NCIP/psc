package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarUserException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.ui.AuthenticationEntryPoint;
import org.acegisecurity.userdetails.UserDetailsService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

import javax.servlet.Filter;
import javax.sql.DataSource;
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
    private UserDetailsService userDetailsService;
    private DataSource dataSource;

    /**
     * Provides a base Spring {@link ApplicationContext} which contains
     * singleton beans for the parameters passed to {@link #initialize}.
     * This is intended to be used as a parent application context when a
     * subclass configures its dependent beans outside of the <code>create*</code>
     * and <code>init*</code> methods.
     */
    protected ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    private ApplicationContext createApplicationContext() {
        StaticListableBeanFactory beans = new StaticListableBeanFactory();
        beans.addBean("configuration", getConfiguration());
        beans.addBean("dataSource", getDataSource());
        beans.addBean("pscUserDetailsService", getUserDetailsService());
        GenericApplicationContext newContext = new GenericApplicationContext(new DefaultListableBeanFactory(beans));
        newContext.refresh();
        return newContext;
    }

    /**
     * Utility method for implementors for loading an application context relative to the
     * implementing class.  Handles using the proper ClassLoader so that the beans in the
     * context are visible to Spring as it instantiates them.
     */
    protected ApplicationContext loadClassRelativeXmlApplicationContext(ApplicationContext parent, String... contextResourcePaths) {
        GenericApplicationContext ctx = new GenericApplicationContext(parent);
        XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
        for (String path : contextResourcePaths) {
            xmlReader.loadBeanDefinitions(new ClassPathResource(path, getClass()));
        }
        xmlReader.setBeanClassLoader(getClass().getClassLoader());
        ctx.setClassLoader(getClass().getClassLoader());
        ctx.refresh();
        return ctx;
    }

    protected Configuration getConfiguration() {
        return configuration;
    }

    protected UserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

    protected DataSource getDataSource() {
        return dataSource;
    }

    public String name() {
        return getClass().getSimpleName().replaceAll("AuthenticationSystem", "");
    }

    /**
     * Initializes this authentication system using the values provided by the template methods.
     * <p>
     * When using this base class, you should generally <em>not</em> override this method, but
     * rather the individual template methods.
     *
     * @throws AuthenticationSystemInitializationFailure
     * @throws StudyCalendarValidationException
     */
    public void initialize(
        Configuration config, UserDetailsService uds, DataSource ds
    ) throws AuthenticationSystemInitializationFailure, StudyCalendarValidationException {
        try {
            this.userDetailsService = uds;
            this.dataSource = ds;
            this.configuration = config;
            this.applicationContext = createApplicationContext();
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
