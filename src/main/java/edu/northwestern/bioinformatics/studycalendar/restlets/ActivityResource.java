package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * Resource representing a single {@link edu.northwestern.bioinformatics.studycalendar.domain.Activity}.
 *
 * @author Saurabh Agrawal
 */
public class ActivityResource extends AbstractRemovableDomainObjectResource<Activity> {

    private ActivityDao activityDao;
    private PlannedActivityDao plannedActivityDao;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
        setAuthorizedFor(Method.PUT, Role.STUDY_COORDINATOR);
    }

    @Override
    protected Activity loadRequestedObject(Request request) {
        String activitySourceName = UriTemplateParameters.ACTIVITY_SOURCE_NAME.extractFrom(request);
        String activityCode = UriTemplateParameters.ACTIVITY_CODE.extractFrom(request);

        return activityDao.getByCodeAndSourceName(activityCode, activitySourceName);
    }


    @Override
    public void store(Activity activity) {
        activityDao.save(activity);
        activity = getRequestedObject();

    }

    @Override
    public void remove(Activity activity) throws Exception {
        ///FIXME: Saurabh : move following logic to ActivityRepository
        List<PlannedActivity> plannedActivities = plannedActivityDao.getPlannedActivitiesForAcivity(activity.getId());
        if (plannedActivities == null || plannedActivities.size() == 0) {
            //delete only if activity is not used any where
            log.info("Deleting the activity"+activity.getId());
            activityDao.delete(activity);

        } else {
            String message = "can not delete the activity because activity is used by other planned activity-"
                    + plannedActivities.get(0).getDetails();
            log.error(message);
            throw new Exception(message);
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