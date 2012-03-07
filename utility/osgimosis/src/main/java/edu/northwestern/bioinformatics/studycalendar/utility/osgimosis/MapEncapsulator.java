package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class MapEncapsulator implements Encapsulator {
    private Membrane membrane;
    private ClassLoader nearClassLoader;

    public MapEncapsulator(Membrane membrane, ClassLoader nearClassLoader) {
        this.membrane = membrane;
        this.nearClassLoader = nearClassLoader;
    }

    @SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
    public Object encapsulate(Object far) {
        return new EncapsulatedMap((Map) far, membrane, nearClassLoader);
    }
}