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
public class BatchUpdatesResource extends AbstractStorableCollectionResource<ScheduledActivity>{
    private ScheduledActivityDao scheduledActivityDao;
    private ScheduleService scheduleService;
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    private List<ScheduledActivity> scheduledActivities = new LinkedList<ScheduledActivity>();
    private StudyCalendarXmlCollectionSerializer<ScheduledActivity> xmlSerializer;


    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAuthorizedFor(Method.POST, Role.SUBJECT_COORDINATOR);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    }

    @Override
    public Collection<ScheduledActivity> getAllObjects() throws ResourceException {
        String updateList = UriTemplateParameters.UPDATE_LIST.extractFrom(getRequest());
        if (updateList == null) {
           throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,"No Activities in request");
        }
        List<String> gridIds = new LinkedList(Arrays.asList(updateList.split(";")));
        ScheduledActivity scheduledActivity;
        for (String gridId : gridIds) {
            scheduledActivity = scheduledActivityDao.getByGridId(gridId);
            if (scheduledActivity == null) {
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Activity does not exist with grid id " +gridId);
            }
            scheduledActivities.add(scheduledActivity);
        }
        return scheduledActivities;
    }

    @Override
    @SuppressWarnings("unused")
    public void acceptRepresentation(Representation representation) throws ResourceException {
        if (representation.getMediaType().isCompatible(MediaType.APPLICATION_JSON)) {
            try {
                JSONObject entity = new JSONObject(representation.getText());
                JSONObject responseEntity = new JSONObject();
                JSONObject statusMessage;
                List<ScheduledActivity> activities =  new ArrayList<ScheduledActivity>(getAllObjects());
                for (ScheduledActivity scheduledActivity : activities) {
                   JSONObject activityState = (JSONObject)(entity.get(scheduledActivity.getGridId()));
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
                       responseEntity.put(scheduledActivity.getGridId(), statusMessage);
                   } catch (ParseException pe) {
                       responseEntity.put(scheduledActivity.getGridId(),
                               createResponseStatusMessage(Status.CLIENT_ERROR_BAD_REQUEST, "Could not parse date "+dateString));
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

    @Override
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
    @Override
    public StudyCalendarXmlCollectionSerializer<ScheduledActivity> getXmlSerializer() {
        return xmlSerializer;
    }

    public void setXmlSerializer(StudyCalendarXmlCollectionSerializer<ScheduledActivity> xmlSerializer) {
        this.xmlSerializer = xmlSerializer;
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
