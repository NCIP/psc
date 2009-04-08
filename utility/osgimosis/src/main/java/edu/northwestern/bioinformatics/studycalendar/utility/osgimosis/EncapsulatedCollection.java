package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class EncapsulatedCollection<E> implements Collection<E> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Collection farCollection;
    private Membrane membrane;

    public EncapsulatedCollection(Collection farCollection, Membrane membrane) {
        this.farCollection = farCollection;
        this.membrane = membrane;
    }

    private Collection getFarCollection() {
        return farCollection;
    }

    public int size() {
        return getFarCollection().size();
    }

    public boolean isEmpty() {
        return getFarCollection().isEmpty();
    }

    public boolean contains(Object o) {
        return getFarCollection().contains(nearToFar(o));
    }

    public Iterator<E> iterator() {
        return new EncapsulatedIterator<E>(farCollection.iterator(), membrane);
    }

    public Object[] toArray() {
        Object[] array = new Object[size()];
        return copyInto(array);
    }

    @SuppressWarnings({ "unchecked" })
    public <T> T[] toArray(T[] a) {
        if (a.length != size()) {
            a = (T[]) Array.newInstance(a.getClass().getComponentType(), size());
        }
        copyInto(a);
        return a;
    }

    private Object[] copyInto(Object[] array) {
        int i = 0;
        for (E e : this) {
            array[i] = e;
            i++;
        }
        return array;
    }

    @SuppressWarnings({ "unchecked" })
    public boolean add(E o) {
        return getFarCollection().add(nearToFar(o));
    }

    public boolean remove(Object o) {
        return getFarCollection().remove(nearToFar(o));
    }

    public boolean containsAll(Collection<?> c) {
        for (Object near : c) {
            if (!contains(near)) return false;
        }
        return true;
    }

    public boolean addAll(Collection<? extends E> c) {
        boolean changed = false;
        for (E e : c) {
            add(e);
            changed = true;
        }
        return changed;
    }

    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object o : c) {
            changed = remove(o) || changed;
        }
        return changed;
    }

    public boolean retainAll(Collection<?> c) {
        boolean changed = false;
        for (Iterator<E> it = this.iterator(); it.hasNext();) {
            if (!c.contains(it.next())) {
                it.remove();
                changed = true;
            }
        }
        return changed;
    }

    public void clear() {
        throw new UnsupportedOperationException("clear not implemented");

    }

    //////

    protected Membrane getMembrane() {
        return membrane;
    }

    protected Object nearToFar(Object element) {
        return membrane.traverse(element, guessFarLoader());
    }

    protected ClassLoader guessFarLoader() {
        if (farCollection.isEmpty()) {
            // TODO: this is probably insufficient
            return Object.class.getClassLoader();
        } else {
            return farCollection.iterator().next().getClass().getClassLoader();
        }
    }
}
