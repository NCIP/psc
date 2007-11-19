package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;

import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
public class ChangePeriodRepetitionsMutator extends AbstractPeriodPropertyChangeMutator {
    private SubjectService subjectService;

    private int oldRepetitionCount;
    private int newRepetitionCount;

    public ChangePeriodRepetitionsMutator(PropertyChange change, TemplateService templateService, SubjectService subjectService) {
        super(change, templateService);
        this.subjectService = subjectService;

        oldRepetitionCount = Integer.parseInt(change.getOldValue());
        newRepetitionCount = Integer.parseInt(change.getNewValue());
    }

    @Override public boolean appliesToExistingSchedules() { return true; }

    @Override
    public void apply(ScheduledCalendar calendar) {
        Collection<ScheduledArm> arms = getScheduledArmsToMutate(calendar);

        if (newRepetitionCount < oldRepetitionCount) decrease(arms);
        else increase(arms);
    }

    private void decrease(Collection<ScheduledArm> arms) {
        for (ScheduledArm scheduledArm : arms) {
            for (ScheduledActivity event : scheduledArm.getEvents()) {
                if (event.getRepetitionNumber() >= newRepetitionCount && getChangedPeriod().equals(templateService.findParent(event.getPlannedActivity()))) {
                    log.debug("Possibly canceling event from rep {}", event.getRepetitionNumber());
                    event.unscheduleIfOutstanding(createDecreaseMessage(event));
                }
            }
        }
    }

    private String createDecreaseMessage(ScheduledActivity event) {
        return new StringBuilder()
            .append("Repetition ").append(event.getRepetitionNumber() + 1)
            .append(" removed in revision ")
            .append(change.getDelta().getRevision().getDisplayName())
            .toString();
    }

    private void increase(Collection<ScheduledArm> arms) {
        for (int r = oldRepetitionCount ; r < newRepetitionCount ; r++) {
            for (ScheduledArm scheduledArm : arms) {
                subjectService.schedulePeriod(getChangedPeriod(),
                    // TODO: eliminate this cast
                    (Amendment) change.getDelta().getRevision(), scheduledArm, r);
            }
        }
    }
}
