package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import java.util.List;

/**
 * @author Rhett Sutphin
 * @see Amendment
 */
public class Customization implements Revision {
    public String getDisplayName() {
        throw new UnsupportedOperationException("getDisplayName not implemented");
        // return null;
    }

    public List<Delta<?>> getDeltas() {
        throw new UnsupportedOperationException("getDeltas not implemented");
        // return null;
    }
}
