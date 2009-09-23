package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import java.util.Set;

/**
 * @author Rhett Sutphin
 */
public class SetEncapsulator implements Encapsulator {
    private Membrane membrane;

    public SetEncapsulator(Membrane membrane) {
        this.membrane = membrane;
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public Object encapsulate(Object far) {
        return new EncapsulatedSet((Set) far, membrane);
    }
}