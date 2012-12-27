/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
// TODO: Since Customization has been dropped from the design, this interface may no longer be necessary.
public interface Revision {
    String getDisplayName();
    List<Delta<?>> getDeltas();
}
