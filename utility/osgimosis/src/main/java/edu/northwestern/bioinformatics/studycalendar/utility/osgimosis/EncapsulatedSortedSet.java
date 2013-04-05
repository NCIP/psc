/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import java.util.SortedSet;
import java.util.Comparator;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
public class EncapsulatedSortedSet<T> extends EncapsulatedSet<T> implements SortedSet<T> {
    private SortedSet farSet;

    public EncapsulatedSortedSet(SortedSet farCollection, Membrane membrane, ClassLoader nearClassLoader) {
        super(farCollection, membrane, nearClassLoader);
        this.farSet = farCollection;
    }

    public SortedSet getFarSortedSet() {
        return farSet;
    }

    public Comparator<? super T> comparator() {
        return (Comparator) farToNear(getFarSortedSet().comparator());
    }

    public SortedSet<T> subSet(T fromElement, T toElement) {
        return (SortedSet<T>) farToNear(
            getFarSortedSet().subSet(nearToFar(fromElement), nearToFar(toElement)));
    }

    public SortedSet<T> headSet(T toElement) {
        return (SortedSet<T>) farToNear(getFarSortedSet().headSet(nearToFar(toElement)));
    }

    public SortedSet<T> tailSet(T fromElement) {
        return (SortedSet<T>) farToNear(getFarSortedSet().tailSet(nearToFar(fromElement)));
    }

    public T first() {
        return (T) farToNear(getFarSortedSet().first());
    }

    public T last() {
        return (T) farToNear(getFarSortedSet().last());
    }
}
