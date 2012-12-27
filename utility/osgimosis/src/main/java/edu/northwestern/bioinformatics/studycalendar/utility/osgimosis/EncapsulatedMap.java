/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "unchecked" })
public class EncapsulatedMap<K, V> implements Map<K, V> {
    private final Map<Object, Object> farMap;
    private final Membrane membrane;
    private final ClassLoader nearClassLoader;

    public EncapsulatedMap(
        Map<Object, Object> farMap, Membrane membrane, ClassLoader nearClassLoader
    ) {
        this.farMap = farMap;
        this.membrane = membrane;
        this.nearClassLoader = nearClassLoader;
    }

    protected Map<Object, Object> getFarMap() {
        return farMap;
    }

    public int size() {
        return getFarMap().size();
    }

    public boolean isEmpty() {
        return getFarMap().isEmpty();
    }

    public boolean containsKey(Object nearKey) {
        return getFarMap().containsKey(nearToFar(nearKey));
    }

    public boolean containsValue(Object nearValue) {
        return getFarMap().containsValue(nearToFar(nearValue));
    }

    public V get(Object nearKey) {
        return (V) farToNear(getFarMap().get(nearToFar(nearKey)));
    }

    public V put(K nearKey, V nearValue) {
        Object farOldValue = getFarMap().put(nearToFar(nearKey), nearToFar(nearValue));
        return (V) farToNear(farOldValue);
    }

    public V remove(Object nearKey) {
        return (V) farToNear(getFarMap().remove(nearToFar(nearKey)));
    }

    public void putAll(Map<? extends K, ? extends V> nearMap) {
        for (Entry<? extends K, ? extends V> nearEntry : nearMap.entrySet()) {
            getFarMap().put(nearToFar(nearEntry.getKey()), nearToFar(nearEntry.getValue()));
        }
    }

    public void clear() {
        getFarMap().clear();
    }

    public Set<K> keySet() {
        return new EncapsulatedSet<K>(getFarMap().keySet(), getMembrane(), getNearClassLoader());
    }

    public Collection<V> values() {
        return new EncapsulatedCollection<V>(
            getFarMap().values(), getMembrane(), getNearClassLoader());
    }

    public Set<Entry<K, V>> entrySet() {
        return new EncapsulatedSet<Entry<K, V>>(
            getFarMap().entrySet(), getMembrane(), getNearClassLoader());
    }

    //////

    protected Membrane getMembrane() {
        return membrane;
    }

    protected ClassLoader getNearClassLoader() {
        return nearClassLoader;
    }

    protected Object nearToFar(Object element) {
        return getMembrane().traverse(element, guessFarLoader());
    }

    protected Object farToNear(Object element) {
        return getMembrane().traverse(element, getNearClassLoader());
    }

    protected ClassLoader guessFarLoader() {
        if (getFarMap().isEmpty()) {
            // TODO: this is probably insufficient
            return Object.class.getClassLoader();
        } else {
            return getFarMap().values().iterator().next().getClass().getClassLoader();
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ':' + getFarMap().toString();
    }
}
