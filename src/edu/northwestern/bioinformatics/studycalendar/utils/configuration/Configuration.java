package edu.northwestern.bioinformatics.studycalendar.utils.configuration;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.Properties;
import java.util.List;
import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class Configuration extends HibernateDaoSupport {
    private static final String DEFAULTS_RESOURCE = "defaultConfiguration.properties";

    private Properties defaults;

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
}
