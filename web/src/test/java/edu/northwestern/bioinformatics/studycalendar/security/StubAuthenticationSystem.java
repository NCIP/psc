package edu.northwestern.bioinformatics.studycalendar.security;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.ui.AuthenticationEntryPoint;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;

import javax.servlet.Filter;

/**
 * @author Rhett Sutphin
 */
public class StubAuthenticationSystem implements AuthenticationSystem {
    private static final ConfigurationProperties PROPERTIES
        = new ConfigurationProperties(new ClassPathResource(
            "stub-details.properties", StubAuthenticationSystem.class));
    public static final ConfigurationProperty<String> EXPECTED_INITIALIZATION_ERROR_MESSAGE
        = new ConfigurationProperty.Text("expectedError");

    private ApplicationContext initialApplicationContext;
    private Configuration initialConfiguration;

    public ConfigurationProperties configurationProperties() {
        return PROPERTIES;
    }

    public String name() {
        throw new UnsupportedOperationException("name not implemented");
    }

    public String behaviorDescription() {
        throw new UnsupportedOperationException("behaviorDescription not implemented");
    }

    public void initialize(ApplicationContext parent, Configuration configuration) {
        if (configuration.isSet(EXPECTED_INITIALIZATION_ERROR_MESSAGE)) {
            throw new StudyCalendarValidationException(configuration.get(EXPECTED_INITIALIZATION_ERROR_MESSAGE));
        } else {
            initialApplicationContext = parent;
            initialConfiguration = configuration;
        }
    }

    public ApplicationContext getInitialApplicationContext() {
        return initialApplicationContext;
    }

    public Configuration getInitialConfiguration() {
        return initialConfiguration;
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
