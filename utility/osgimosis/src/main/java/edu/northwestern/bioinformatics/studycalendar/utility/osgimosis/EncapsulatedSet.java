package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import java.util.Set;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class EncapsulatedSet<T> extends EncapsulatedCollection<T> implements Set<T> {
    public EncapsulatedSet(Set farSet, Membrane membrane) {
        super(farSet, membrane);
    }
}
