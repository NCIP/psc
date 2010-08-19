package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Rhett Sutphin
 */
@Deprecated
public class SubjectCoordinatorSchedulesResource extends AbstractPscResource {
    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);

        addAuthorizationsFor(Method.GET,
            STUDY_SUBJECT_CALENDAR_MANAGER,
            STUDY_TEAM_ADMINISTRATOR,
            DATA_READER);
    }

    @Override
    public void handleGet() {
        getResponse().redirectPermanent(
            String.format("%s/api/v1/users/%s/managed-schedules", 
                getApplicationBaseUrl(), UriTemplateParameters.USERNAME.extractFrom(getRequest())));
    }
}
