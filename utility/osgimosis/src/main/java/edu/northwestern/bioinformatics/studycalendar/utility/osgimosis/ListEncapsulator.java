package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import java.util.List;

/**
 * @author Jalpa Patel
 */
public class ListEncapsulator implements Encapsulator {
    private Membrane membrane;
    private ClassLoader nearClassLoader;

    public ListEncapsulator(Membrane membrane, ClassLoader nearClassLoader) {
        this.membrane = membrane;
        this.nearClassLoader = nearClassLoader;
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public Object encapsulate(Object far) {
        return new EncapsulatedList((List) far, membrane, nearClassLoader);
    }
}
