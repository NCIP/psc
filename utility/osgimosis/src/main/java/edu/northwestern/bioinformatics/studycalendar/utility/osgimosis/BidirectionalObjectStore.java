/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A store for pairs of objects.  Any object can be retrieved by providing
 * the corresponding object.  Objects are compared by identity.
 * <p>
 * The store weakly refers to the objects using {@link java.lang.ref.SoftReference}s,
 * ensuring that the store can't use up all the memory in the JVM.
 *
 * @author Rhett Sutphin
 */
public class BidirectionalObjectStore {
    private boolean useSoftReferences;
    private Map<Integer, List<Pair>> pairs;

    public BidirectionalObjectStore() {
        this(true);
    }

    public BidirectionalObjectStore(boolean useSoftReferences) {
        this.useSoftReferences = useSoftReferences;
        this.pairs = new HashMap<Integer, List<Pair>>();
    }

    public synchronized Object get(Object o) {
        purge();
        List<Pair> matches = pairs.get(key(o));
        if (matches != null) {
            for (Pair pair : matches) {
                Object other = pair.correspondent(o);
                if (other != null) return other;
            }
        }
        return null;
    }

    public synchronized void put(Object o1, Object o2) {
        remove(o1); remove(o2);

        Pair pair = useSoftReferences ? new SoftPair(o1, o2) : new StrongPair(o1, o2);
        storeWithKey(key(o1), pair);
        storeWithKey(key(o2), pair);
        purge();
    }

    /**
     * Removes the pair which contains the given object.
     */
    private void remove(Object o) {
        List<Pair> matches = pairs.get(key(o));
        if (matches != null) {
            for (Iterator<Pair> it = matches.iterator(); it.hasNext();) {
                Pair match = it.next();
                if (match.includes(o)) {
                    it.remove();
                    remove(match.correspondent(o));
                }
            }
        }
    }

    private int key(Object o) {
        return System.identityHashCode(o);
    }

    private void storeWithKey(int key, Pair pair) {
        if (!pairs.containsKey(key)) {
            pairs.put(key, new ArrayList<Pair>(2));
        }
        pairs.get(key).add(pair);
    }

    private synchronized void purge() {
        for (Iterator<List<Pair>> pairsIt = pairs.values().iterator(); pairsIt.hasNext();) {
            List<Pair> pairList = pairsIt.next();
            for (Iterator<Pair> lit = pairList.iterator(); lit.hasNext();) {
                Pair pair = lit.next();
                if (!pair.valid()) lit.remove();
            }
            if (pairList.isEmpty()) pairsIt.remove();
        }
    }

    public int referenceCount() {
        purge();
        int i = 0;
        for (List<Pair> list : pairs.values()) {
            i += list.size();
        }
        return i;
    }

    public interface Pair {
        Object correspondent(Object candidate);
        boolean valid();
        boolean includes(Object o);
    }

    private class StrongPair implements Pair {
        private Object one, two;

        private StrongPair(Object one, Object two) {
            this.one = one;
            this.two = two;
        }

        public Object correspondent(Object candidate) {
            if (candidate == one) {
                return two;
            } else if (candidate == two) {
                return one;
            }
            return null;
        }

        public boolean valid() {
            return this.one != null && this.two != null;
        }

        public boolean includes(Object o) {
            return this.one == o || this.two == o;
        }
    }

    private class SoftPair implements Pair {
        private SoftReference<Object> one, two;

        private SoftPair(Object one, Object two) {
            this.one = new SoftReference<Object>(one);
            this.two = new SoftReference<Object>(two);
        }

        public Object correspondent(Object candidate) {
            if (candidate == one.get()) {
                return two.get();
            } else if (candidate == two.get()) {
                return one.get();
            }
            return null;
        }

        public boolean valid() {
            return this.one.get() != null && this.two.get() != null;
        }

        public boolean includes(Object o) {
            return this.one.get() == o || this.two.get() == o;
        }
    }
}
