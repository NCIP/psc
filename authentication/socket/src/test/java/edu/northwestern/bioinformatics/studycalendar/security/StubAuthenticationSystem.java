/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

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
import org.springframework.core.io.ByteArrayResource;

import javax.servlet.Filter;

/**
 * @author Rhett Sutphin
 */
public class StubAuthenticationSystem implements AuthenticationSystem {
    private static final DefaultConfigurationProperties PROPERTIES
        = new DefaultConfigurationProperties(new ByteArrayResource(new byte[0]));
    public static final ConfigurationProperty<String> EXPECTED_INITIALIZATION_ERROR_MESSAGE
        = PROPERTIES.add(new DefaultConfigurationProperty.Text("expectedError"));

    private static Configuration lastConfiguration, lastValidationConfiguration;

    public ConfigurationProperties configurationProperties() {
        return PROPERTIES;
    }

    public String name() {
        return "stub";
    }

    public String behaviorDescription() {
        throw new UnsupportedOperationException("behaviorDescription not implemented");
    }

    public void validate(Configuration configuration) throws StudyCalendarValidationException {
        lastValidationConfiguration = configuration;
    }

    public void initialize(Configuration configuration) throws AuthenticationSystemInitializationFailure {
        if (configuration.isSet(EXPECTED_INITIALIZATION_ERROR_MESSAGE)) {
            throw new AuthenticationSystemInitializationFailure(
                configuration.get(EXPECTED_INITIALIZATION_ERROR_MESSAGE));
        } else {
            lastConfiguration = configuration;
        }
    }

    public static Configuration getLastInitializationConfiguration() {
        return lastConfiguration;
    }

    public static Configuration getLastValidationConfiguration() {
        return lastValidationConfiguration;
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

    public boolean usesLocalLoginScreen() {
        throw new UnsupportedOperationException("usesLocalLoginScreen not implemented");
    }
}
