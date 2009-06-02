package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;
import org.restlet.resource.ResourceException;
import org.restlet.data.Status;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityProperty;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;

import java.util.List;
import java.text.SimpleDateFormat;

/**
 * @author Jalpa Patel
 */
public class ScheduleRepresentationHelper {
    private static final SimpleDateFormat dayFormatter = new SimpleDateFormat("yyyy-MM-dd");
    public static JSONObject createJSONStateInfo(ScheduledActivityState state) throws ResourceException{
        try {
            JSONObject stateInfo = new JSONObject();
            stateInfo.put("name", state.getMode());
            stateInfo.put("date", state.getDate());
            stateInfo.put("reason", state.getReason());
            return stateInfo;
        } catch (JSONException e) {
	        throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
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
	        throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
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
	        throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
	    }
    }

    public static JSONObject createJSONScheduledActivity(ScheduledActivity sa) throws ResourceException {
        try {
            JSONObject jsonSA = new JSONObject();
            if (sa.getGridId() != null) {
                jsonSA.put("id", sa.getGridId());
            }
            jsonSA.put("study", sa.getScheduledStudySegment().getStudySegment()
                                   .getEpoch().getPlannedCalendar().getStudy().getAssignedIdentifier());
            jsonSA.put("study_segment", sa.getScheduledStudySegment().getName());
            jsonSA.put("ideal_date", sa.getIdealDate());
            jsonSA.put("plan_day", sa.getPlannedActivity().getDay());
            jsonSA.put("current_state", createJSONStateInfo(sa.getCurrentState()));
            jsonSA.put("activity", createJSONActivity(sa.getActivity()));
            JSONArray state_history =  new JSONArray();
            for (ScheduledActivityState state : sa.getAllStates()) {
                state_history.put(createJSONStateInfo(state));
            }
            jsonSA.put("state_history", state_history);
            return jsonSA;
        } catch (JSONException e) {
	        throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
	    }
    }

    public static JSONObject createJSONScheduledActivities(Boolean hidden_activities, List<ScheduledActivity> scheduledActivities) throws ResourceException{
        try {
            JSONObject jsonScheduledActivities = new JSONObject();
            if (hidden_activities != null) {
                jsonScheduledActivities.put("hidden_activities", hidden_activities);
            }
            JSONArray activities = new JSONArray();
            for (ScheduledActivity scheduledActivity: scheduledActivities ) {
                activities.put(createJSONScheduledActivity(scheduledActivity));
            }
            jsonScheduledActivities.put("activities", activities);
            return jsonScheduledActivities;
        } catch (JSONException e) {
	        throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
	    }
    }

    public static JSONObject createJSONStudySegment(ScheduledStudySegment segment) throws ResourceException {
        try {
            JSONObject jsonSegment = new JSONObject();
            jsonSegment.put("name", segment.getName());
            JSONObject jsonRange = new JSONObject();
            jsonRange.put("start_date", dayFormatter.format(segment.getDateRange().getStart()));
            jsonRange.put("stop_date", dayFormatter.format(segment.getDateRange().getStop()));
            jsonSegment.put("range", jsonRange);
            JSONObject jsonPlannedSegmentInfo = new JSONObject();
            JSONObject jsonPlannedSegment = new JSONObject();
            jsonPlannedSegment.put("id", segment.getStudySegment().getId());
            jsonPlannedSegment.put("name", segment.getStudySegment().getName());
            jsonPlannedSegmentInfo.put("segment", jsonPlannedSegment);
            JSONObject jsonEpoch = new JSONObject();
            jsonEpoch.put("id", segment.getStudySegment().getEpoch().getId());
            jsonEpoch.put("name", segment.getStudySegment().getEpoch().getName());
            jsonPlannedSegmentInfo.put("epoch", jsonEpoch);
            JSONObject jsonStudy =  new JSONObject();
            jsonStudy.put("assigned_identifier", segment.getStudySegment().getEpoch().
                                               getPlannedCalendar().getStudy().getAssignedIdentifier());
            jsonPlannedSegmentInfo.put("study", jsonStudy);
            jsonSegment.put("planned", jsonPlannedSegmentInfo);
            return jsonSegment;
        } catch (JSONException e) {
	        throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
	    }
    }
}
