/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import java.util.Map;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "unchecked" })
public class EncapsulatedMapEntry<K, V> implements Map.Entry<K, V> {
    private final Map.Entry<Object, Object> farEntry;
    private final Membrane membrane;
    private final ClassLoader nearClassLoader;

    public EncapsulatedMapEntry(
        Map.Entry<Object, Object> farEntry, Membrane membrane, ClassLoader nearClassLoader
    ) {
        this.farEntry = farEntry;
        this.membrane = membrane;
        this.nearClassLoader = nearClassLoader;
    }

    protected Map.Entry<Object, Object> getFarEntry() {
        return farEntry;
    }

    public K getKey() {
        return (K) farToNear(getFarEntry().getKey());
    }

    public V getValue() {
        return (V) farToNear(getFarEntry().getValue());
    }

    public V setValue(V v) {
        return (V) farToNear(getFarEntry().setValue(nearToFar(v)));
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
        if (getFarEntry().getValue() == null) {
            // TODO: this is probably insufficient
            return Object.class.getClassLoader();
        } else {
            return getFarEntry().getValue().getClass().getClassLoader();
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ':' + getFarEntry().toString();
    }
}
