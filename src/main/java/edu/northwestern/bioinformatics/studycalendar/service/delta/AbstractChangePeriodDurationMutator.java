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
public abstract class AbstractChangePeriodDurationMutator extends AbstractPeriodPropertyChangeMutator {
    protected ScheduleService scheduleService;

    public AbstractChangePeriodDurationMutator(PropertyChange change, TemplateService templateService, ScheduleService scheduleService) {
        super(change, templateService);
        this.scheduleService = scheduleService;
    }

    protected abstract int durationChangeInDays();

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
