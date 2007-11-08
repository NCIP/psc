package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.PlannedEventDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Revision;

/**
 * @author Rhett Sutphin
 */
public class RemovePlannedEventMutator extends RemoveMutator {
    public RemovePlannedEventMutator(Remove remove, PlannedEventDao dao) {
        super(remove, dao);
    }

    @Override
    public boolean appliesToExistingSchedules() {
        return true;
    }

    @Override
    public void apply(ScheduledCalendar calendar) {
        PlannedActivity removedPlannedEvent = (PlannedActivity) findChild();
        Revision revision = change.getDelta().getRevision();
        for (ScheduledArm scheduledArm : calendar.getScheduledArms()) {
            for (ScheduledEvent event : scheduledArm.getEvents()) {
                if (removedPlannedEvent.equals(event.getPlannedActivity())) {
                    event.unscheduleIfOutstanding("Removed in revision " + revision.getDisplayName());
                }
            }
        }
    }
}
