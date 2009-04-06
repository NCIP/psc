package org.dynamicjava.api_bridge;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Rhett Sutphin
 */
public class DelegatorCollection<T> implements Collection<T> {
    private ApiBridge apiBridge, delegateApiBridge;
    private Collection<T> apiObject;

    public DelegatorCollection(Collection<T> apiCollection, ApiBridge apiBridge) {
        this.apiObject = apiCollection;
        this.apiBridge = apiBridge;
        this.delegateApiBridge = apiBridge.getReverseApiBridge(apiObject.getClass().getClassLoader());
    }

    public int size() {
        return apiObject.size();
    }

    public boolean isEmpty() {
        return apiObject.isEmpty();
    }

    public boolean contains(Object o) {
        return apiObject.contains(delegateApiBridge.bridge(o, true));
    }

    public Iterator<T> iterator() {
        return new DelegatorIterator<T>(apiObject.iterator(), apiBridge);
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
        return apiObject.add((T) delegateApiBridge.bridge(o, true));
    }

    public boolean remove(Object o) {
        return apiObject.remove(delegateApiBridge.bridge(o, true));
    }

    public boolean containsAll(Collection<?> c) {
        return apiObject.containsAll(new DelegatorCollection(c, delegateApiBridge));
    }

    public boolean addAll(Collection<? extends T> c) {
        return apiObject.addAll(new DelegatorCollection(c, delegateApiBridge));
    }

    public boolean removeAll(Collection<?> c) {
        return apiObject.removeAll(new DelegatorCollection(c, delegateApiBridge));
    }

    public boolean retainAll(Collection<?> c) {
        return apiObject.retainAll(new DelegatorCollection(c, delegateApiBridge));
    }

    public void clear() {
        apiObject.clear();
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).
            append("[delegate=").append(apiObject).append(']').
            toString();
    }
}
