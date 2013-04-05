/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class SimpleRevision implements Revision {
    private List<Delta<?>> deltas;

    public SimpleRevision(List<Delta<?>> deltas) {
        this.deltas = deltas;
    }
    
    public static SimpleRevision create(Delta<?>... deltas) {
        return new SimpleRevision(new ArrayList<Delta<?>>(Arrays.asList(deltas)));
    }

    public String getDisplayName() {
        return "Test revision";
    }

    public List<Delta<?>> getDeltas() {
        return deltas;
    }
}
