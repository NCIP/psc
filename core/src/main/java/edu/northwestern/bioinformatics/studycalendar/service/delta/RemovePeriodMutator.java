/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Revision;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;

/**
 * @author Rhett Sutphin
 */
public class RemovePeriodMutator extends RemoveMutator {
    private TemplateService templateService;

    public RemovePeriodMutator(Remove remove, PeriodDao dao, TemplateService templateService) {
        super(remove, dao);
        this.templateService = templateService;
    }

    @Override
    public boolean appliesToExistingSchedules() {
        return true;
    }

    @Override
    public void apply(ScheduledCalendar calendar) {
        // second cast is for silly javac/generics bug
        StudySegment studySegment = (StudySegment) (PlanTreeNode) change.getDelta().getNode();
        Period removedPeriod = (Period) findChild();
        Revision rev = change.getDelta().getRevision();
        for (ScheduledStudySegment scheduledStudySegment : calendar.getScheduledStudySegmentsFor(studySegment)) {
            log.debug("Applying removal of {} to {}", removedPeriod, scheduledStudySegment.getName());
            for (ScheduledActivity sa : scheduledStudySegment.getActivities()) {
                Period period = templateService.findParent(sa.getPlannedActivity());
                if (period.equals(removedPeriod)) {
                    sa.unscheduleIfOutstanding("Removed in revision " + rev.getDisplayName());
                }
            }
        }
    }
}
