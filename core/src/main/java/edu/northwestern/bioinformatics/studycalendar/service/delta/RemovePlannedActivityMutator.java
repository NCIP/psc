/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Revision;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;

/**
 * @author Rhett Sutphin
 */
public class RemovePlannedActivityMutator extends RemoveMutator {
    public RemovePlannedActivityMutator(Remove remove, PlannedActivityDao dao) {
        super(remove, dao);
    }

    @Override
    public boolean appliesToExistingSchedules() {
        return true;
    }

    @Override
    public void apply(ScheduledCalendar calendar) {
        PlannedActivity removedPlannedActivity = (PlannedActivity) findChild();
        Revision revision = change.getDelta().getRevision();
        for (ScheduledStudySegment scheduledStudySegment : calendar.getScheduledStudySegments()) {
            for (ScheduledActivity event : scheduledStudySegment.getActivities()) {
                if (removedPlannedActivity.equals(event.getPlannedActivity())) {
                    event.unscheduleIfOutstanding("Removed in revision " + revision.getDisplayName());
                }
            }
        }
    }
}

