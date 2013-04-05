/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import java.util.Iterator;

/**
 * @author Rhett Sutphin
 */
public class EncapsulatedIterator<T> implements Iterator<T> {
    private Iterator<?> farIterator;
    private Membrane membrane;
    private ClassLoader nearClassLoader;

    public EncapsulatedIterator(Iterator<?> farIterator, Membrane membrane, ClassLoader nearClassLoader) {
        this.farIterator = farIterator;
        this.membrane = membrane;
        this.nearClassLoader = nearClassLoader;
    }

    public boolean hasNext() {
        return farIterator.hasNext();
    }

    @SuppressWarnings({ "unchecked" })
    public T next() {
        return (T) farToNear(farIterator.next());
    }

    public void remove() {
        farIterator.remove();
    }
    
    protected Iterator<?> getFarIterator() {
        return farIterator;
    }

    protected ClassLoader getNearClassLoader() {
        return nearClassLoader;
    }

    protected Membrane getMembrane() {
        return membrane;
    }

    protected Object farToNear(Object element) {
        return getMembrane().traverse(element, nearClassLoader);
    }
}
