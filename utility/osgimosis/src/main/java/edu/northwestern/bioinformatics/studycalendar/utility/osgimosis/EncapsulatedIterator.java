package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import java.util.Iterator;

/**
 * @author Rhett Sutphin
 */
public class EncapsulatedIterator<T> implements Iterator<T> {
    private Iterator<?> farIterator;
    private Membrane membrane;

    public EncapsulatedIterator(Iterator<?> farIterator, Membrane membrane) {
        this.farIterator = farIterator;
        this.membrane = membrane;
    }

    public boolean hasNext() {
        return farIterator.hasNext();
    }

    @SuppressWarnings({ "unchecked" })
    public T next() {
        return (T) membrane.farToNear(farIterator.next());
    }

    public void remove() {
        farIterator.remove();
    }
}
