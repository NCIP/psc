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
        DEPLOYMENT_NAME = new Property.Text("deploymentName");
    public static final Property<String>
        MAIL_REPLY_TO = new Property.Text("replyTo");
    public static final Property<List<String>>
        MAIL_EXCEPTIONS_TO = new Property.Csv("mailExceptionsTo");
    public static final Property<String>
        SMTP_HOST = new Property.Text("smtpHost");
    public static final Property<Integer>
        SMTP_PORT = new Property.Int("smtpPort");

    public Configuration() {
        defaults = new Properties();
        try {
            defaults.load(getClass().getResourceAsStream(DEFAULTS_RESOURCE));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load defaults " + DEFAULTS_RESOURCE, e);
        }
    }

    public <V> V get(Property<V> property) {
        return parseValue(property, getValue(property.getKey()));
    }

    public <V> V getDefault(Property<V> property) {
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

    private <V> V parseValue(Property<V> property, String value) {
        return value == null ? null : property.fromStorageFormat(value);
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
}
