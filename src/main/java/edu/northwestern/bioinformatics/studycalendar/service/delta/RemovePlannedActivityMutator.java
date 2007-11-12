package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Revision;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;

/**
 * @author Rhett Sutphin
 */
public class RemovePlannedActivityMutator extends RemoveMutator {
    public RemovePlannedActivityMutator(Remove remove, PlannedActivityDao dao) {
        super(remove, dao);
    }

    @Override
    public boolean appliesToExistingSchedules() {
        return true;
    }

    @Override
    public void apply(ScheduledCalendar calendar) {
        PlannedActivity removedPlannedActivity = (PlannedActivity) findChild();
        Revision revision = change.getDelta().getRevision();
        for (ScheduledArm scheduledArm : calendar.getScheduledArms()) {
            for (ScheduledActivity event : scheduledArm.getEvents()) {
                if (removedPlannedActivity.equals(event.getPlannedActivity())) {
                    event.unscheduleIfOutstanding("Removed in revision " + revision.getDisplayName());
                }
            }
        }
    }
}

