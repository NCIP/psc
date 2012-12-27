/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools.configuration;

import gov.nih.nci.cabig.ctms.tools.configuration.AbstractConfiguration;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationEntry;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.northwestern.bioinformatics.studycalendar.tools.configuration.RawDataConfiguration;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBasedDictionary;

/**
 * Provides the {@link gov.nih.nci.cabig.ctms.tools.configuration.Configuration} interface
 * over a String-String {@link Dictionary}. This takes advantage of
 * {@link ConfigurationProperty}'s built-in to/from string functionality to create a
 * non-database, easily serializable configuration representation.
 * In particular, the dictionaries it produces are compatible with OSGi's Configuration
 * Admin service.
 *
 * @author Rhett Sutphin
 */
public class DictionaryConfiguration extends AbstractConfiguration implements RawDataConfiguration {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private ConfigurationProperties configurationProperties;
    private Dictionary<String, String> storage;

    public DictionaryConfiguration(ConfigurationProperties configurationProperties) {
        this(configurationProperties, null);
    }

    public DictionaryConfiguration(ConfigurationProperties configurationProperties, Dictionary<String, String> storage) {
        this.configurationProperties = configurationProperties;
        this.storage = storage == null ?  new MapBasedDictionary<String, String>() : storage;
    }

    public DictionaryConfiguration(Configuration source) {
        this(source, source.getProperties());
    }

    @SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
    public DictionaryConfiguration(Configuration source, ConfigurationProperties properties) {
        this(properties);
        if (source instanceof RawDataConfiguration) {
            this.storage = MapBasedDictionary.copy(((RawDataConfiguration) source).getRawData());
        } else {
            for (ConfigurationProperty property : getProperties().getAll()) {
                if (source.isSet(property)) {
                    log.trace("Copying {}={} into dictionary", property, source.get(property));
                    this.set(property, source.get(property));
                }
            }
        }
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

    public Map<String, String> getRawData() {
        Map<String, String> data = new LinkedHashMap<String, String>();
        Enumeration<String> keys = getDictionary().keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            data.put(key, getDictionary().get(key));
        }
        return data;
    }

    public ConfigurationProperties getProperties() {
        return configurationProperties;
    }

    public void setConfigurationProperties(ConfigurationProperties configurationProperties) {
        this.configurationProperties = configurationProperties;
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
