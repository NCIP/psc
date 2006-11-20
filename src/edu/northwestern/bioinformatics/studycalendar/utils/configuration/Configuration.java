package edu.northwestern.bioinformatics.studycalendar.utils.configuration;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.Properties;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class Configuration extends HibernateDaoSupport {
    private static final String DEFAULTS_RESOURCE = "defaultConfiguration.properties";

    public static final ConfigurationProperty<String>
        DEPLOYMENT_NAME = new ConfigurationProperty.Text("deploymentName");
    public static final ConfigurationProperty<String>
        MAIL_REPLY_TO = new ConfigurationProperty.Text("replyTo");
    public static final ConfigurationProperty<List<String>>
        MAIL_EXCEPTIONS_TO = new ConfigurationProperty.Csv("mailExceptionsTo");
    public static final ConfigurationProperty<String>
        SMTP_HOST = new ConfigurationProperty.Text("smtpHost");
    public static final ConfigurationProperty<Integer>
        SMTP_PORT = new ConfigurationProperty.Int("smtpPort");
    public static final ConfigurationProperty<Boolean>
        SHOW_FULL_EXCEPTIONS = new ConfigurationProperty.Bool("showFullExceptions");
    public static final ConfigurationProperty<Boolean>
        SHOW_DEBUG_INFORMATION = new ConfigurationProperty.Bool("showDebugInformation");

    private java.util.Map<String, Object> map;
    private Properties defaults;

    public Configuration() {
        defaults = new Properties();
        try {
            defaults.load(getClass().getResourceAsStream(DEFAULTS_RESOURCE));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load defaults " + DEFAULTS_RESOURCE, e);
        }
    }

    public <V> V get(ConfigurationProperty<V> property) {
        return parseValue(property, getValue(property.getKey()));
    }

    public <V> V getDefault(ConfigurationProperty<V> property) {
        return parseValue(property, defaults.getProperty(property.getKey()));
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

    private <V> V parseValue(ConfigurationProperty<V> property, String value) {
        return value == null ? null : property.fromStorageFormat(value);
    }

    public <V> void set(ConfigurationProperty<V> property, V value) {
        ConfigurationEntry entry
            = (ConfigurationEntry) getHibernateTemplate().get(ConfigurationEntry.class, property.getKey());
        if (entry == null) {
            entry = new ConfigurationEntry();
            entry.setKey(property.getKey());
        }
        entry.setValue(property.toStorageFormat(value));
        getHibernateTemplate().saveOrUpdate(entry);
    }

    public java.util.Map<String, Object> getMap() {
        if (map == null) map = new Map();
        return map;
    }

    private class Map implements java.util.Map<String, Object> {
        public int size() {
            return ConfigurationProperty.values().size();
        }

        public boolean isEmpty() {
            return false;
        }

        public boolean containsKey(Object key) {
            return ConfigurationProperty.keys().contains(key);
        }

        public boolean containsValue(Object value) {
            return ConfigurationProperty.values().contains(value);
        }

        public Object get(Object key) {
            ConfigurationProperty<?> property = ConfigurationProperty.getPropertyForKey((String) key);
            return property == null ? null : Configuration.this.get(property);
        }

        ////// COLLECTIVE INTERFACES NOT IMPLEMENTED //////

        public Set<String> keySet() {
            throw new UnsupportedOperationException("keySet not implemented");
        }

        public Collection<Object> values() {
            throw new UnsupportedOperationException("values not implemented");
        }

        public Set<Entry<String, Object>> entrySet() {
            throw new UnsupportedOperationException("entrySet not implemented");
        }

        ////// READ-ONLY //////

        public Object put(String key, Object value) {
            throw new UnsupportedOperationException("Configuration map is read-only");
        }

        public Object remove(Object key) {
            throw new UnsupportedOperationException("Configuration map is read-only");
        }

        public void putAll(java.util.Map<? extends String, ? extends Object> t) {
            throw new UnsupportedOperationException("Configuration map is read-only");
        }

        public void clear() {
            throw new UnsupportedOperationException("Configuration map is read-only");
        }
    }
}
