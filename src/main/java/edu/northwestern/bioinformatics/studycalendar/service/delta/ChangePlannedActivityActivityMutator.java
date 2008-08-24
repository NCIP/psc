package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;

/**
 * @author Rhett Sutphin
 */
public class ChangePlannedActivityActivityMutator extends ChangePlannedActivitySimplePropertyMutator {
    private ActivityDao activityDao;

    public ChangePlannedActivityActivityMutator(
        PropertyChange change, ScheduledActivityDao scheduledActivityDao, ActivityDao activityDao
    ) {
        super(change, scheduledActivityDao);
        this.activityDao = activityDao;
    }

    protected Object getAssignableNewValue() {
        return activityDao.getByUniqueKey(change.getNewValue());
    }
}
