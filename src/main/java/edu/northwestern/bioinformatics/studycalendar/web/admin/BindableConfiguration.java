package edu.northwestern.bioinformatics.studycalendar.web.admin;

import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;

import java.util.TreeMap;

/**
 * @author Rhett Sutphin
 */
public class BindableConfiguration extends TreeMap<String, BindableConfigurationEntry<?>> {
    private boolean ignoreMissingProperties;
    private Configuration targetConfiguration;
    private static final ConfigurationProperty<String> IGNORED_PROPERTY 
        = new ConfigurationProperty.Text("__ignored__");

    @SuppressWarnings({ "unchecked" })
    public BindableConfiguration(Configuration targetConfig, boolean ignoreMissingProperties) {
        this.ignoreMissingProperties = ignoreMissingProperties;
        this.targetConfiguration = targetConfig;
        for (ConfigurationProperty<?> property : targetConfig.getProperties().getAll()) {
            put(property.getKey(), new BindableConfigurationEntry(targetConfig, property));
        }
    }

    public BindableConfiguration(Configuration targetConfig) {
        this(targetConfig, false);
    }

    public BindableConfigurationEntry<?> get(Object key) {
        BindableConfigurationEntry<?> value = super.get(key);
        if (value != null) return value;

        if (ignoreMissingProperties) {
            return new BindableConfigurationEntry<String>(targetConfiguration, IGNORED_PROPERTY);
        } else {
            return null;
        }
    }
}
