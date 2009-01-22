package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Nataliya Shurupova
 * @author Rhett Sutphin
 */
@Transactional(propagation = Propagation.REQUIRED)
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

    /**
     * Searches all the activities in the system for those that match the given
     * criteria.  Returns a list of transient Source elements containing just the
     * matching activities.
     */
    public List<Source> getFilteredSources(String nameOrCodeSearch, ActivityType desiredType, Source desiredSource) {
        List<Activity> matches = activityDao.getActivitiesBySearchText(nameOrCodeSearch, desiredType, desiredSource);
        Map<String, Source> sources = new TreeMap<String, Source>(String.CASE_INSENSITIVE_ORDER);
        for (Activity match : matches) {
            if (match.getSource() == null) continue;
            String key = match.getSource().getNaturalKey();
            if (!sources.containsKey(key)) {
                Source newSource = match.getSource().transientClone();
                sources.put(newSource.getNaturalKey(), newSource);
            }
            sources.get(key).addActivity(match.transientClone());
        }
        for (Source source : sources.values()) {
            Collections.sort(source.getActivities());
        }
        return new ArrayList<Source>(sources.values());
    }

    ////// CONFIGURATION

    @Required
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    @Required
    public void setPlannedActivityDao(PlannedActivityDao plannedActivityDao) {
        this.plannedActivityDao = plannedActivityDao;
    }
}
