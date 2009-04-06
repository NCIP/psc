package edu.northwestern.bioinformatics.studycalendar.tools;

import org.apache.commons.collections.iterators.IteratorEnumeration;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Facade for using a Map where a dictionary is required without
 * Hashtable's no-null values restriction.
 *
 * @author Rhett Sutphin
 */
public class MapBasedDictionary<K, V> extends Dictionary<K, V> {
    private Map<K,V> map;

    public MapBasedDictionary() {
        this(new LinkedHashMap<K, V>());
    }

    public MapBasedDictionary(Map<K, V> map) {
        this.map = map;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public Enumeration<K> keys() {
        return new IteratorEnumeration(map.keySet().iterator());
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public Enumeration<V> elements() {
        return new IteratorEnumeration(map.values().iterator());
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
        return map.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return map.remove(key);
    }
}
