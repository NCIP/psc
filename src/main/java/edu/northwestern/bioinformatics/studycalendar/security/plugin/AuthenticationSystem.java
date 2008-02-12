package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import org.acegisecurity.ui.AuthenticationEntryPoint;
import org.acegisecurity.AuthenticationManager;
import org.springframework.context.ApplicationContext;

import javax.servlet.Filter;

/**
 * This interface defines a facade for pluggable authentication modules.
 * It is expected that most implementations will delegate to a library
 * implementation (e.g., one of the many included in Acegi Security).
 * <p>
 * Implementors must have a public default constructor.
 * Most initialization should happen in the {@link #initialize} method.
 * </p>
 *
 * @author Rhett Sutphin
 */
public interface AuthenticationSystem {
    String DEFAULT_TARGET_PATH = "/pages/dashboard";

    String PSC_URL_CONFIGURATION_PROPERTY_NAME = "psc.url";

    /**
     * List the configuration properties this plugin would like to read.
     * These properties will probably be statically defined in implementing
     * classes.
     * 
     * @see gov.nih.nci.cabig.ctms.tools.configuration.DatabaseBackedConfiguration
     * @see {@link ConfigurationProperties#empty} if you don't need any
     */
    ConfigurationProperties configurationProperties();

    /**
     * Initialize any internal objects needed to perform authentication.
     * This method will be called before this object is used for any other purpose.
     * <p>
     * A simple authenticator might directly create any required objects in this method;
     * a more complex one might prefer to load an
     * {@link org.springframework.context.ApplicationContext} from the classpath.
     * <p>
     * It is guaranteed that this method will only be called once per instance.
     *
     * @param parent the system application context
     * @param configuration the object from which the configuration properties
     *      specified with {@link #configurationProperties()} may be read
     * @throws StudyCalendarValidationException if any of the configuration properties
     *      have invalid values
     * @throws AuthenticationSystemInitializationFailure if initialization cannot complete for
     *      any other reason
     * @see org.springframework.context.support.ClassPathXmlApplicationContext
     */
    void initialize(
        ApplicationContext parent, Configuration configuration
    ) throws AuthenticationSystemInitializationFailure, StudyCalendarValidationException;

    /**
     * Acegi {@link AuthenticationManager} for this system.
     *
     * @see AuthenticationSystemTools#createProviderManager
     */
    AuthenticationManager authenticationManager();

    /**
     * Return a {@link Filter} which implements a piece of the authentication
     * mechanism for this plugin (e.g., SSO).  The returned filter will be
     * applied to all requests.  Its {@link Filter#init} method will not be called.
     * <p>
     * If you need more than one filter, consider Acegi's
     * {@link org.acegisecurity.util.FilterChainProxy}.
     * <p>
     * If you don't need a filter, return <code>null</code>.
     * <p>
     * Note that this filter need not implement the entire authentication process
     * from top to bottom -- just the parts that are particular to this system.
     */
    Filter filter();

    /**
     * Acegi entry point for this system.
     * @return
     */
    AuthenticationEntryPoint entryPoint();

    /**
     * Returns a filter which handles <code>/j_acegi_logout</code> and performs whatever
     * actions are required to log out in this system.  At a minimum, this will include
     * the behavior implemented in {@link org.acegisecurity.ui.logout.SecurityContextLogoutHandler}.
     * <p>
     * Many implementations will find the filter defined as <code>defaultLogoutFilter</code>
     * in the application context passed to {@link #initialize} sufficient.
     */
    Filter logoutFilter();

    //////
    // TODO: custom HTTP authentication methods
    //////
}
