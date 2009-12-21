package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
public class CollectionEncapsulator implements Encapsulator {
    private Membrane membrane;
    private ClassLoader nearClassLoader;

    public CollectionEncapsulator(Membrane membrane, ClassLoader nearClassLoader) {
        this.membrane = membrane;
        this.nearClassLoader = nearClassLoader;
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public Object encapsulate(Object far) {
        return new EncapsulatedCollection((Collection) far, membrane, nearClassLoader);
    }
}
