package org.dynamicjava.api_bridge;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "unchecked" })
public class DelegatorCollection<T> extends AbstractDelegator implements Collection<T> {
    public DelegatorCollection(Collection<T> apiCollection, ApiBridge apiBridge) {
        super(apiCollection, apiBridge);
    }

    public int size() {
        return getDelegateCollection().size();
    }

    private Collection<T> getDelegateCollection() {
        return (Collection<T>) getDelegate();
    }

    public boolean isEmpty() {
        return getDelegateCollection().isEmpty();
    }

    public boolean contains(Object o) {
        return getDelegateCollection().contains(reverseBridge(o));
    }

    public Iterator<T> iterator() {
        return new DelegatorIterator<T>(getDelegateCollection().iterator(), getApiBridge());
    }

    public Object[] toArray() {
        Object[] array = new Object[this.size()];
        int i = 0;
        for (T t : this) {
            array[i] = t;
            i++;
        }
        return array;
    }

    public <K> K[] toArray(K[] a) {
        if (a.length == 0) {
            a = (K[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size());
        }
        int i = 0;
        Object[] out = a;
        for (T t : this) {
            out[i] = t;
            i++;
        }
        return a;
    }

    public boolean add(T o) {
        return getDelegateCollection().add((T) reverseBridge(o));
    }

    public boolean remove(Object o) {
        return getDelegateCollection().remove(reverseBridge(o));
    }

    public boolean containsAll(Collection<?> c) {
        return getDelegateCollection().containsAll(new DelegatorCollection(c, getDelegateApiBridge()));
    }

    public boolean addAll(Collection<? extends T> c) {
        return getDelegateCollection().addAll(new DelegatorCollection(c, getDelegateApiBridge()));
    }

    public boolean removeAll(Collection<?> c) {
        return getDelegateCollection().removeAll(new DelegatorCollection(c, getDelegateApiBridge()));
    }

    public boolean retainAll(Collection<?> c) {
        return getDelegateCollection().retainAll(new DelegatorCollection(c, getDelegateApiBridge()));
    }

    public void clear() {
        getDelegateCollection().clear();
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).
            append("[delegate=").append(getDelegateCollection()).append(']').
            toString();
    }
}
