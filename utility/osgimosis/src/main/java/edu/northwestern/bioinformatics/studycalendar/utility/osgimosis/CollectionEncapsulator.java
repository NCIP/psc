package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
public class CollectionEncapsulator implements Encapsulator {
    private Membrane membrane;

    public CollectionEncapsulator(Membrane membrane) {
        this.membrane = membrane;
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public Object encapsulate(Object far) {
        return new EncapsulatedCollection((Collection) far, membrane);
    }
}
