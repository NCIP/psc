package edu.northwestern.bioinformatics.studycalendar.configuration;

import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationEntry;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.DatabaseBackedConfiguration;
import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationProperties;

/**
 * @author Rhett Sutphin
 */
public class StoredAuthenticationSystemConfiguration extends DatabaseBackedConfiguration {
    public ConfigurationProperties getProperties() {
        return DefaultConfigurationProperties.empty();
    }

    @Override
    protected Class<? extends ConfigurationEntry> getConfigurationEntryClass() {
        return AuthenticationSystemConfigurationEntry.class;
    }
}
