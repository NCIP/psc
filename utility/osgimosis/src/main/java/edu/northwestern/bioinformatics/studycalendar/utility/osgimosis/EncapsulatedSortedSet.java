package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import java.util.SortedSet;
import java.util.Comparator;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
public class EncapsulatedSortedSet<T> extends EncapsulatedSet<T> implements SortedSet<T> {
    private SortedSet farSet;

    public EncapsulatedSortedSet(SortedSet farCollection, Membrane membrane) {
        super(farCollection, membrane);
        this.farSet = farCollection;
    }

    public SortedSet getFarSortedSet() {
        return farSet;
    }

    public Comparator<? super T> comparator() {
        return (Comparator) getMembrane().farToNear(getFarSortedSet().comparator());
    }

    public SortedSet<T> subSet(T fromElement, T toElement) {
        return (SortedSet<T>) getMembrane().farToNear(
            getFarSortedSet().subSet(nearToFar(fromElement), nearToFar(toElement)));
    }

    public SortedSet<T> headSet(T toElement) {
        return (SortedSet<T>) getMembrane().farToNear(getFarSortedSet().headSet(nearToFar(toElement)));
    }

    public SortedSet<T> tailSet(T fromElement) {
        return (SortedSet<T>) getMembrane().farToNear(getFarSortedSet().tailSet(nearToFar(fromElement)));
    }

    public T first() {
        return (T) getMembrane().farToNear(getFarSortedSet().first());
    }

    public T last() {
        return (T) getMembrane().farToNear(getFarSortedSet().last());
    }
}
