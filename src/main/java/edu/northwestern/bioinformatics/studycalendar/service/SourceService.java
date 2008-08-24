package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Saurabh Agrawal
 */
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class SourceService {
    private PlannedActivityDao plannedActivityDao;
    private SourceDao sourceDao;


    /**
     * <p>  Updates the source. Following is the logic to update a source
     * <li>
     * Add any activities that do not already exist.   </li>
     * <li> Update any activities that already exist and still exist in the new representation.  Activities should be matched by their natural key -- i.e., the code. </li>
     * <li>Remove any activities that do not exist in the new representation, so long as they are not used any any existing templates.     </li>
     * </p>
     *
     * @param source       source
     * @param targetSource targetSource
     */
    @Transient
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void updateSource( final Source source,final Source targetSource) {
        BeanUtils.copyProperties(source, targetSource, new String[]{"activities", "id"});

        //delete  or update the activity

        removeAndUpdateActivities(targetSource.getActivities(), source.getActivities());

        //add new activities
        targetSource.addNewActivities(source.getActivities());
        sourceDao.save(source);


    }

    /**
     * <p>Updates and remove activities from source.
     * <li> Update any activities that already exist and still exist in the new representation.  Activities should be matched by their natural key -- i.e., the code. </li>
     * <li>Remove any activities that do not exist in the new representation, so long as they are not used any any existing templates.     </li>
     * </p>
     *
     * @param targetActivities existing activities
     * @param activities
     */
    @Transient
    private void removeAndUpdateActivities(List<Activity> targetActivities, final List<Activity> activities) {
        //delete  or update the activity
        List<Activity> activitiesToRemove = new ArrayList<Activity>();

        for (Activity existingActivity : targetActivities) {

            Activity activity = existingActivity.findActivityInCollectionWhichHasSameCode(activities);

            if (activity != null) {
                existingActivity.updateActivity(activity);
            } else {
                //remove this activity only if its not used any where
                List<PlannedActivity> plannedActivities = plannedActivityDao.getPlannedActivitiesForActivity(existingActivity.getId());
                if (plannedActivities == null || plannedActivities.size() == 0) {
                    activitiesToRemove.add(existingActivity);

                }
            }
        }
        targetActivities.removeAll(activitiesToRemove);
    }

    @Required
    public void setPlannedActivityDao(final PlannedActivityDao plannedActivityDao) {
        this.plannedActivityDao = plannedActivityDao;
    }

    @Required
    public void setSourceDao(final SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }
}
