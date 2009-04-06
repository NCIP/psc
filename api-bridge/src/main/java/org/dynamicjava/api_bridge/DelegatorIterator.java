package org.dynamicjava.api_bridge;

import java.util.Iterator;

/**
 * @author Rhett Sutphin
 */
public class DelegatorIterator<T> implements Iterator<T> {
    private Iterator<T> apiObject;
    private ApiBridge apiBridge;

    public DelegatorIterator(Iterator<T> apiIterator, ApiBridge apiBridge) {
        apiObject = apiIterator;
        this.apiBridge = apiBridge;
    }

    public boolean hasNext() {
        return apiObject.hasNext();
    }

    public T next() {
        return (T) apiBridge.bridge(apiObject.next(), true);
    }

    public void remove() {
        apiObject.remove();
    }
}
