package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledEventDao;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;

/**
 * @author Rhett Sutphin
 */
public class ChangePlannedEventDayMutator extends AbstractChangePlannedEventMutator {
    private ScheduleService scheduleService;
    private int amount;

    public ChangePlannedEventDayMutator(
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
