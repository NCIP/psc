package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarUserException;
import edu.northwestern.bioinformatics.studycalendar.tools.DictionaryConfiguration;
import edu.northwestern.bioinformatics.studycalendar.security.AuthenticationSystemConfiguration;
import edu.northwestern.bioinformatics.studycalendar.web.osgi.InstalledAuthenticationSystem;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import org.springframework.validation.Errors;

/**
 * @author Rhett Sutphin
 */
public class AuthenticationSystemSelectorCommand implements Validatable {
    private InstalledAuthenticationSystem installedAuthenticationSystem;
    private AuthenticationSystemDirectory directory;
    private DictionaryConfiguration workConfiguration;
    private BindableConfiguration conf;

    public AuthenticationSystemSelectorCommand(
        Configuration liveConfiguration, AuthenticationSystemDirectory directory,
        InstalledAuthenticationSystem installedAuthenticationSystem
    ) {
        this.directory = directory;
        this.installedAuthenticationSystem = installedAuthenticationSystem;

        ConfigurationProperties liveProperties = DefaultConfigurationProperties.union(AuthenticationSystemConfiguration.UNIVERSAL_PROPERTIES,
            installedAuthenticationSystem.getAuthenticationSystem().configurationProperties());
        this.workConfiguration = new DictionaryConfiguration(liveConfiguration, liveProperties);
        conf = new BindableConfiguration(workConfiguration, true);
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

    public AuthenticationSystemDirectory getDirectory() {
        return directory;
    }

    public BindableConfiguration getConf() {
        return conf;
    }
}
