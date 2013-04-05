/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
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
        Collection<ScheduledActivity> allScheduledActivities = calendar.getScheduledActivitiesFor(plannedActivity);
        Collection<ScheduledActivity> scheduledActivities = new ArrayList<ScheduledActivity>();
        for (ScheduledActivity sa : allScheduledActivities) {
            if (sa.getCurrentState().getMode().isOutstanding()) scheduledActivities.add(sa);
        }
        return scheduledActivities;
    }
}
