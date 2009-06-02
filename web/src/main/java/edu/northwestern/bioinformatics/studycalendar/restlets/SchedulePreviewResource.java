package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;

import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.restlet.resource.ResourceException;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.restlet.Context;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.data.*;
import org.springframework.beans.factory.annotation.Required;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * @author Jalpa Patel
 */
public class SchedulePreviewResource extends AbstractDomainObjectResource<ScheduledCalendar> {
    private AmendedTemplateHelper helper;
    private SubjectService subjectService;
    private Study study;
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
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
                start_dates.add(i,formatter.parse(start_date));
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
        if (getRequestedObject() != null ) {
            if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
                JSONObject dayWiseActivities = new JSONObject();
                Map<Date,List<ScheduledActivity>> activities =  new TreeMap<Date,List<ScheduledActivity>>();
                for (ScheduledStudySegment scheduledStudySegment: scheduledCalendar.getScheduledStudySegments()) {
                    activities.putAll(scheduledStudySegment.getActivitiesByDate());
                }
                try {
                    for (Date date : activities.keySet()) {
                        dayWiseActivities.put(new String(formatter.format(date)),
                                ScheduleRepresentationHelper.createJSONScheduledActivities(null, activities.get(date)));
                    }
                } catch (JSONException e) {
	                throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
	            }
                return new JsonRepresentation(dayWiseActivities);
            } else {
                return super.represent(variant);
            }
        } else {
            return null;
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
