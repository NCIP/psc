package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * Resource representing a single {@link edu.northwestern.bioinformatics.studycalendar.domain.Activity}.
 *
 * @author Saurabh Agrawal
 */
public class ActivityResource extends AbstractRemovableStorableDomainObjectResource<Activity> {

    private ActivityDao activityDao;
    private PlannedActivityDao plannedActivityDao;
    private Source source;
    private SourceDao sourceDao;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
        setAuthorizedFor(Method.PUT, Role.STUDY_COORDINATOR);
    }

    @Override
    protected Activity loadRequestedObject(Request request) throws ResourceException {
        String activitySourceName = UriTemplateParameters.ACTIVITY_SOURCE_NAME.extractFrom(request);
        String activityCode = UriTemplateParameters.ACTIVITY_CODE.extractFrom(request);
        if (activitySourceName == null) {
           throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,"No Source in the request");
        }
        source = sourceDao.getByName(activitySourceName);
        if (source == null) {
           throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,"Unknown Source " + activitySourceName);
        }
        else {
           return activityDao.getByCodeAndSourceName(activityCode, activitySourceName);
        }
    }

    @Override
    public void store(Activity activity) throws ResourceException {
        if (getRequestedObject() == null) {
            activityDao.save(activity);
        } else {
            //Search the existing activity and edit the properties
            Activity existingActivity = activityDao.getById(getRequestedObject().getId());
            existingActivity.setCode(activity.getCode());
            existingActivity.setName(activity.getName());
            existingActivity.setDescription(activity.getDescription());
            existingActivity.setType(activity.getType());
            activityDao.save(existingActivity);

        }

    }


    @Override
    public void remove(Activity activity) {
        //delete only if activity is not used any where
        log.info("Deleting the activity" + activity.getId());
        activityDao.delete(activity);

    }

    @Override
    public void verifyRemovable(final Activity activity) throws ResourceException {
        List<PlannedActivity> plannedActivities = plannedActivityDao.getPlannedActivitiesForActivity(activity.getId());
        if (plannedActivities != null && plannedActivities.size() > 0) {

            String message = "Can not delete the activity" + UriTemplateParameters.ACTIVITY_CODE.extractFrom(getRequest()) +
                    " because activity is used by other planned activity " + plannedActivities.get(0).getDetails();
            log.error(message);

            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    message);

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
    @Required
    public void setSourceDao(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }
}