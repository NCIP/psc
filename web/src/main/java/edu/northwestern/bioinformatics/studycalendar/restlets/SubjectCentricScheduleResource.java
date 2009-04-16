package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.service.AuthorizationService;
import edu.northwestern.bioinformatics.studycalendar.web.subject.SubjectCentricSchedule;
import edu.northwestern.bioinformatics.studycalendar.web.subject.ScheduleDay;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudySubjectAssignmentXmlSerializer;
import gov.nih.nci.cabig.ctms.lang.NowFactory;
import org.restlet.Context;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.data.*;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.restlet.resource.Representation;
import org.springframework.beans.factory.annotation.Required;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;

import java.util.*;
import java.text.SimpleDateFormat;


/**
 * @author Jalpa Patel
 */
public class SubjectCentricScheduleResource extends AbstractCollectionResource<StudySubjectAssignment> {
    private StudyCalendarXmlCollectionSerializer<StudySubjectAssignment> xmlSerializer;
    private SubjectDao subjectDao;
    private AuthorizationService authorizationService;
    private NowFactory nowFactory;

    private final SimpleDateFormat dayFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private final String ID = "id";
    private final String ACTIVITY = "activity";
    private final String STATE = "state";
    private final String IDEAL_DATE = "ideal-date";
    private final String DETAILS = "details";
    private final String STUDY = "study";
    private final String SEGMENT = "studySegment";
    private final String PLANNEDDAY = "planned-day";
    private final String HISTORY = "history";
    private final String CONDITION = "condition";

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        ((StudySubjectAssignmentXmlSerializer)xmlSerializer).setSubjectCentric(true);
        
    }

    @Override
    @SuppressWarnings({ "ThrowInsideCatchBlockWhichIgnoresCaughtException" })
    public Collection<StudySubjectAssignment> getAllObjects() throws ResourceException {
        String subjectId = UriTemplateParameters.SUBJECT_IDENTIFIER.extractFrom(getRequest());
        if (subjectId == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,"No subject identifier in request");
        }
        Subject subject = subjectDao.findSubjectByPersonId(subjectId);
        if (subject == null) {
            subject = subjectDao.getByGridId(subjectId);
            if (subject == null) {
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,"Subject doesn't exist with id "+subjectId);
            }
        }
        return subject.getAssignments();
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        List<StudySubjectAssignment> allAssignments = new ArrayList<StudySubjectAssignment> (getAllObjects());
        List<StudySubjectAssignment> visibleAssignments
                = authorizationService.filterAssignmentsForVisibility(allAssignments, getCurrentUser());
        if (variant.getMediaType().includes(MediaType.TEXT_XML)) {
            return createXmlRepresentation(visibleAssignments);
        }
        else if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
            return createJSONRepresentation(allAssignments,visibleAssignments);
        }
        return null;
    }

    public Representation createJSONRepresentation(List<StudySubjectAssignment> allAssignments,List<StudySubjectAssignment> visibleAssignments)
            throws ResourceException  {
        Set<StudySubjectAssignment> hiddenAssignments
                = new LinkedHashSet<StudySubjectAssignment>(allAssignments);
        for (StudySubjectAssignment visibleAssignment : visibleAssignments) {
                hiddenAssignments.remove(visibleAssignment);
        }
        SubjectCentricSchedule schedule = new SubjectCentricSchedule(
            visibleAssignments, new ArrayList<StudySubjectAssignment>(hiddenAssignments), nowFactory);
        JSONObject dayWiseActivities = new JSONObject();
        try {
            for (ScheduleDay scheduleDay : schedule.getDays()) {
                if (!scheduleDay.getActivities().isEmpty()) {
                   dayWiseActivities.put(new String(dayFormatter.format(scheduleDay.getDate())),createActivityList(scheduleDay.getActivities())); 
                }
            }
        } catch (JSONException e) {
	        throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
	    }
        JsonRepresentation jr = new JsonRepresentation(dayWiseActivities);
	    return jr;
    }

    private JSONArray createActivityList(List<ScheduledActivity> scheduledActivities) throws ResourceException {
        JSONArray activityList =  new JSONArray();
        try {
            for (ScheduledActivity scheduledActivity: scheduledActivities) {
                JSONObject activity = new JSONObject();
                activity.put(ID,scheduledActivity.getGridId());
                activity.put(ACTIVITY,scheduledActivity.getActivity().getName());
                activity.put(STATE,scheduledActivity.getCurrentState().getMode());
                activity.put(IDEAL_DATE,dayFormatter.format(scheduledActivity.getIdealDate()));
                activity.put(DETAILS, scheduledActivity.getDetails());
                activity.put(STUDY,scheduledActivity.getScheduledStudySegment().getScheduledCalendar()
                       .getAssignment().getStudySite().getStudy().getAssignedIdentifier());
                activity.put(SEGMENT,scheduledActivity.getScheduledStudySegment().getName());
                activity.put(CONDITION,scheduledActivity.getPlannedActivity().getCondition());
                activity.put(PLANNEDDAY,scheduledActivity.getPlannedActivity().getDay());
                List<String> history = new ArrayList<String>();
                for (ScheduledActivityState state : scheduledActivity.getPreviousStates()) {
                    history.add(state.getTextSummary());
                }
                activity.put(HISTORY,history);
                activityList.put(activity);
            }
        }  catch (JSONException e) {
	          throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
	    }
        return activityList;
    }

    public StudyCalendarXmlCollectionSerializer<StudySubjectAssignment> getXmlSerializer() {
        return xmlSerializer;
    }

    public void setXmlSerializer(StudyCalendarXmlCollectionSerializer<StudySubjectAssignment> xmlSerializer) {
        this.xmlSerializer = xmlSerializer;
    }

    @Required
    public void setSubjectDao(SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }

    @Required
    public void setAuthorizationService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Required
    public void setNowFactory(NowFactory nowFactory) {
        this.nowFactory = nowFactory;
    }
}
