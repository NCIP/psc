package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.ActivityService;
import org.restlet.data.Method;
import org.restlet.Request;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.BUSINESS_ADMINISTRATOR;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.DATA_READER;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER;

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
    private ActivityService activityService;

    @Override
    public void doInit() {
        super.doInit();
        addAuthorizationsFor(Method.GET,
                STUDY_CALENDAR_TEMPLATE_BUILDER,
                BUSINESS_ADMINISTRATOR,
                DATA_READER);

        addAuthorizationsFor(Method.PUT,
                BUSINESS_ADMINISTRATOR);

        addAuthorizationsFor(Method.DELETE,
                BUSINESS_ADMINISTRATOR);
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
    public Activity store(Activity activity) throws ResourceException {
        if (getRequestedObject() == null) {
            activityService.saveActivity(activity);
        } else {
            //Search the existing activity and edit the properties
            Activity existingActivity = activityDao.getById(getRequestedObject().getId());
            existingActivity.setCode(activity.getCode());
            existingActivity.setName(activity.getName());
            existingActivity.setDescription(activity.getDescription());
            activityService.saveActivity(existingActivity);
        }
        return activity;
    }

    @Override
    public void remove(Activity activity) {
        //delete only if activity is not used any where
        log.debug("Deleting activity {}", activity);
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

    @Required
    public void setActivityService(ActivityService activityService) {
        this.activityService = activityService;
    }
}