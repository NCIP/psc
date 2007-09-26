package edu.northwestern.bioinformatics.studycalendar.utils.configuration;

import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;

import java.util.Map;
import java.util.HashMap;

/**
 * An in-memory-only version of {@link gov.nih.nci.cabig.ctms.tools.configuration.DatabaseBackedConfiguration}, suitable for testing
 *
 * @author Rhett Sutphin
 */
public class MockConfiguration extends Configuration {
    private Map<String, Object> configuration;

    public MockConfiguration() {
        this.configuration = new HashMap<String, Object>();
    }

    public <V> V get(ConfigurationProperty<V> property) {
        return (V) configuration.get(property.getKey());
    }

    public <V> void set(ConfigurationProperty<V> property, V value) {
        configuration.put(property.getKey(), value);
    }
}
