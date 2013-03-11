/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Rhett Sutphin
 */
abstract class AbstractChangePlannedActivityMutator extends SimplePropertyChangeMutator {

    public AbstractChangePlannedActivityMutator(PropertyChange change) {
        super(change);
    }

    @Override public boolean appliesToExistingSchedules() { return true; }

    @Override
    public abstract void apply(ScheduledCalendar calendar);

    public Collection<ScheduledActivity> findEventsToMutate(ScheduledCalendar calendar) {
        PlannedActivity plannedActivity = (PlannedActivity) (PlanTreeNode) change.getDelta().getNode();
        Collection<ScheduledActivity> scheduledActivities = new ArrayList<ScheduledActivity>();
        Collection<ScheduledStudySegment> scheduledStudySegments = calendar.getScheduledStudySegmentsFor(plannedActivity.getPeriod().getStudySegment());
        for (ScheduledStudySegment scheduledStudySegment : scheduledStudySegments) {
            for (ScheduledActivity sa : scheduledStudySegment.getActivities()) {
                if (sa.getCurrentState().getMode().isOutstanding()) scheduledActivities.add(sa);
            }
        }
        return scheduledActivities;
    }
}
