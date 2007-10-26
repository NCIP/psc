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
public class ChangePeriodDurationQuantityMutator extends AbstractChangePeriodDurationMutator {
    private int durationQuanityChange;

    public ChangePeriodDurationQuantityMutator(
        PropertyChange change, TemplateService templateService, ScheduleService scheduleService
    ) {
        super(change, templateService, scheduleService);
        durationQuanityChange = Integer.parseInt(change.getNewValue()) - Integer.parseInt(change.getOldValue());
    }

    @Override
    protected int durationChangeInDays() {
        return getChangedPeriod().getDuration().getUnit().inDays() * durationQuanityChange;
    }
}
