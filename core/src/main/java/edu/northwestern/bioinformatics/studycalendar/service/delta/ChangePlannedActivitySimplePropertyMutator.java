/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;

/**
 * @author Rhett Sutphin
 */
public class ChangePlannedActivitySimplePropertyMutator extends AbstractChangePlannedActivityMutator {

    public ChangePlannedActivitySimplePropertyMutator(PropertyChange change) {
        super(change);
    }

    @Override
    public void apply(ScheduledCalendar calendar) {
        for (ScheduledActivity event : findEventsToMutate(calendar)) {
            bind(event, getAssignableValue(change.getNewValue()));
        }
    }
}