/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import java.util.List;
import java.util.Collection;
import java.util.ListIterator;

/**
 * @author Jalpa Patel
 */
@SuppressWarnings({ "unchecked", "RawUseOfParameterizedType" })
public class EncapsulatedList<E> extends EncapsulatedCollection<E> implements List<E> {
    private List farList;

    public EncapsulatedList(List farList, Membrane membrane, ClassLoader nearClassLoader) {
        super(farList, membrane, nearClassLoader);
        this.farList = farList;
    }

    protected List getFarList() {
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
        return (E) farToNear(getFarList().get(index));
    }

    public E set(int index, E element) {
        return (E) farToNear(getFarList().set(index, nearToFar(element)));
    }

    public void add(int index, E element) {
        getFarList().add(index, nearToFar(element));
    }

    public E remove(int index) {
        return (E) farToNear(getFarList().remove(index));
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
        return new EncapsulatedListIterator<E>(farIterator, getMembrane(), getNearClassLoader(), guessFarLoader());
    }

    public List<E> subList(int fromIndex, int toIndex) {
        List farSubList = getFarList().subList(fromIndex, toIndex);
        return new EncapsulatedList<E>(farSubList, getMembrane(), getNearClassLoader());
    }
}
