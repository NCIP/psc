package edu.northwestern.bioinformatics.studycalendar.security;

import gov.nih.nci.cabig.ctms.tools.configuration.AbstractConfiguration;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationEntry;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * Provides the {@link Configuration} interface over a String-String {@link Dictionary}.
 * This takes advantage of {@link ConfigurationProperty}'s built-in to/from string
 * functionality to create a non-database, easily serializable configuration representation.
 * In particular, it is compatible with OSGi's Configuration Admin service.
 *
 * @author Rhett Sutphin
 */
public class DictionaryConfiguration extends AbstractConfiguration {
    private ConfigurationProperties configurationProperties;
    private Dictionary<String, String> storage;

    public DictionaryConfiguration(ConfigurationProperties configurationProperties) {
        this(configurationProperties, null);
    }

    public DictionaryConfiguration(ConfigurationProperties configurationProperties, Dictionary<String, String> storage) {
        this.configurationProperties = configurationProperties;
        this.storage = storage == null ?  new Hashtable<String, String>() : storage;
    }

    public Dictionary<String, String> getDictionary() {
        return storage;
    }

    @Override
    protected <V> ConfigurationEntry getEntry(ConfigurationProperty<V> vConfigurationProperty) {
        String key = vConfigurationProperty.getKey();
        if (hasKey(key)) {
            return new Entry(key);
        } else {
            return null;
        }
    }

    private boolean hasKey(String key) {
        if (storage.get(key) != null) return true;
        Enumeration<String> keys = storage.keys();
        while (keys.hasMoreElements()) {
            if (keys.nextElement().equals(key)) return true;
        }
        return false;
    }

    @Override
    protected void store(ConfigurationEntry configurationEntry) {
    }

    @Override
    protected void remove(ConfigurationEntry configurationEntry) {
        storage.remove(configurationEntry.getKey());
    }

    @Override
    protected Class<? extends ConfigurationEntry> getConfigurationEntryClass() {
        return Entry.class;
    }

    @Override
    protected <V> ConfigurationEntry createNewEntry(ConfigurationProperty<V> property) {
        return new Entry(property.getKey());
    }

    public ConfigurationProperties getProperties() {
        return configurationProperties;
    }

    private class Entry extends ConfigurationEntry {
        public Entry(String key) { setKey(key); }

        @Override
        public String getValue() {
            return storage.get(getKey());
        }

        @Override
        public void setValue(String s) {
            storage.put(getKey(), s);
        }
    }
}
