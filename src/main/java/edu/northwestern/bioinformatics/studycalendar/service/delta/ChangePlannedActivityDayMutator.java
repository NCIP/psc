package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledEventDao;

/**
 * @author Rhett Sutphin
 */
public class ChangePlannedActivityDayMutator extends AbstractChangePlannedActivityMutator {
    private ScheduleService scheduleService;
    private int amount;

    public ChangePlannedActivityDayMutator(
        PropertyChange change, ScheduledEventDao scheduledEventDao, ScheduleService scheduleService
    ) {
        super(change, scheduledEventDao);
        this.scheduleService = scheduleService;
        amount = Integer.parseInt(change.getNewValue()) - Integer.parseInt(change.getOldValue());
    }

    @Override
    public void apply(ScheduledCalendar calendar) {
        for (ScheduledEvent event : findEventsToMutate(calendar)) {
            scheduleService.reviseDate(event, amount, change.getDelta().getRevision());
        }
    }
}
