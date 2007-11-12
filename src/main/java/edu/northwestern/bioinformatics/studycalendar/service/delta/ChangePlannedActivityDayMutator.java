package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;

/**
 * @author Rhett Sutphin
 */
public class ChangePlannedActivityDayMutator extends AbstractChangePlannedActivityMutator {
    private ScheduleService scheduleService;
    private int amount;

    public ChangePlannedActivityDayMutator(
        PropertyChange change, ScheduledActivityDao scheduledActivityDao, ScheduleService scheduleService
    ) {
        super(change, scheduledActivityDao);
        this.scheduleService = scheduleService;
        amount = Integer.parseInt(change.getNewValue()) - Integer.parseInt(change.getOldValue());
    }

    @Override
    public void apply(ScheduledCalendar calendar) {
        for (ScheduledActivity event : findEventsToMutate(calendar)) {
            scheduleService.reviseDate(event, amount, change.getDelta().getRevision());
        }
    }
}
