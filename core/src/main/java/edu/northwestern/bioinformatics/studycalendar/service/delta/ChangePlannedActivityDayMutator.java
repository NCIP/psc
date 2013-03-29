/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;

/**
 * @author Rhett Sutphin
 */
public class ChangePlannedActivityDayMutator extends AbstractChangePlannedActivityMutator {
    private ScheduleService scheduleService;
    private int amount;

    public ChangePlannedActivityDayMutator(
        PropertyChange change, ScheduleService scheduleService
    ) {
        super(change);
        this.scheduleService = scheduleService;
        amount = Integer.parseInt(change.getNewValue()) - Integer.parseInt(change.getOldValue());
    }

    @Override
    public void apply(ScheduledCalendar calendar) {
        if (amount != 0) {
            for (ScheduledActivity event : findEventsToMutate(calendar)) {
                scheduleService.reviseDate(event, amount, change.getDelta().getRevision());
            }
        }
    }
}
