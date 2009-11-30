package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import static edu.northwestern.bioinformatics.studycalendar.restlets.AbstractPscResource.getApiDateFormat;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import edu.northwestern.bioinformatics.studycalendar.web.subject.SubjectCentricSchedule;
import edu.northwestern.bioinformatics.studycalendar.web.subject.ScheduleDay;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Representation;
import org.restlet.ext.json.JsonRepresentation;
import org.springframework.beans.factory.annotation.Required;
import gov.nih.nci.cabig.ctms.lang.NowFactory;

import java.util.Collections;
import java.util.List;

/**
 * @author Jalpa Patel
 */
// TODO: this is split from the Resources that use it along an odd seam -- the two resources still have a
// bunch of duplicated code related to building the whole object
public class ScheduleRepresentationHelper {
    private TemplateService templateService;
    private NowFactory nowFactory;

    public static JSONObject createJSONStateInfo(ScheduledActivityState state) throws ResourceException{
        try {
            JSONObject stateInfo = new JSONObject();
            stateInfo.put("name", state.getMode().toString());
            if (state.getDate() != null) {
                stateInfo.put("date", getApiDateFormat().format(state.getDate()));
            }
            stateInfo.put("reason", state.getReason());
            return stateInfo;
        } catch (JSONException e) {
	        throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
	    }
    }

    public static JSONObject createJSONActivityProperty(ActivityProperty ap) throws ResourceException {
        try {
            JSONObject jsonAP = new JSONObject();
            jsonAP.put("namespace", ap.getNamespace());
            jsonAP.put("name", ap.getName());
            jsonAP.put("value", ap.getValue());
            return jsonAP;
        } catch (JSONException e) {
	        throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
	    }

    }

    public static JSONObject createJSONActivity(Activity activity) throws ResourceException{
        try {
            JSONObject jsonActivity = new JSONObject();
            jsonActivity.put("name", activity.getName());
            jsonActivity.put("type", activity.getType().getName());
            if (!activity.getProperties().isEmpty()) {
                JSONArray properties  = new JSONArray();
                for (ActivityProperty ap: activity.getProperties()) {
                    properties.put(createJSONActivityProperty(ap));
                }
                jsonActivity.put("properties", properties);
            }
            return jsonActivity;
        } catch (JSONException e) {
	        throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
	    }
    }

    public static String formatDaysFromPlan(ScheduledActivity sa) {
        DayNumber day = sa.getDayNumber();
        if (day == null) {
            return null;
        } else {
            return day.getHasCycle() ? day.toString() : "Day " + day.getDayNumber();
        }
    }

    public JSONObject createJSONScheduledActivity(ScheduledActivity sa) throws ResourceException {
        try {
            JSONObject jsonSA = new JSONObject();
            if (sa.getGridId() != null) {
                jsonSA.put("id", sa.getGridId());
            }
            jsonSA.put("study", templateService.findAncestor(sa.getScheduledStudySegment().getStudySegment(), PlannedCalendar.class).getStudy().getName());
            jsonSA.put("study_segment", sa.getScheduledStudySegment().getName());
            jsonSA.put("ideal_date", getApiDateFormat().format(sa.getIdealDate()));
            if (sa.getPlannedActivity() != null && sa.getPlannedActivity().getPlanDay() != null) {
                jsonSA.put("plan_day", sa.getPlannedActivity().getPlanDay());
            }
            jsonSA.put("current_state", createJSONStateInfo(sa.getCurrentState()));
            jsonSA.put("activity", createJSONActivity(sa.getActivity()));
            if (sa.getScheduledStudySegment().getScheduledCalendar().getAssignment() != null) {
                jsonSA.put("assignment", createJSONAssignmentProperties(
                    sa.getScheduledStudySegment().getScheduledCalendar().getAssignment()));
                jsonSA.put("subject", sa.getScheduledStudySegment().getScheduledCalendar().getAssignment().getSubject().getFullName());
            }

            jsonSA.put("details", sa.getDetails());
            if (sa.getPlannedActivity() != null) {
                jsonSA.put("condition", sa.getPlannedActivity().getCondition());
            }
            if (!sa.getLabels().isEmpty()) {
                jsonSA.put("labels", StringUtils.join(sa.getLabels().iterator(), ' '));
            }
            jsonSA.put("formatted_plan_day", formatDaysFromPlan(sa));

            JSONArray state_history =  new JSONArray();
            for (ScheduledActivityState state : sa.getAllStates()) {
                state_history.put(createJSONStateInfo(state));
            }
            jsonSA.put("state_history", state_history);
            return jsonSA;
        } catch (JSONException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
        }
    }

    private static JSONObject createJSONAssignmentProperties(StudySubjectAssignment assignment) {
        return new JSONObject(
            new MapBuilder<String, String>()
                .put("id", assignment.getGridId())
                .put("name", assignment.getName()).toMap());
    }

    public JSONObject createJSONScheduledActivities(Boolean hidden_activities, List<ScheduledActivity> scheduledActivities) throws ResourceException{
        try {
            JSONObject jsonScheduledActivities = new JSONObject();
            if (hidden_activities != null) {
                jsonScheduledActivities.put("hidden_activities", hidden_activities);
            }
            JSONArray activities = new JSONArray();
            Collections.sort(scheduledActivities);
            for (ScheduledActivity scheduledActivity: scheduledActivities ) {
                activities.put(createJSONScheduledActivity(scheduledActivity));
            }
            jsonScheduledActivities.put("activities", activities);
            return jsonScheduledActivities;
        } catch (JSONException e) {
	        throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
	    }
    }

    public JSONObject createJSONStudySegment(ScheduledStudySegment segment) throws ResourceException {
        try {
            JSONObject jsonSegment = new JSONObject();
            jsonSegment.put("name", segment.getName());
            if (segment.getGridId() != null) {
                jsonSegment.put("id", segment.getGridId());
            }
            if (segment.getScheduledCalendar().getAssignment() != null) {
                jsonSegment.put("assignment", createJSONAssignmentProperties(
                    segment.getScheduledCalendar().getAssignment()));
                jsonSegment.put("subject", segment.getScheduledCalendar().getAssignment().getSubject().getFullName());
            }
            JSONObject jsonRange = new JSONObject();
            jsonRange.put("start_date", getApiDateFormat().format(segment.getDateRange().getStart()));
            jsonRange.put("stop_date", getApiDateFormat().format(segment.getDateRange().getStop()));
            jsonSegment.put("range", jsonRange);
            JSONObject jsonPlannedSegmentInfo = new JSONObject();
            JSONObject jsonPlannedSegment = new JSONObject();
            jsonPlannedSegment.put("id", segment.getStudySegment().getGridId());
            jsonPlannedSegment.put("name", segment.getStudySegment().getName());
            jsonPlannedSegmentInfo.put("segment", jsonPlannedSegment);
            JSONObject jsonEpoch = new JSONObject();
            jsonEpoch.put("id", templateService.findAncestor(segment.getStudySegment(),Epoch.class).getGridId());
            jsonEpoch.put("name", templateService.findAncestor(segment.getStudySegment(),Epoch.class).getName());
            jsonPlannedSegmentInfo.put("epoch", jsonEpoch);
            JSONObject jsonStudy =  new JSONObject();
            jsonStudy.put("assigned_identifier", templateService.findAncestor(segment.getStudySegment(), PlannedCalendar.class).getStudy().getAssignedIdentifier());
            jsonPlannedSegmentInfo.put("study", jsonStudy);
            jsonSegment.put("planned", jsonPlannedSegmentInfo);
            return jsonSegment;
        } catch (JSONException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
        }
    }

    public Representation createJSONRepresentation(List<StudySubjectAssignment> visibleAssignments, List<StudySubjectAssignment> hiddenAssignments )
            throws ResourceException  {
        SubjectCentricSchedule schedule = new SubjectCentricSchedule(
            visibleAssignments, hiddenAssignments, nowFactory);
        JSONObject jsonData = new JSONObject();
        try {
            JSONObject dayWiseActivities = new JSONObject();
            for (ScheduleDay scheduleDay : schedule.getDays()) {
                if (!scheduleDay.getActivities().isEmpty()) {
                    dayWiseActivities.put(getApiDateFormat().format(scheduleDay.getDate()),
                           createJSONScheduledActivities(scheduleDay.getHasHiddenActivities(), scheduleDay.getActivities()));
                }
            }
            JSONArray studySegments = new JSONArray();
            for (StudySubjectAssignment studySubjectAssignment: visibleAssignments) {
                for (ScheduledStudySegment scheduledStudySegment : studySubjectAssignment.getScheduledCalendar().getScheduledStudySegments()) {
                    studySegments.put(createJSONStudySegment(scheduledStudySegment));
                }
            }
            jsonData.put("days", dayWiseActivities);
            jsonData.put("study_segments", studySegments);
        } catch (JSONException e) {
	        throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
	    }
        return new JsonRepresentation(jsonData);
    }

    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Required
    public void setNowFactory(NowFactory nowFactory) {
        this.nowFactory = nowFactory;
    }
}
