package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;

/**
 * @author Rhett Sutphin
 */
public class ChangePeriodStartDayMutator extends SimplePropertyChangeMutator {
    private TemplateService templateService;
    private ScheduleService scheduleService;
    private int shiftAmount;

    public ChangePeriodStartDayMutator(
        PropertyChange change, TemplateService templateService, ScheduleService scheduleService
    ) {
        super(change);
        this.templateService = templateService;
        this.scheduleService = scheduleService;
        shiftAmount = Integer.parseInt(change.getNewValue()) - Integer.parseInt(change.getOldValue());
    }

    @Override
    public boolean appliesToExistingSchedules() { return true; }

    @Override
    public void apply(ScheduledCalendar calendar) {
        // second cast is for javac bug
        Period period = (Period) (PlanTreeNode) change.getDelta().getNode();
        for (ScheduledArm scheduledArm : calendar.getScheduledArms()) {
            for (ScheduledEvent event : scheduledArm.getEvents()) {
                if (period.equals(templateService.findParent(event.getPlannedEvent()))) {
                    scheduleService.reviseDate(event, shiftAmount, change.getDelta().getRevision());
                }
            }
        }
    }
}
