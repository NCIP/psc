package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import java.util.List;

/**
 * @author Jalpa Patel
 */
public class ListEncapsulator implements Encapsulator{
    private Membrane membrane;

    public ListEncapsulator(Membrane membrane) {
        this.membrane = membrane;
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public Object encapsulate(Object far) {
        return new EncapsulatedList((List) far, membrane);
    }
}
