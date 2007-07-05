package edu.northwestern.bioinformatics.studycalendar.utils.configuration;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.Set;
import java.util.Collection;

/**
 * Provides a base DAO class for an EAV-style configuration system for an application.
 * The actual database records are modeled by {@link ConfigurationEntry}.  The database
 * table should be created something like this (this is for PostgreSQL; others will be different):
 * <pre>CREATE TABLE configuration (
 *   key TEXT PRIMARY KEY,
 *   value TEXT,
 *   version INTEGER NOT NULL DEFAULT 0
 * );</pre>
 * Or, using Bering:
 * <pre>    void up() {
 *       createTable("configuration") { t ->
 *           t.includePrimaryKey = false
 *           t.addColumn("key", "string", primaryKey: true)
 *           t.addColumn("value", "string")
 *           t.addVersionColumn()
 *       }
 *   }</pre> 
 *
 * @see ConfigurationEntry
 * @see ConfigurationProperties
 * @see ConfigurationProperty
 * @author Rhett Sutphin
 */
public abstract class DatabaseBackedConfiguration extends HibernateDaoSupport {
    private java.util.Map<String, Object> map;

    /**
     * Subclasses must implement this.  Most of the time, the returned
     * instance will be <code>static</code>ly configured in the subclass.
     */
    public abstract ConfigurationProperties getProperties();

    ////// GET/SET TO DB

    public <V> V get(ConfigurationProperty<V> property) {
        ConfigurationEntry entry
            = (ConfigurationEntry) getHibernateTemplate().get(ConfigurationEntry.class, property.getKey());
        if (entry == null) {
            return property.getDefault();
        } else {
            return entry.getValue() == null
                ? null
                : property.fromStorageFormat(entry.getValue());
        }
    }

    public <V> V getDefault(ConfigurationProperty<V> property) {
        return property.getDefault();
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
            return getProperties().size();
        }

        public boolean isEmpty() {
            return false;
        }

        public boolean containsKey(Object key) {
            return getProperties().containsKey((String) key);
        }

        public boolean containsValue(Object value) {
            // if you want to actually implement this, you need to do an
            // exhaustive search, so let's skip it for now
            throw new UnsupportedOperationException("not implemented");
        }

        public Object get(Object key) {
            ConfigurationProperty<?> property = getProperties().get((String) key);
            return property == null ? null : DatabaseBackedConfiguration.this.get(property);
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
