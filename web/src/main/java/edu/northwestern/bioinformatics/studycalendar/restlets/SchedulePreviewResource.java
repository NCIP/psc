package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.springframework.beans.factory.annotation.Required;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Jalpa Patel
 */
public class SchedulePreviewResource extends AbstractDomainObjectResource<ScheduledCalendar> {
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
        int indexValue = getIndexForSegmentDatePairsFromRequest(query);

        List<StudySegment> segments =  new ArrayList<StudySegment>();
        List<Date> start_dates = new ArrayList<Date>();

        for (int i=0;i<indexValue;i++) {
            String segment = query.getFirstValue("segment["+ i +"]");
            String start_date = query.getFirstValue("start_date["+ i +"]");
            for (Epoch epoch: study.getPlannedCalendar().getEpochs())  {
                for (StudySegment studySegment : epoch.getStudySegments()) {
                    if (studySegment.getGridId().matches(segment)) {
                       segments.add(i, studySegment);
                    }
                }
            }
            try {
                start_dates.add(i, getApiDateFormat().parse(start_date));
            }  catch (ParseException pe) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not parse date " + start_date);
            }
        }
        for (int i=0;i<indexValue;i++) {
            subjectService.scheduleStudySegmentPreview(scheduledCalendar, segments.get(i), start_dates.get(i));
        }
        return scheduledCalendar;
    }

    private int getIndexForSegmentDatePairsFromRequest(Form query) throws ResourceException {
        Set<String> names= query.getNames();

        List<String> segments = new ArrayList<String>();
        List<String> dates = new ArrayList<String>();
        for (String name : names) {
            if (name.contains("segment")) {
               segments.add(name);
            }
            if (name.contains("start_date")) {
               dates.add(name);
            }
        }
        if (dates.size() == segments.size()) {
            return dates.size();
        } else {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,"There must be pair for segment & date.");
        }
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
