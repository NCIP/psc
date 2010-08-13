package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.ui.AuthenticationEntryPoint;

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
    String DEFAULT_TARGET_PATH = "/";

    String PSC_URL_CONFIGURATION_PROPERTY_NAME = "psc.url";

    /**
     * List the configuration properties this plugin would like to read.
     * These properties will probably be statically defined in implementing
     * classes.
     * 
     * @see gov.nih.nci.cabig.ctms.tools.configuration.Configuration
     * @see gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationProperties
     * @see gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationProperties#empty
     */
    ConfigurationProperties configurationProperties();

    /**
     * The short name for this authentication scheme.
     */
    String name();

    /**
     * A phrase describing the behavior of this scheme.
     * E.g., "delegates authentication decisions to an enterprise-wide CAS server".
     */
    String behaviorDescription();

    /**
     * Validate that the provided configuration is sufficent to initialize this system.  Should
     * not have any side effects.
     * @throws StudyCalendarValidationException if any of the configuration properties
     *      have invalid values
     */
    void validate(Configuration configuration) throws StudyCalendarValidationException;

    /**
     * Initialize any internal objects needed to perform authentication.
     * This method will be called before this object is used for any other purpose, except for
     * {@link #configurationProperties}, {@link #name}, and {@link #behaviorDescription}.
     * <p>
     * A simple authenticator might directly create any required objects in this method;
     * a more complex one might prefer to load an
     * {@link org.springframework.context.ApplicationContext} from the classpath.
     * <p>
     * It is guaranteed that this method will only be called once per instance.
     *
     * @param configuration the object from which the configuration properties
     *      specified with {@link #configurationProperties()} may be read
     * @throws AuthenticationSystemInitializationFailure if initialization cannot complete for
     *      an unexpected reason
     * @see org.springframework.context.support.ClassPathXmlApplicationContext
     */
    void initialize(Configuration configuration) throws AuthenticationSystemInitializationFailure;

    /**
     * Acegi {@link AuthenticationManager} for this system.
     * <p>
     * This method may not return <code>null</code> after {@link #initialize} has been called.
     * <p>
     * On successful authentication, the {@link org.acegisecurity.Authentication#getPrincipal() principal}
     * in the {@link Authentication} token returned by this authentication manager must be a value
     * returned by the {@link org.acegisecurity.userdetails.UserDetailsService} available as
     * an OSGi service in the execution environment.  In general that means you need to use
     * that service instance in your Acegi configuration whereever <tt>UserDetailsService</tt>
     * is called for.
     *
     * @see AuthenticationSystemTools#createProviderManager
     * @see edu.northwestern.bioinformatics.studycalendar.security.plugin.AbstractAuthenticationSystem#getUserDetailsService() 
     */
    AuthenticationManager authenticationManager();

    /**
     * Return a {@link Filter} which implements a piece of the authentication
     * mechanism for this plugin (e.g., SSO).  The returned filter will be
     * applied to all requests which require authentication, plus any request
     * that matches <code>/auth/*</code>.  Its {@link Filter#init} method will 
     * not be called.
     * <p>
     * If you need more than one filter, consider {@link edu.northwestern.bioinformatics.studycalendar.tools.MultipleFilterFilter} or Acegi's
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
     * <p>
     * This method may not return <code>null</code> after {@link #initialize} has been called.
     */
    AuthenticationEntryPoint entryPoint();

    /**
     * Returns a filter which handles <code>/auth/logout</code> and performs whatever
     * actions are required to log out in this system.  At a minimum, this will include
     * the behavior implemented in {@link org.acegisecurity.ui.logout.SecurityContextLogoutHandler}.
     * <p>
     * If this method returns null, PSC will use a sensible default.
     */
    Filter logoutFilter();

    /**
     * Create an unauthenticated Acegi {@link Authentication} token for the given username
     * and password.  This token should be authenticable by the {@link AuthenticationManager}
     * returned by {@link #authenticationManager}.
     * <p>
     * If this implementation doesn't support username and password authentication, this method
     * should return null.
     *
     * @see org.acegisecurity.providers.UsernamePasswordAuthenticationToken
     */
    Authentication createUsernamePasswordAuthenticationRequest(String username, String password);

    /**
     * Create an unauthenticated Acegi {@link Authentication} from the given token which can be
     * serviced by the configured {@link AuthenticationManager}.
     * This {@link Authentication} will be used to verify tokens passed to the RESTful API
     * using the <code>psc_token</code> authentication scheme.
     * <p>
     * If this authentication system does not support token-based authentication, this method
     * should return null.
     */
    Authentication createTokenAuthenticationRequest(String token);

    /**
     * Does this authentication system use the passwords that are stored in PSC?  If not,
     * the system administrators will not be prompted to set them.
     */
    boolean usesLocalPasswords();

    /**
     * Does this authentication system use PSC's internal login screen?  If not, attempts to
     * access it will redirect to the root to allow the auth system's filters to process them.
     * @see UsernameAndPasswordAuthenticationSystem
     */
    boolean usesLocalLoginScreen();

    interface ServiceKeys {
        String NAME = "name";
        String BEHAVIOR_DESCRIPTION = "behaviorDescription";
        String CONFIGURATION_PROPERTIES = "configurationProperties";
    }
}
