package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.beans.factory.annotation.Required;

/**
 * Resource representing a single {@link edu.northwestern.bioinformatics.studycalendar.domain.Activity}.
 *
 * @author Saurabh Agrawal
 */
public class ActivityResource extends AbstractStorableDomainObjectResource<Activity> {

    private ActivityDao activityDao;

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
                
        return activityDao.getByCodeAndSourceName(activityCode,activitySourceName);
    }

    @Override
    public void store(Activity activity) {

        if (getRequestedObject() == null) {
            activityDao.save(activity);
        } else {
            ///FIXME:Saurabh...implement the logic for updating the activity
            activity = getRequestedObject();
        }
    }

   
    @Required
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }
}