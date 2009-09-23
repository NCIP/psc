package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import java.util.SortedSet;

/**
 * @author Rhett Sutphin
 */
public class SortedSetEncapsulator implements Encapsulator {
    private Membrane membrane;

    public SortedSetEncapsulator(Membrane membrane) {
        this.membrane = membrane;
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public Object encapsulate(Object far) {
        return new EncapsulatedSortedSet((SortedSet) far, membrane);
    }
}
