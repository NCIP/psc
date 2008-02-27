package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import gov.nih.nci.security.acegi.csm.authentication.CSMAuthenticationProvider;
import org.springframework.core.io.ClassPathResource;
import org.acegisecurity.AuthenticationManager;

/**
 * @author Rhett Sutphin
 */
public class LocalAuthenticationSystem extends UsernameAndPasswordAuthenticationSystem {
    private static final ConfigurationProperties PROPERTIES = ConfigurationProperties.empty();

    public ConfigurationProperties configurationProperties() {
        return PROPERTIES;
    }

    @Override
    protected AuthenticationManager createAuthenticationManager() {
        CSMAuthenticationProvider provider
            = (CSMAuthenticationProvider) getApplicationContext().getBean("csmAuthenticationProvider");
        return AuthenticationSystemTools.createProviderManager(getApplicationContext(), provider);
    }
}
