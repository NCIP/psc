package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.ScheduleRepresentationHelper;
import org.apache.commons.collections15.CollectionUtils;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.representation.Variant;
import org.springframework.beans.factory.annotation.Required;

import java.text.ParseException;
import java.util.*;
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
    private TemplateService templateService;

    @Override
    public void init(Context context, Request request, Response response) {
        helper.setRequest(request);
        super.init(context, request, response);
        addAuthorizationsFor(Method.GET, helper.getReadAuthorizations());

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
        List<ScheduledActivity> scheduledActivities = new ArrayList<ScheduledActivity>();
        for (ScheduledStudySegment scheduledStudySegment: scheduledCalendar.getScheduledStudySegments()) {
            scheduledActivities.addAll(scheduledStudySegment.getActivities());
        }

        SortedMap<Date,List<ScheduledActivity>> activities =  createActivitiesByDate(scheduledActivities);
        return new ScheduleRepresentationHelper(activities, scheduledCalendar.getScheduledStudySegments(), templateService);
    }

    public SortedMap<Date, List<ScheduledActivity>> createActivitiesByDate(List<ScheduledActivity> scheduledActivities) {
        SortedMap<Date, List<ScheduledActivity>> byDate = new TreeMap<Date, List<ScheduledActivity>>();
        Collections.sort(scheduledActivities);
        for (ScheduledActivity scheduledActivity : scheduledActivities) {
            Date key = scheduledActivity.getActualDate();
            if (!byDate.containsKey(key)) {
                byDate.put(key, new ArrayList<ScheduledActivity>());
            }
            byDate.get(key).add(scheduledActivity);
        }
        return byDate;
    }

    @Required
    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @Required
    public void setAmendedTemplateHelper(AmendedTemplateHelper amendedTemplateHelper) {
        this.helper = amendedTemplateHelper;
    }

    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }    
}
