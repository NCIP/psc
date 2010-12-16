package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserStudySubjectAssignmentRelationship;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.representation.Variant;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_SUBJECT_CALENDAR_MANAGER;

/**
 * @author Jalpa Patel
 */
public class BatchUpdatesResource extends AbstractPscResource {
    private ScheduledActivityDao scheduledActivityDao;
    private ScheduleService scheduleService;

    private Map<Integer, UserStudySubjectAssignmentRelationship> ussars =
        new HashMap<Integer, UserStudySubjectAssignmentRelationship>();

    @Override
    public void doInit() {
        super.doInit();

        addAuthorizationsFor(Method.POST,
                STUDY_SUBJECT_CALENDAR_MANAGER);
        
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    }

    @Override
    @SuppressWarnings("unused")
    public Representation post(Representation representation, Variant variant) throws ResourceException {
        PscUser user = getCurrentUser();
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
                        StudySubjectAssignment scheduledStudyAssignment = scheduledActivity.getScheduledStudySegment().getScheduledCalendar().getAssignment();
                        UserStudySubjectAssignmentRelationship ussar = findUserStudySubjectAssignmentRelationship(scheduledStudyAssignment);

                        if (ussar.getCanUpdateSchedule()) {
                            JSONObject activityState = (JSONObject)(entity.get(activityId));
                            String state = activityState.get("state").toString();
                            String reason = activityState.get("reason").toString();
                            String dateString = activityState.get("date").toString();
                            try {
                                Date date = getApiDateFormat().parse(dateString);
                                ScheduledActivityMode newMode = ScheduledActivityMode.getByName(state);
                                if (newMode == null) {
                                    statusMessage = createResponseStatusMessage(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown State: "+state);
                                } else {
                                    scheduledActivity.changeState(newMode.createStateInstance(date, reason));
                                    String target = store(scheduledActivity);
                                    statusMessage = createResponseStatusMessage(Status.SUCCESS_CREATED, "Activity State Updated");
                                    statusMessage.put("Location",getRequest().getRootRef()+target);
                                }
                                responseEntity.put(activityId, statusMessage);
                            } catch (ParseException pe) {
                                responseEntity.put(activityId,
                                   createResponseStatusMessage(Status.CLIENT_ERROR_BAD_REQUEST, "Could not parse date "+dateString));
                            }

                        } else {
                            responseEntity.put(activityId,
                                   createResponseStatusMessage(Status.CLIENT_ERROR_FORBIDDEN, "Does not have proper credentials to modify the activity "+ scheduledActivity.getGridId()));
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

        return null;
    }

    private UserStudySubjectAssignmentRelationship findUserStudySubjectAssignmentRelationship(StudySubjectAssignment scheduledStudyAssignment) {
        if (!ussars.containsKey(scheduledStudyAssignment.getId())) {
            ussars.put(scheduledStudyAssignment.getId(), new UserStudySubjectAssignmentRelationship(getCurrentUser(), scheduledStudyAssignment));
        }
        return ussars.get(scheduledStudyAssignment.getId());
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
