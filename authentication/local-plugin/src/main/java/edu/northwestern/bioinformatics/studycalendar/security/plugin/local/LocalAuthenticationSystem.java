/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin.local;

import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystemTools;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.UsernameAndPasswordAuthenticationSystem;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationProperties;
import org.acegisecurity.AuthenticationManager;
import org.springframework.context.ApplicationContext;

/**
 * @author Rhett Sutphin
 */
public class LocalAuthenticationSystem extends UsernameAndPasswordAuthenticationSystem {
    private static final ConfigurationProperties PROPERTIES = DefaultConfigurationProperties.empty();
    private ApplicationContext localSystemContext;

    public ConfigurationProperties configurationProperties() {
        return PROPERTIES;
    }

    public String behaviorDescription() {
        return "uses passwords stored in PSC's own database";
    }

    @Override
    protected void initBeforeCreate() {
        localSystemContext = loadClassRelativeXmlApplicationContext(
            getApplicationContext(), "local-authentication-beans.xml");
    }

    @Override
    protected AuthenticationManager createAuthenticationManager() {
        PscAuthenticationProvider provider
            = (PscAuthenticationProvider) localSystemContext.getBean("pscAuthenticationProvider");
        return AuthenticationSystemTools.createProviderManager(localSystemContext, provider);
    }

    @Override
    public boolean usesLocalPasswords() {
        return true;
    }
}
