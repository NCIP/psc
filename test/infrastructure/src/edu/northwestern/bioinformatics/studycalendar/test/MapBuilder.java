package edu.northwestern.bioinformatics.studycalendar.test;

import org.springframework.util.Assert;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Chainable creator for maps a la {@link StringBuilder}.  Might want to move
 * to ctms-commons if it proves useful.
 *
 * @author Rhett Sutphin
 */
public class MapBuilder<K, V> {
    private Map<K, V> map;

    public MapBuilder() {
        this(null);
    }

    public MapBuilder(Map<K, V> map) {
        this.map = map == null ? createDefaultMap() : map;
    }

    private LinkedHashMap<K, V> createDefaultMap() {
        return new LinkedHashMap<K, V>();
    }

    public static <K, V> Map<K, V> zip(List<K> keys, List<V> values) {
        Assert.state(keys.size() >= values.size(), "Must have at least as many keys as values");
        Iterator<K> kIt = keys.iterator();
        Iterator<V> vIt = values.iterator();
        MapBuilder<K, V> builder = new MapBuilder<K,V>();
        while (kIt.hasNext()) {
            if (vIt.hasNext()) {
                builder.put(kIt.next(), vIt.next());
            } else {
                builder.put(kIt.next(), null);
            }
        }
        return builder.toMap();
    }

    public MapBuilder<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    public Map<K, V> toMap() {
        return map;
    }
}
