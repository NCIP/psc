package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;

import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
abstract class AbstractChangePlannedActivityMutator extends SimplePropertyChangeMutator {
    protected ScheduledActivityDao scheduledActivityDao;

    public AbstractChangePlannedActivityMutator(PropertyChange change, ScheduledActivityDao scheduledActivityDao) {
        super(change);
        this.scheduledActivityDao = scheduledActivityDao;
    }

    @Override public boolean appliesToExistingSchedules() { return true; }

    @Override
    public abstract void apply(ScheduledCalendar calendar);

    public Collection<ScheduledActivity> findEventsToMutate(ScheduledCalendar calendar) {
        return scheduledActivityDao.getEventsFromPlannedActivity(
            (PlannedActivity) (PlanTreeNode) change.getDelta().getNode(), calendar);
    }
}
