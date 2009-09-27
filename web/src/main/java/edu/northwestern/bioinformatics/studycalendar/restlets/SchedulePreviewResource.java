package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.ScheduleRepresentationHelper;
import org.apache.commons.collections15.CollectionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.springframework.beans.factory.annotation.Required;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jalpa Patel
 */
public class SchedulePreviewResource extends AbstractDomainObjectResource<ScheduledCalendar> {
    private static final Pattern START_DATE_PARAM_PATTERN = Pattern.compile("start_date\\[(.*)\\]");
    private static final Pattern SEGMENT_PARAM_PATTERN = Pattern.compile("segment\\[(.*)\\]");

    private AmendedTemplateHelper helper;
    private SubjectService subjectService;
    private ScheduledCalendar scheduledCalendar = new ScheduledCalendar();

    @Override
    public void init(Context context, Request request, Response response) {
        helper.setRequest(request);
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
     }

    @Override
    protected ScheduledCalendar loadRequestedObject(Request request) throws ResourceException {
        Study study;
        try {
            study = helper.getAmendedTemplate();
        } catch (AmendedTemplateHelper.NotFound notFound) {
             throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, notFound.getMessage());
        }
        Form query  = request.getResourceRef().getQueryAsForm();

        Map<String, Date> startDates = buildStartDateMap(query);
        Map<String, StudySegment> segments =  buildSegmentMap(study, query);

        if (!startDates.keySet().equals(segments.keySet())) {
            Collection<String> startDateOnly = CollectionUtils.subtract(startDates.keySet(), segments.keySet());
            if (startDateOnly.size() > 0) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "The following start_date(s) do not have matching segment(s): " + startDateOnly);
            }
            Collection<String> segmentOnly = CollectionUtils.subtract(segments.keySet(), startDates.keySet());
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                "The following segment(s) do not have matching start_date(s): " + segmentOnly);
        } else if (startDates.size() == 0) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                "At least one segment/start_date pair is required");
        }

        for (String key : segments.keySet()) {
            subjectService.scheduleStudySegmentPreview(scheduledCalendar, segments.get(key), startDates.get(key));
        }
        return scheduledCalendar;
    }

    private Map<String, Date> buildStartDateMap(Form params) throws ResourceException {
        Map<String, Date> map = new HashMap<String, Date>();
        for (Parameter param : params) {
            Matcher match = START_DATE_PARAM_PATTERN.matcher(param.getName());
            if (match.matches()) {
                try {
                    map.put(match.group(1), getApiDateFormat().parse(param.getValue()));
                } catch (ParseException e) {
                    throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                        String.format("Invalid date: %s=%s.  The date must be formatted as yyyy-mm-dd.",
                            param.getName(), param.getValue()));
                }
            }
        }
        return map;
    }

    private Map<String, StudySegment> buildSegmentMap(Study study, Form params) throws ResourceException {
        Map<String, StudySegment> map = new HashMap<String, StudySegment>();
        for (Parameter param : params) {
            Matcher match = SEGMENT_PARAM_PATTERN.matcher(param.getName());
            if (match.matches()) {
                map.put(match.group(1), findSegment(study, param.getValue()));
            }
        }
        return map;
    }

    private StudySegment findSegment(Study study, String segmentGridId) throws ResourceException {
        for (Epoch epoch: study.getPlannedCalendar().getEpochs())  {
            for (StudySegment studySegment : epoch.getStudySegments()) {
                if (studySegment.getGridId().equals(segmentGridId)) {
                   return studySegment;
                }
            }
        }
        throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
            "No study segment with identifier " + segmentGridId + " in the study");
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if (getRequestedObject() != null) {
            if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
                return createJSONRepresentation(getRequestedObject());
            } else {
                return super.represent(variant);
            }
        } else {
            return null;
        }
    }

    private Representation createJSONRepresentation(ScheduledCalendar scheduledCalendar) throws ResourceException {
        try {
            JSONObject jsonData = new JSONObject();
            JSONObject dayWiseActivities = new JSONObject();
            JSONArray studySegments = new JSONArray();
            SortedMap<Date,List<ScheduledActivity>> activities =  new TreeMap<Date,List<ScheduledActivity>>();
            for (ScheduledStudySegment scheduledStudySegment: scheduledCalendar.getScheduledStudySegments()) {
                activities.putAll(scheduledStudySegment.getActivitiesByDate());
                studySegments.put(ScheduleRepresentationHelper.createJSONStudySegment(scheduledStudySegment));
            }
            for (Date date : activities.keySet()) {
                 dayWiseActivities.put(getApiDateFormat().format(date),
                                ScheduleRepresentationHelper.createJSONScheduledActivities(null, activities.get(date)));
            }
            jsonData.put("days", dayWiseActivities);
            jsonData.put("study_segments", studySegments);
            return new JsonRepresentation(jsonData);
        } catch (JSONException e) {
            // TODO: this is major bad: swallowing exception
	        throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
	    }
    }

    @Required
    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @Required
    public void setAmendedTemplateHelper(AmendedTemplateHelper amendedTemplateHelper) {
        this.helper = amendedTemplateHelper;
    }

}
