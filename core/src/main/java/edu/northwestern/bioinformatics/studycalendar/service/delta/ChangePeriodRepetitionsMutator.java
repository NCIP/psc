/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
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
        Collection<ScheduledStudySegment> studySegments = getScheduledStudySegmentsToMutate(calendar);

        if (newRepetitionCount < oldRepetitionCount) decrease(studySegments);
        else increase(studySegments);
    }

    private void decrease(Collection<ScheduledStudySegment> studySegments) {
        for (ScheduledStudySegment scheduledStudySegment : studySegments) {
            for (ScheduledActivity event : scheduledStudySegment.getActivities()) {
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

    private String createIncreaseMessage(int repetitionNumber) {
        return new StringBuilder()
            .append("Repetition ").append(repetitionNumber + 1)
            .append(" added in revision ")
            .append(change.getDelta().getRevision().getDisplayName())
            .toString();
    }

    private void increase(Collection<ScheduledStudySegment> studySegments) {
        for (int r = oldRepetitionCount ; r < newRepetitionCount ; r++) {
            for (ScheduledStudySegment scheduledStudySegment : studySegments) {
                subjectService.schedulePeriod(getChangedPeriod(),
                    // TODO: eliminate this cast
                    (Amendment) change.getDelta().getRevision(), createIncreaseMessage(r),
                    scheduledStudySegment, r);
            }
        }
    }
}
