package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledEventDao;

/**
 * @author Rhett Sutphin
 */
public class ChangePlannedActivitySimplePropertyMutator extends AbstractChangePlannedActivityMutator {

    public ChangePlannedActivitySimplePropertyMutator(PropertyChange change, ScheduledEventDao scheduledEventDao) {
        super(change, scheduledEventDao);
    }

    @Override
    public void apply(ScheduledCalendar calendar) {
        for (ScheduledEvent event : findEventsToMutate(calendar)) {
            if (event.getCurrentState().getMode().isOutstanding()) {
                bind(event, change.getNewValue());
            }
        }
    }

}