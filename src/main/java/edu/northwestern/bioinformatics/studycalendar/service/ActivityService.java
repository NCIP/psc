package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Nataliya Shurupova
 * @author Rhett Sutphin
 */
public class ActivityService {
    private ActivityDao activityDao;
    private PlannedActivityDao plannedActivityDao;

    /**
     * Deletes the activity, if it has no reference by planned activity.
     * @return boolean, corresponding to the successful or unsuccessful deletion
     * @param activity - activity we want to remove
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public boolean deleteActivity(Activity activity) {
        Integer id = activity.getId();
        List<PlannedActivity> plannedActivities = plannedActivityDao.getPlannedActivitiesForActivity(id);
        if (plannedActivities == null || plannedActivities.size() == 0) {
            activityDao.delete(activity);
            return true;
        } else {
            return false;
        }
    }

    @Required
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    @Required
    public void setPlannedActivityDao(PlannedActivityDao plannedActivityDao) {
        this.plannedActivityDao = plannedActivityDao;
    }
}
