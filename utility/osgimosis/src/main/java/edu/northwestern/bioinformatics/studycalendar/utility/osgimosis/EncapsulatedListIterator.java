/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import java.util.ListIterator;

/**
 * @author Jalpa Patel
 */
@SuppressWarnings({ "unchecked", "RawUseOfParameterizedType" })
public class EncapsulatedListIterator<E> extends EncapsulatedIterator<E> implements ListIterator<E> {
    private ClassLoader farClassLoader;

    public EncapsulatedListIterator(ListIterator<?> farIterator, Membrane membrane, ClassLoader nearClassLoader, ClassLoader farClassLoader) {
        super(farIterator, membrane, nearClassLoader);
        this.farClassLoader = farClassLoader;
    }

    protected ListIterator getFarListIterator() {
        return (ListIterator<?>) getFarIterator();
    }

    public boolean hasPrevious() {
        return getFarListIterator().hasPrevious();
    }

    public E previous() {
        return (E) farToNear(getFarListIterator().previous());
    }

    public int nextIndex() {
        return getFarListIterator().nextIndex();
    }

    public int previousIndex() {
        return getFarListIterator().previousIndex();
    }

    public void set(E o) {
        getFarListIterator().set(nearToFar(o));
    }

    public void add(E o) {
        getFarListIterator().add(nearToFar(o));
    }

    private E nearToFar(E o) {
        return (E) getMembrane().traverse(o, farClassLoader);
    }
}
