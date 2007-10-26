package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.ParticipantService;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;

import java.util.Collection;

import org.apache.commons.logging.Log;

/**
 * @author Rhett Sutphin
 */
public class ChangePeriodRepetitionsMutator extends AbstractPeriodPropertyChangeMutator {
    private ParticipantService participantService;

    private int oldRepetitionCount;
    private int newRepetitionCount;

    public ChangePeriodRepetitionsMutator(PropertyChange change, TemplateService templateService, ParticipantService participantService) {
        super(change, templateService);
        this.participantService = participantService;

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
            for (ScheduledEvent event : scheduledArm.getEvents()) {
                if (event.getRepetitionNumber() >= newRepetitionCount && getChangedPeriod().equals(templateService.findParent(event.getPlannedEvent()))) {
                    log.debug("Possibly canceling event from rep {}", event.getRepetitionNumber());
                    event.unscheduleIfOutstanding(createDecreaseMessage(event));
                }
            }
        }
    }

    private String createDecreaseMessage(ScheduledEvent event) {
        return new StringBuilder()
            .append("Repetition ").append(event.getRepetitionNumber() + 1)
            .append(" removed in revision ")
            .append(change.getDelta().getRevision().getDisplayName())
            .toString();
    }

    private void increase(Collection<ScheduledArm> arms) {
        for (int r = oldRepetitionCount ; r < newRepetitionCount ; r++) {
            for (ScheduledArm scheduledArm : arms) {
                participantService.schedulePeriod(getChangedPeriod(),
                    // TODO: eliminate this cast
                    (Amendment) change.getDelta().getRevision(), scheduledArm, r);
            }
        }
    }
}
