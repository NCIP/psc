package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledEventDao;

import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
abstract class AbstractChangePlannedEventMutator extends SimplePropertyChangeMutator {
    protected ScheduledEventDao scheduledEventDao;

    public AbstractChangePlannedEventMutator(PropertyChange change, ScheduledEventDao scheduledEventDao) {
        super(change);
        this.scheduledEventDao = scheduledEventDao;
    }

    @Override public boolean appliesToExistingSchedules() { return true; }

    @Override
    public abstract void apply(ScheduledCalendar calendar);

    public Collection<ScheduledEvent> findEventsToMutate(ScheduledCalendar calendar) {
        return scheduledEventDao.getEventsFromPlannedEvent(
            (PlannedActivity) (PlanTreeNode) change.getDelta().getNode(), calendar);
    }
}
