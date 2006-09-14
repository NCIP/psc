package edu.northwestern.bioinformatics.studycalendar.utils;

import edu.nwu.bioinformatics.commons.DelegatingMap;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class ExpandingMap<K, V> extends DelegatingMap<K, V> {
    private Map<K, V> delegate;
    private Filler<V> filler;

    public ExpandingMap() {
        this(null, null);
    }

    public ExpandingMap(Filler<V> filler) {
        this(filler, null);
    }

    public ExpandingMap(Map<K, V> delegate) {
        this(null, delegate);
    }

    public ExpandingMap(Filler<V> filler, Map<K, V> delegate) {
        this.delegate = delegate == null ? new HashMap<K, V>() : delegate;
        this.filler = filler == null ? new NullFiller<V>() : filler;
    }

    protected Map<K, V> getDelegateMap() {
        return delegate;
    }

    ////// EXPANDING BEHAVIOR

    public V get(Object key) {
        if (!containsKey(key)) {
            getDelegateMap().put((K) key, filler.createNew(key));
        }
        return super.get(key);
    }

    ////// FILLERS

    public static interface Filler<V> {
        V createNew(Object key);
    }

    public static class StaticFiller<V> implements Filler<V> {
        private V fill;

        public StaticFiller(V fill) {
            this.fill = fill;
        }

        public V createNew(Object key) {
            return fill;
        }
    }

    public static final class NullFiller<V> extends StaticFiller<V> {
        public NullFiller() { super(null); }
    }

}
