package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;

/**
 * @author Rhett Sutphin
 */
public class ChangePlannedActivitySimplePropertyMutator extends AbstractChangePlannedActivityMutator {

    public ChangePlannedActivitySimplePropertyMutator(PropertyChange change, ScheduledActivityDao scheduledActivityDao) {
        super(change, scheduledActivityDao);
    }

    @Override
    public void apply(ScheduledCalendar calendar) {
        for (ScheduledActivity event : findEventsToMutate(calendar)) {
            if (event.getCurrentState().getMode().isOutstanding()) {
                bind(event, change.getNewValue());
            }
        }
    }

}