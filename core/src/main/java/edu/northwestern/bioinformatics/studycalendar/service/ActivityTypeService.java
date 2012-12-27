/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.beans.factory.annotation.Required;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;

/**
 * @author Nataliya Shurupova
 */
@Transactional(propagation = Propagation.REQUIRED)
public class ActivityTypeService {
    private ActivityTypeDao activityTypeDao;

    /**
     * Deletes the activity type, if it has no reference by activity.
     * @return boolean, corresponding to the successful or unsuccessful deletion
     * @param activityType - activityType we want to remove
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public boolean deleteActivityType(ActivityType activityType) {
        activityTypeDao.delete(activityType);
        return true;
    }

    ////// CONFIGURATION

    @Required
    public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
        this.activityTypeDao = activityTypeDao;
    }
}
