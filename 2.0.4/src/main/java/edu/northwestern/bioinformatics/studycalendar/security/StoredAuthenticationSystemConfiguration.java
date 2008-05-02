package edu.northwestern.bioinformatics.studycalendar.security;

import gov.nih.nci.cabig.ctms.tools.configuration.DatabaseBackedConfiguration;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationEntry;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;

/**
 * @author Rhett Sutphin
 */
public class StoredAuthenticationSystemConfiguration extends DatabaseBackedConfiguration {
    public ConfigurationProperties getProperties() {
        return AuthenticationSystemConfiguration.UNIVERSAL_PROPERTIES;
    }

    @Override
    protected Class<? extends ConfigurationEntry> getConfigurationEntryClass() {
        return AuthenticationSystemConfigurationEntry.class;
    }
}
