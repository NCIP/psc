package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;

/**
 * @author Rhett Sutphin
 */
public class ChangePeriodDurationQuantityMutator extends AbstractPeriodPropertyChangeMutator {
    private ScheduleService scheduleService;
    private int durationQuanityChange;

    public ChangePeriodDurationQuantityMutator(
        PropertyChange change, TemplateService templateService, ScheduleService scheduleService
    ) {
        super(change, templateService);
        this.scheduleService = scheduleService;
        durationQuanityChange = Integer.parseInt(change.getNewValue()) - Integer.parseInt(change.getOldValue());
    }

    private int durationChangeInDays() {
        return getChangedPeriod().getDuration().getUnit().inDays() * durationQuanityChange;
    }

    @Override
    public void apply(ScheduledCalendar calendar) {
        for (ScheduledArm arm : getScheduledArmsToMutate(calendar)) {
            for (ScheduledEvent event : arm.getEvents()) {
                if (getChangedPeriod().equals(templateService.findParent(event.getPlannedEvent()))) {
                    scheduleService.reviseDate(event, durationChangeInDays() * event.getRepetitionNumber(),
                        change.getDelta().getRevision());
                }
            }
        }
    }
}
