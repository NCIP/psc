package edu.northwestern.bioinformatics.studycalendar.security.plugin.local;

import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import org.acegisecurity.AuthenticationManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.UsernameAndPasswordAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystemTools;

/**
 * @author Rhett Sutphin
 */
public class LocalAuthenticationSystem extends UsernameAndPasswordAuthenticationSystem {
    private static final ConfigurationProperties PROPERTIES = ConfigurationProperties.empty();
    private ApplicationContext localSystemContext;

    public ConfigurationProperties configurationProperties() {
        return PROPERTIES;
    }

    @Override
    protected void initBeforeCreate() {
        localSystemContext = new ClassPathXmlApplicationContext(
            new String[] { "local-authentication-beans.xml" },
            getClass(), getApplicationContext());
    }

    @Override
    protected AuthenticationManager createAuthenticationManager() {
        PSCAuthenticationProvider provider
            = (PSCAuthenticationProvider) localSystemContext.getBean("pscAuthenticationProvider");
        return AuthenticationSystemTools.createProviderManager(localSystemContext, provider);
    }

    @Override
    public boolean usesLocalPasswords() {
        return true;
    }
}
