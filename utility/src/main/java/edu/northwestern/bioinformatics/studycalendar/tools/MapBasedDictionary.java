/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

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

    public static <K, V> Dictionary<K, V> copy(Map<K, V> src) {
        return new MapBasedDictionary<K,V>(new LinkedHashMap<K, V>(src));
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

    @Override
    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MapBasedDictionary)) return false;

        MapBasedDictionary that = (MapBasedDictionary) o;

        if (map != null ? !map.equals(that.map) : that.map != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return map != null ? map.hashCode() : 0;
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).
            append("[map=").append(map).append(']').toString();
    }
}
