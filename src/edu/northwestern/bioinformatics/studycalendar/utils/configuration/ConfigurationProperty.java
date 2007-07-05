package edu.northwestern.bioinformatics.studycalendar.utils.configuration;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.io.IOException;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;

/**
 * @author Rhett Sutphin
*/
public abstract class ConfigurationProperty<V> {
    private final String key;
    private ConfigurationProperties collection;

    protected ConfigurationProperty(String key) {
        this.key = key;
    }

    // collaborator access only
    void setCollection(ConfigurationProperties collection) {
        this.collection = collection;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return collection.getNameFor(getKey());
    }

    public String getDescription() {
        return collection.getDescriptionFor(getKey());
    }

    public V getDefault() {
        String stored = collection.getStoredDefaultFor(getKey());
        return stored == null ? null : fromStorageFormat(stored);
    }

    public String getControlType() {
        return "text";
    }

    public abstract String toStorageFormat(V value);
    public abstract V fromStorageFormat(String stored);

    ////// IMPLEMENTATIONS

    public static class Text extends ConfigurationProperty<String> {
        public Text(String key) { super(key); }

        @Override
        public String toStorageFormat(String value) {
            return value;
        }

        @Override
        public String fromStorageFormat(String stored) {
            return stored;
        }
    }

    public static class Csv extends ConfigurationProperty<List<String>> {
        public Csv(String key) { super(key); }

        @Override
        public String toStorageFormat(List<String> value) {
            return StringUtils.join(value.iterator(), ", ");
        }

        @Override
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

        @Override
        public String toStorageFormat(Integer value) {
            return value.toString();
        }

        @Override
        public Integer fromStorageFormat(String stored) {
            return new Integer(stored);
        }
    }

    public static class Bool extends ConfigurationProperty<Boolean> {
        public Bool(String key) {
            super(key);
        }

        @Override
        public String getControlType() {
            return "boolean";
        }

        @Override
        public String toStorageFormat(Boolean value) {
            return value.toString();
        }

        @Override
        public Boolean fromStorageFormat(String stored) {
            return Boolean.valueOf(stored);
        }
    }
}
