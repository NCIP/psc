package edu.northwestern.bioinformatics.studycalendar.security;

import edu.northwestern.bioinformatics.studycalendar.security.plugin.AbstractAuthenticationSystem;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import org.acegisecurity.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
public class TestableAuthenticationSystem extends AbstractAuthenticationSystem {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final ConfigurationProperties PROPERTIES
        = new ConfigurationProperties(new ClassPathResource(
            "testable-details.properties", TestableAuthenticationSystem.class));
    public static final ConfigurationProperty<String> SERVICE_URL
        = PROPERTIES.add(new ConfigurationProperty.Text("serviceUrl"));
    public static final ConfigurationProperty<String> APPLICATION_URL
        = PROPERTIES.add(new ConfigurationProperty.Text(PSC_URL_CONFIGURATION_PROPERTY_NAME));

    public ConfigurationProperties configurationProperties() {
        return PROPERTIES;
    }

    public String behaviorDescription() {
        throw new UnsupportedOperationException("behaviorDescription not implemented");
        // return null;
    }

    @Override
    protected Collection<ConfigurationProperty<?>> requiredConfigurationProperties() {
        return Arrays.asList((ConfigurationProperty<?>) SERVICE_URL, APPLICATION_URL);
    }

    public Authentication createUsernamePasswordAuthenticationRequest(String username, String password) {
        throw new UnsupportedOperationException("createUsernamePasswordAuthenticationRequest not implemented");
    }

    public Authentication createTokenAuthenticationRequest(String token) {
        throw new UnsupportedOperationException("createTokenAuthenticationRequest not implemented");
    }
}