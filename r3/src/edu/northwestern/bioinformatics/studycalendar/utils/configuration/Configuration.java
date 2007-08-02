package edu.northwestern.bioinformatics.studycalendar.utils.configuration;

import org.apache.commons.lang.StringUtils;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.Properties;
import java.util.List;
import java.util.Arrays;
import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class Configuration extends HibernateDaoSupport {
    private static final String DEFAULTS_RESOURCE = "defaultConfiguration.properties";

    private Properties defaults;

    public static final Property<String>
        DEPLOYMENT_NAME = new StringProperty("deploymentName");
    public static final Property<String>
        MAIL_REPLY_TO = new StringProperty("replyTo");
    public static final Property<List<String>>
        MAIL_EXCEPTIONS_TO = new ListProperty("mailExceptionsTo");
    public static final Property<String>
        SMTP_HOST = new StringProperty("smtpHost");
    public static final Property<Integer>
        SMTP_PORT = new IntegerProperty("smtpPort");

    public Configuration() {
        defaults = new Properties();
        try {
            defaults.load(getClass().getResourceAsStream(DEFAULTS_RESOURCE));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load defaults " + DEFAULTS_RESOURCE, e);
        }
    }

    public <V> V get(Property<V> property) {
        String value = getValue(property.getKey());
        return value == null ? null : property.fromStorageFormat(value);
    }

    private String getValue(String key) {
        ConfigurationEntry entry
            = (ConfigurationEntry) getHibernateTemplate().get(ConfigurationEntry.class, key);
        if (entry == null) {
            return defaults.getProperty(key);
        } else {
            return entry.getValue();
        }
    }

    public <V> void set(Property<V> property, V value) {
        ConfigurationEntry entry
            = (ConfigurationEntry) getHibernateTemplate().get(ConfigurationEntry.class, property.getKey());
        if (entry == null) {
            entry = new ConfigurationEntry();
            entry.setKey(property.getKey());
        }
        entry.setValue(property.toStorageFormat(value));
        getHibernateTemplate().saveOrUpdate(entry);
    }

    public static abstract class Property<V> {
        private final String key;

        public Property(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public abstract String toStorageFormat(V value);
        public abstract V fromStorageFormat(String stored);
    }

    public static class StringProperty extends Property<String> {
        public StringProperty(String key) { super(key); }

        public String toStorageFormat(String value) {
            return value;
        }

        public String fromStorageFormat(String stored) {
            return stored;
        }
    }

    public static class ListProperty extends Property<List<String>> {
        public ListProperty(String key) { super(key); }

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

    public static class IntegerProperty extends Property<Integer> {
        public IntegerProperty(String key) { super(key); }

        public String toStorageFormat(Integer value) {
            return value.toString();
        }

        public Integer fromStorageFormat(String stored) {
            return new Integer(stored);
        }
    }
}
