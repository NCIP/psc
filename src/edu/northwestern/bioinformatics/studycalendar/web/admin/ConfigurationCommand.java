package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.utils.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.utils.configuration.ConfigurationProperty;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Rhett Sutphin
 */
public class ConfigurationCommand {
    private Configuration configuration;
    private Map<String, BoundProperty<?>> conf;

    public ConfigurationCommand(Configuration configuration) {
        this.configuration = configuration;
        conf = new TreeMap<String, BoundProperty<?>>();
        for (ConfigurationProperty<?> property : configuration.getProperties().getAll()) {
            conf.put(property.getKey(), new BoundProperty(property));
        }
    }

    public Map<String, BoundProperty<?>> getConf() {
        return conf;
    }

    public final class BoundProperty<V> {
        private ConfigurationProperty<V> property;

        public BoundProperty(ConfigurationProperty<V> property) {
            this.property = property;
        }

        public ConfigurationProperty<V> getProperty() {
            return property;
        }

        public V getValue() {
            return configuration.get(property);
        }

        public void setValue(V value) {
            configuration.set(property, value);
        }

        public V getDefault() {
            return property.getDefault();
        }
    }
}
