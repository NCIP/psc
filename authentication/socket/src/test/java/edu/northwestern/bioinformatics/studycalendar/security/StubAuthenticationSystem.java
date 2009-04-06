package edu.northwestern.bioinformatics.studycalendar.security;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystemInitializationFailure;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationProperty;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.ui.AuthenticationEntryPoint;
import org.acegisecurity.userdetails.UserDetailsService;

import javax.servlet.Filter;
import javax.sql.DataSource;

/**
 * @author Rhett Sutphin
 */
public class StubAuthenticationSystem implements AuthenticationSystem {
    private static final ConfigurationProperties PROPERTIES
        = DefaultConfigurationProperties.empty();
    public static final ConfigurationProperty<String> EXPECTED_INITIALIZATION_ERROR_MESSAGE
        = new DefaultConfigurationProperty.Text("expectedError");

    private static Configuration lastConfiguration;
    private static DataSource lastDataSource;
    private static UserDetailsService lastUserDetailsService;

    public ConfigurationProperties configurationProperties() {
        return PROPERTIES;
    }

    public String name() {
        return "stub";
    }

    public String behaviorDescription() {
        throw new UnsupportedOperationException("behaviorDescription not implemented");
    }

    public void initialize(
        Configuration configuration, UserDetailsService userDetailsService, DataSource dataSource
    ) throws AuthenticationSystemInitializationFailure, StudyCalendarValidationException {
        if (configuration.isSet(EXPECTED_INITIALIZATION_ERROR_MESSAGE)) {
            throw new StudyCalendarValidationException(configuration.get(EXPECTED_INITIALIZATION_ERROR_MESSAGE));
        } else {
            lastConfiguration = configuration;
            lastUserDetailsService = userDetailsService;
            lastDataSource = dataSource;
        }
    }

    public static Configuration getLastConfiguration() {
        return lastConfiguration;
    }

    public static DataSource getLastDataSource() {
        return lastDataSource;
    }

    public static UserDetailsService getLastUserDetailsService() {
        return lastUserDetailsService;
    }
    
    //////

    public AuthenticationManager authenticationManager() {
        throw new UnsupportedOperationException("authenticationManager not implemented");
    }

    public Filter filter() {
        throw new UnsupportedOperationException("filter not implemented");
    }

    public AuthenticationEntryPoint entryPoint() {
        throw new UnsupportedOperationException("entryPoint not implemented");
    }

    public Filter logoutFilter() {
        throw new UnsupportedOperationException("logoutFilter not implemented");
    }

    public Authentication createUsernamePasswordAuthenticationRequest(String username, String password) {
        throw new UnsupportedOperationException("createUsernamePasswordAuthenticationRequest not implemented");
    }

    public Authentication createTokenAuthenticationRequest(String token) {
        throw new UnsupportedOperationException("createTokenAuthenticationRequest not implemented");
    }

    public boolean usesLocalPasswords() {
        throw new UnsupportedOperationException("usesLocalPasswords not implemented");
    }
}
