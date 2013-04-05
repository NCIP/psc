/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarUserException;
import edu.northwestern.bioinformatics.studycalendar.security.AuthenticationSystemConfiguration;
import edu.northwestern.bioinformatics.studycalendar.tools.configuration.DictionaryConfiguration;
import edu.northwestern.bioinformatics.studycalendar.web.osgi.InstalledAuthenticationSystem;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;

/**
 * @author Rhett Sutphin
 */
public class AuthenticationSystemSelectorCommand implements Validatable {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private InstalledAuthenticationSystem installedAuthenticationSystem;
    private AuthenticationSystemDirectory directory;
    private DictionaryConfiguration workConfiguration;
    private BindableConfiguration conf;

    public AuthenticationSystemSelectorCommand(
        String newAuthenticationSystem,
        Configuration liveConfiguration,
        AuthenticationSystemDirectory directory,
        InstalledAuthenticationSystem installedAuthenticationSystem
    ) {
        this.directory = directory;
        this.installedAuthenticationSystem = installedAuthenticationSystem;

        ConfigurationProperties liveProperties = combinedConfigurationProperties(
            installedAuthenticationSystem.getAuthenticationSystem().configurationProperties());
        this.workConfiguration = new DictionaryConfiguration(liveConfiguration, liveProperties);
        if (newAuthenticationSystem != null) {
            ConfigurationProperties newProps = directory.get(newAuthenticationSystem).getConfigurationProperties();
            workConfiguration.setConfigurationProperties(combinedConfigurationProperties(newProps));
        }
        conf = new BindableConfiguration(workConfiguration, true);
        log.debug("Working with configuration {}", workConfiguration);
    }

    private ConfigurationProperties combinedConfigurationProperties(ConfigurationProperties systemProps) {
        return DefaultConfigurationProperties.union(
            AuthenticationSystemConfiguration.UNIVERSAL_PROPERTIES,
            systemProps);
    }

    public void validate(Errors errors) {
        try {
            AuthenticationSystemDirectory.Entry entry = directory.get(
                workConfiguration.get(AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM));
            entry.retrieveAndValidateWith(workConfiguration);
        } catch (StudyCalendarUserException scue) {
            scue.rejectInto(errors);
        }
    }

    @SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
    public void apply() {
        installedAuthenticationSystem.updateCompleteAuthenticationSystem(workConfiguration);
    }

    public Configuration getWorkConfiguration() {
        return workConfiguration;
    }

    public AuthenticationSystemDirectory getDirectory() {
        return directory;
    }

    public BindableConfiguration getConf() {
        return conf;
    }
}
