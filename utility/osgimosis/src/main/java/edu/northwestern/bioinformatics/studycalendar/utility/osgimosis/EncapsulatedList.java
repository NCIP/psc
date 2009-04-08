package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import java.util.List;
import java.util.Collection;
import java.util.ListIterator;

/**
 * @author Jalpa Patel
 */
@SuppressWarnings({"unchecked"})
public class EncapsulatedList<E> extends EncapsulatedCollection<E> implements List<E> {
    private List farList;

    public EncapsulatedList(List farList, Membrane membrane) {
        super(farList, membrane);
        this.farList = farList;
    }

    public List getFarList() {
        return farList;
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        int i = index;
        for (E e : c) {
            this.add(i, e);
            i++;
        }
        return index != i;
    }

    public E get(int index) {
        return (E) getMembrane().farToNear(getFarList().get(index));
    }

    public E set(int index, E element) {
        return (E) getMembrane().farToNear(getFarList().set(index, nearToFar(element)));
    }

    public void add(int index, E element) {
        getFarList().add(index, nearToFar(element));
    }

    public E remove(int index) {
        return (E)getMembrane().farToNear(getFarList().remove(index));
    }

    public int indexOf(Object o) {
        return getFarList().indexOf(nearToFar(o));
    }

    public int lastIndexOf(Object o) {
        return getFarList().lastIndexOf(nearToFar(o));
    }

    public ListIterator<E> listIterator() {
        return encapsulateListIterator(getFarList().listIterator());
    }

    public ListIterator<E> listIterator(int index) {
        return encapsulateListIterator(getFarList().listIterator(index));
    }

    private EncapsulatedListIterator<E> encapsulateListIterator(ListIterator farIterator) {
        return new EncapsulatedListIterator<E>(farIterator, getMembrane(), guessFarLoader());
    }

    public List<E> subList(int fromIndex, int toIndex) {
        List farSubList = getFarList().subList(fromIndex, toIndex);
        return new EncapsulatedList<E>(farSubList, getMembrane());
    }
}
