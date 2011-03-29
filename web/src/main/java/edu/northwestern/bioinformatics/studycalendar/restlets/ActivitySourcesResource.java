package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.ActivitySourcesJsonRepresentation;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.service.ActivityService;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

import java.util.*;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST;

/**
 * @author Saurabh Agrawal
 */
public class ActivitySourcesResource extends AbstractCollectionResource<Source> {
    private ActivityService activityService;
    private SourceDao sourceDao;
    private ActivityTypeDao activityTypeDao;
    private ActivityDao activityDao;

    private StudyCalendarXmlCollectionSerializer<Source> xmlSerializer;

    @Override
    public void doInit() {
        super.doInit();
        setAllAuthorizedFor(Method.GET);
        
        addAuthorizationsFor(Method.GET,
                STUDY_CALENDAR_TEMPLATE_BUILDER,
                BUSINESS_ADMINISTRATOR,
                DATA_READER);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    }

    @Override
    @SuppressWarnings({ "ThrowInsideCatchBlockWhichIgnoresCaughtException" })
    public Collection<Source> getAllObjects() throws ResourceException {
        String q = QueryParameters.Q.extractFrom(getRequest());
        String typeName = QueryParameters.TYPE.extractFrom(getRequest());
        String typeId = QueryParameters.TYPE_ID.extractFrom(getRequest());
        if (q == null && typeId == null && typeName == null) {
            return sourceDao.getAll();
        }
        ActivityType type = null;
        if (typeName != null) {
            type = activityTypeDao.getByName(typeName);
            if (type == null) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown activity type: " + typeName);
            }
        } else if (typeId != null) {
            try {
                type = activityTypeDao.getById(Integer.parseInt(typeId));
            } catch (NumberFormatException nfe) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "type-id must be an integer");
            }
        }
        return activityService.getFilteredSources(q, type, null);
    }


    private List<Activity> extractActivities(String sourceName) {
        String selectAll = "selectAll";
        String select = "select";
        List<Activity> activities = new ArrayList<Activity>();
        if (sourceName == null || sourceName.equals(selectAll)) {
            activities = activityDao.getAll();
        } else if(!sourceName.equals(select)) {
            Source source = sourceDao.getByName(sourceName);
            activities = activityDao.getBySourceId(source.getId());
        }
        return activities;
    }

    @Override
    public Representation get(Variant variant) throws ResourceException {
        if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
            String sourceName = QueryParameters.SOURCE.extractFrom(getRequest());
            Source source = null;
            if (sourceName != null) {
                source = sourceDao.getByName(sourceName);
            } else {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, String.format("Provided ource is null"));
            }
            String action = QueryParameters.ACTION.extractFrom(getRequest());

            String name = QueryParameters.ACTIVITY_NAME.extractFrom(getRequest());
            String code = QueryParameters.ACTIVITY_CODE.extractFrom(getRequest());
            String type = QueryParameters.ACTIVITY_TYPE.extractFrom(getRequest());
            String description = QueryParameters.ACTIVITY_DESCRIPTION.extractFrom(getRequest());
            String activityId = QueryParameters.ACTIVITY_ID.extractFrom(getRequest());


            if (action != null) {
                if (action.toLowerCase().equals("new") || action.toLowerCase().equals("edit")) {
                    if (action.toLowerCase().equals("new")) {
                        verifyActivity(name, code, source);
                    }
                    Activity activity = createOrEditActivity(activityId, name, code, type, description, source);
                    activityDao.save(activity);
                } else if (action.toLowerCase().equals("delete")){
                    deleteActivity(activityId);
                } else {
                    throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, String.format("Action %s is not supported by resource", action));
                }
            }

            List<Activity> activities = new ArrayList<Activity>(extractActivities(sourceName));
            Integer limit = extractLimit();
            Integer offset = extractOffset(activities.size(), limit);
            List<Activity> toRender = buildRenderableActivities(activities, limit, offset);
            List<ActivityType> types = activityTypeDao.getAll();
            return new ActivitySourcesJsonRepresentation(toRender, activities.size(), offset, limit, types);
        } else {
            return super.get(variant);
        }
    }

    public void deleteActivity(String activityId) throws ResourceException {
        String message = "Can not delete the activity because activity is used by other planned activities";
        Activity activity = activityDao.getById(new Integer(activityId));
        boolean deleted= activityService.deleteActivity(activity);
        if (!deleted){
            log.error(message);
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, message);
        }
    }

    public Activity createOrEditActivity(String activityId, String name, String code, String type, String description, Source source){
        Activity activity;
        if (activityId == null) {
            activity = new Activity();
        } else {
            activity = activityDao.getById(new Integer(activityId));
        }
        activity.setName(name);
        activity.setDescription(description);
        ActivityType activityType = activityTypeDao.getById(new Integer(type));
        activity.setType(activityType);
        activity.setSource(source);
        activity.setCode(code);
        return activity;
    }

    public void verifyActivity(String name, String code, Source source) throws ResourceException {
        String message = null;
        if (name != null && name.length()>0) {
            if (activityDao.getByNameAndSourceName(name, source.getName()) != null) {
                message = "The activity name " + name + " already exists. Please enter a different activity name.";
            }
        } else {
            message = "The activity name is empty.";
        }
        if(code !=null && code.length()>0) {
            if(activityDao.getByCodeAndSourceName(code, source.getName()) != null) {
                message = "The activity code " + code + " already exists. Please enter a different activity code.";
            }
        } else {
            message = "The activity code is empty";
        }
        if (message != null) {
            log.error(message);
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, message);
        }
    }

    //need to move implementation to the abstract class of some sort, otherwise, the same as UserResource
    private Integer extractLimit() throws ResourceException {
        String limitS = QueryParameters.LIMIT.extractFrom(getRequest());
        if (limitS == null) return null;
        try {
            Integer limit = new Integer(limitS);
            if (limit < 1) {
                throw new ResourceException(
                    CLIENT_ERROR_BAD_REQUEST, "Limit must be a positive integer.");
            }
            return limit;
        } catch (NumberFormatException nfe) {
            throw new ResourceException(
                CLIENT_ERROR_BAD_REQUEST, "Limit must be a positive integer.");
        }
    }

    private Integer extractOffset(int total, Integer limit) throws ResourceException {
        String offsetS = QueryParameters.OFFSET.extractFrom(getRequest());
        if (offsetS == null) return 0;
        if (limit == null) {
            throw new ResourceException(
                CLIENT_ERROR_BAD_REQUEST, "Offset does not make sense without limit.");
        }
        try {
            Integer offset = new Integer(offsetS);
            if (offset < 0) {
                throw new ResourceException(
                    CLIENT_ERROR_BAD_REQUEST, "Offset must be a nonnegative integer.");
            }
            if (offset >= total && offset > 0) {
                throw new ResourceException(CLIENT_ERROR_BAD_REQUEST, String.format(
                    "Offset %d is too large.  There are %d result(s), so the max offset is %d.",
                    offset, total, Math.max(0, total - 1)));
            }
            return offset;
        } catch (NumberFormatException nfe) {
            throw new ResourceException(
                CLIENT_ERROR_BAD_REQUEST, "Offset must be a nonnegative integer.");
        }
    }


    private List<Activity> buildRenderableActivities(List<Activity> activities, Integer limit, Integer offset) {
        List<Activity> toWrap;
        if (limit == null) {
            toWrap = activities;
        } else {
            toWrap = activities.subList(offset, Math.min(offset + limit, activities.size()));
        }
        return toWrap;
    }

    @Override
    public StudyCalendarXmlCollectionSerializer<Source> getXmlSerializer() {
        return xmlSerializer;
    }

    ////// CONFIGURATION

    @Required
    public void setSourceDao(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }

    @Required
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    @Required
    public void setXmlSerializer(StudyCalendarXmlCollectionSerializer<Source> xmlSerializer) {
        this.xmlSerializer = xmlSerializer;
    }

    @Required
    public void setActivityService(ActivityService activityService) {
        this.activityService = activityService;
    }

    @Required
    public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
        this.activityTypeDao = activityTypeDao;
    }
}
