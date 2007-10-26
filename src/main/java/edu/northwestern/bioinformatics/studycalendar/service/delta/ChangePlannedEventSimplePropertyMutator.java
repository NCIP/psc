package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledEventDao;

import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
public class ChangePlannedEventSimplePropertyMutator extends SimplePropertyChangeMutator {
    private ScheduledEventDao scheduledEventDao;

    public ChangePlannedEventSimplePropertyMutator(PropertyChange change, ScheduledEventDao scheduledEventDao) {
        super(change);
        this.scheduledEventDao = scheduledEventDao;
    }

    @Override public boolean appliesToExistingSchedules() { return true; }

    @Override
    public void apply(ScheduledCalendar calendar) {
        for (ScheduledEvent event : findEventsToMutate(calendar)) {
            if (event.getCurrentState().getMode().isOutstanding()) {
                bind(event, change.getNewValue());
            }
        }
    }

    public Collection<ScheduledEvent> findEventsToMutate(ScheduledCalendar calendar) {
        return scheduledEventDao.getEventsFromPlannedEvent(
            (PlannedEvent) (PlanTreeNode) change.getDelta().getNode(), calendar);
    }
}
