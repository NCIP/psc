package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.Context;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Representation;
import org.restlet.data.*;
import org.springframework.beans.factory.annotation.Required;
import org.json.JSONObject;
import org.json.JSONException;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.*;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;

import java.util.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * @author Jalpa Patel
 */
public class BatchUpdatesResource extends AbstractPscResource{
    private ScheduledActivityDao scheduledActivityDao;
    private ScheduleService scheduleService;
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAuthorizedFor(Method.POST, Role.SUBJECT_COORDINATOR);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    @SuppressWarnings("unused")
    public void acceptRepresentation(Representation representation) throws ResourceException {
        if (representation.getMediaType().isCompatible(MediaType.APPLICATION_JSON)) {
            try {
                JSONObject entity = new JSONObject(representation.getText());
                JSONObject responseEntity = new JSONObject();
                JSONObject statusMessage;
                Iterator activityIds = entity.keys();
                while (activityIds.hasNext()) {
                    String activityId = activityIds.next().toString();
                    ScheduledActivity scheduledActivity = scheduledActivityDao.getByGridId(activityId);
                    if (scheduledActivity == null) {
                        statusMessage = createResponseStatusMessage(Status.CLIENT_ERROR_NOT_FOUND, "Activity does not exist with grid id " + activityId);
                        responseEntity.put(activityId, statusMessage);
                    } else {
                        JSONObject activityState = (JSONObject)(entity.get(activityId));
                        String state = activityState.get("state").toString();
                        String reason = activityState.get("reason").toString();
                        String dateString = activityState.get("date").toString();
                        try {
                            Date date = formatter.parse(dateString);
                            ScheduledActivityState scheduledActivityState = scheduleService.createScheduledActivityState(state, date, reason);
                            if (scheduledActivityState == null) {
                                statusMessage = createResponseStatusMessage(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown State: "+state);
                            } else {
                                scheduledActivity.changeState(scheduledActivityState);
                                String target = store(scheduledActivity);
                                statusMessage = createResponseStatusMessage(Status.SUCCESS_CREATED, "Activity State Updated");
                                statusMessage.put("Location",getRequest().getRootRef()+target);
                            }
                            responseEntity.put(activityId, statusMessage);
                        } catch (ParseException pe) {
                            responseEntity.put(activityId,
                               createResponseStatusMessage(Status.CLIENT_ERROR_BAD_REQUEST, "Could not parse date "+dateString));
                        }
                    }
                }
                getResponse().setEntity(new JsonRepresentation(responseEntity));
                getResponse().setStatus(Status.SUCCESS_MULTI_STATUS);
            } catch (JSONException e) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unparseable entity", e);
            } catch (IOException e) {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not read entity", e);
            }
        } else {
            throw new ResourceException(
                Status.CLIENT_ERROR_BAD_REQUEST, "Unsupported content type: " + representation.getMediaType());
        }
    }

    public String store(ScheduledActivity scheduledActivity){
        scheduledActivityDao.save(scheduledActivity);
        return String.format( "/studies/%s/schedules/%s/activities/%s",
            Reference.encode(scheduledActivity.getScheduledStudySegment().getScheduledCalendar().getAssignment().getStudySite().getStudy().getAssignedIdentifier()),
            Reference.encode(scheduledActivity.getScheduledStudySegment().getScheduledCalendar().getAssignment().getGridId()),
            Reference.encode(scheduledActivity.getGridId())
            );
    }

    public JSONObject createResponseStatusMessage(Status status, String message) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("Status", status.getCode());
        object.put("Message", message);
        return object;
    }

    @Required
    public void setScheduledActivityDao(ScheduledActivityDao scheduledActivityDao) {
        this.scheduledActivityDao = scheduledActivityDao;
    }

    @Required
    public void setScheduleService(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }
}
