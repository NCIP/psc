package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
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
        PSCAuthenticationProvider provider
            = (PSCAuthenticationProvider) getApplicationContext().getBean("pscAuthenticationProvider");
        return AuthenticationSystemTools.createProviderManager(getApplicationContext(), provider);
    }

    @Override
    public boolean usesLocalPasswords() {
        return true;
    }
}
