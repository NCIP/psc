package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;

/**
 * @author Rhett Sutphin
 */
public class ChangePeriodDurationUnitMutator extends AbstractChangePeriodDurationMutator {
    private Boolean dayToWeek;

    public ChangePeriodDurationUnitMutator(PropertyChange change, TemplateService templateService, ScheduleService scheduleService) {
        super(change, templateService, scheduleService);
        Duration.Unit old = Duration.Unit.valueOf(change.getOldValue());
        Duration.Unit nu  = Duration.Unit.valueOf(change.getNewValue());
        if (old == nu) {
            dayToWeek = null;
        } else if (old == Duration.Unit.day && nu == Duration.Unit.week) {
            dayToWeek = true;
        } else if (old == Duration.Unit.week && nu == Duration.Unit.day) {
            dayToWeek = false;
        } else {
            log.error("Invalid change {} in revision {}.  Skipping.", change, change.getDelta().getRevision());
            dayToWeek = null;
        }
    }

    @Override
    protected int durationChangeInDays() {
        if (dayToWeek == null) return 0;
        int durationChange = getChangedPeriod().getDuration().getQuantity() * 6;
        if (!dayToWeek) durationChange *= -1;
        return durationChange;
    }
}
