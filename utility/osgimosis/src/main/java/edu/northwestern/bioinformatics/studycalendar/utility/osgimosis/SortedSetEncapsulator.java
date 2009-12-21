package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import java.util.SortedSet;

/**
 * @author Rhett Sutphin
 */
public class SortedSetEncapsulator implements Encapsulator {
    private Membrane membrane;
    private ClassLoader nearClassLoader;

    public SortedSetEncapsulator(Membrane membrane, ClassLoader nearClassLoader) {
        this.membrane = membrane;
        this.nearClassLoader = nearClassLoader;
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public Object encapsulate(Object far) {
        return new EncapsulatedSortedSet((SortedSet) far, membrane, nearClassLoader);
    }
}
