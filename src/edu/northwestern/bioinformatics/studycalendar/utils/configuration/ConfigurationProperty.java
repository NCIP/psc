package edu.northwestern.bioinformatics.studycalendar.utils.configuration;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collection;
import java.util.Properties;
import java.io.IOException;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;

/**
 * @author Rhett Sutphin
*/
public abstract class ConfigurationProperty<V> {
    private static final Map<String, ConfigurationProperty> INSTANCES = new TreeMap<String, ConfigurationProperty>();
    private static final String DETAILS_RESOURCE = "details.properties";
    private static Properties details;

    private final String key;

    public ConfigurationProperty(String key) {
        this.key = key;
        INSTANCES.put(key, this);
    }

    public static Collection<ConfigurationProperty> values() {
        return INSTANCES.values();
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        loadDetails();
        return details.getProperty(key + ".name");
    }

    public String getDescription() {
        loadDetails();
        return details.getProperty(key + ".description");
    }

    public String getControlType() {
        return "text";
    }

    public abstract String toStorageFormat(V value);
    public abstract V fromStorageFormat(String stored);

    private synchronized static void loadDetails() {
        if (details == null) {
            details = new Properties();
            try {
                details.load(ConfigurationProperty.class.getResourceAsStream(DETAILS_RESOURCE));
            } catch (IOException e) {
                throw new StudyCalendarSystemException("Failed to load property details " + DETAILS_RESOURCE, e);
            }
        }
    }

    ////// IMPLEMENTATIONS

    public static class Text extends ConfigurationProperty<String> {
        public Text(String key) { super(key); }

        public String toStorageFormat(String value) {
            return value;
        }

        public String fromStorageFormat(String stored) {
            return stored;
        }
    }

    public static class Csv extends ConfigurationProperty<List<String>> {
        public Csv(String key) { super(key); }

        public String toStorageFormat(List<String> value) {
            return StringUtils.join(value.iterator(), ", ");
        }

        public List<String> fromStorageFormat(String stored) {
            String[] values = stored.split(",");
            for (int i = 0; i < values.length; i++) {
                values[i] = values[i].trim();
            }
            return Arrays.asList(values);
        }
    }

    public static class Int extends ConfigurationProperty<Integer> {
        public Int(String key) { super(key); }

        public String toStorageFormat(Integer value) {
            return value.toString();
        }

        public Integer fromStorageFormat(String stored) {
            return new Integer(stored);
        }
    }

    public static class Bool extends ConfigurationProperty<Boolean> {
        public Bool(String key) {
            super(key);
        }

        public String getControlType() {
            return "boolean";
        }

        public String toStorageFormat(Boolean value) {
            return value.toString();
        }

        public Boolean fromStorageFormat(String stored) {
            return Boolean.valueOf(stored);
        }
    }
}
