package edu.northwestern.bioinformatics.studycalendar.utils;

import edu.nwu.bioinformatics.commons.DelegatingMap;
import edu.nwu.bioinformatics.commons.DelegatingSortedMap;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Rhett Sutphin
 */
public class ExpandingMap<K, V> extends DelegatingSortedMap<K, V> {
    private SortedMap<K, V> delegate;
    private Filler<V> filler;

    public ExpandingMap() {
        this(null, null);
    }

    public ExpandingMap(Filler<V> filler) {
        this(filler, null);
    }

    public ExpandingMap(SortedMap<K, V> delegate) {
        this(null, delegate);
    }

    public ExpandingMap(Filler<V> filler, SortedMap<K, V> delegate) {
        this.delegate = delegate == null ? new TreeMap<K, V>() : delegate;
        this.filler = filler == null ? new NullFiller<V>() : filler;
    }

    @Override
    protected SortedMap<K, V> getDelegateSortedMap() {
        return delegate;
    }

    ////// EXPANDING BEHAVIOR

    @Override
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

    public static class ConstructorFiller<V> implements Filler<V> {
        private Class<? extends V> clazz;


        public ConstructorFiller(Class<? extends V> clazz) {
            this.clazz = clazz;
        }

        public V createNew(Object key) {
            try {
                return clazz.newInstance();
            } catch (InstantiationException e) {
                throw new StudyCalendarSystemException("Could not instantiate " + clazz + ".  Does it have a public default constructor?", e);
            } catch (IllegalAccessException e) {
                throw new StudyCalendarSystemException("Could not instantiate " + clazz + ".  Does it have a public default constructor?", e);
            }
        }
    }

    public static final class NullFiller<V> extends StaticFiller<V> {
        public NullFiller() { super(null); }
    }

}
