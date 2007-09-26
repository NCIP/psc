package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public interface Revision {
    String getDisplayName();
    List<Delta<?>> getDeltas();
}
