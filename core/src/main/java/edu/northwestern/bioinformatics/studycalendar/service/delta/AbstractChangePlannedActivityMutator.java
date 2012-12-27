/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
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
    protected ScheduledActivityDao scheduledActivityDao;

    public AbstractChangePlannedActivityMutator(PropertyChange change, ScheduledActivityDao scheduledActivityDao) {
        super(change);
        this.scheduledActivityDao = scheduledActivityDao;
    }

    @Override public boolean appliesToExistingSchedules() { return true; }

    @Override
    public abstract void apply(ScheduledCalendar calendar);

    public Collection<ScheduledActivity> findEventsToMutate(ScheduledCalendar calendar) {
        Collection<ScheduledActivity> scheduledActivities
            = new ArrayList<ScheduledActivity>(scheduledActivityDao.getEventsFromPlannedActivity(
                (PlannedActivity) (PlanTreeNode) change.getDelta().getNode(), calendar));
        for (Iterator<ScheduledActivity> it = scheduledActivities.iterator(); it.hasNext();) {
            ScheduledActivity sa = it.next();
            if (!sa.getCurrentState().getMode().isOutstanding()) it.remove();
        }
        return scheduledActivities;
    }
}
