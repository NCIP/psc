package edu.northwestern.bioinformatics.studycalendar.restlets;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;

import org.json.JSONObject;
import org.json.JSONArray;


/**
 * @author Jalpa Patel
 */
public class ScheduleRepresentationHelperTest extends StudyCalendarTestCase{
    private ScheduledActivity sa;
    private ActivityProperty ap, ap1;
    private ScheduledActivityState state;
    private Activity activity;
    private PlannedActivity pa;
    private ScheduledStudySegment scheduledSegment;
    private Study study;
    private List<ActivityProperty> properties;
    private List<ScheduledActivity> scheduledActivities;
    private ScheduledCalendar scheduledCalendar = new ScheduledCalendar();
    private static final SimpleDateFormat dayFormatter = new SimpleDateFormat("yyyy-MM-dd");
    public void setUp() throws Exception {
        super.setUp();
        ActivityType activityType = Fixtures.createActivityType("Type1");
        activity = Fixtures.createActivity("activity1", activityType);
        ap = Fixtures.createActivityProperty("URI","text","activity defination");
        ap1 = Fixtures.createActivityProperty("URI","template","activity uri");
        properties = new ArrayList<ActivityProperty>();
        properties.add(ap);
        properties.add(ap1);
        pa = Fixtures.createPlannedActivity(activity, 4);
        state = new Scheduled();
        state.setDate(DateTools.createDate(2009, Calendar.APRIL, 3));
        state.setReason("Just moved by 2 days");
        sa = Fixtures.createScheduledActivity(pa, 2009, 04, 01, state);
        scheduledActivities =  new ArrayList<ScheduledActivity>();
        scheduledActivities.add(sa);
        study = Fixtures.createSingleEpochStudy("S", "Treatment");
        Epoch epoch = study.getPlannedCalendar().getEpochs().get(0);
        epoch.setId(11);
        StudySegment studySegment = epoch.getStudySegments().get(0);
        studySegment.setName("Segment1");
        studySegment.setId(21);
        scheduledSegment = Fixtures.createScheduledStudySegment(studySegment);
        scheduledCalendar.addStudySegment(scheduledSegment);
        sa.setScheduledStudySegment(scheduledSegment);
    }
    public void testStateInfoInJson() throws Exception {
        JSONObject stateInfo = ScheduleRepresentationHelper.createJSONStateInfo(state);
        assertEquals("State mode is different", state.getMode(), stateInfo.get("name"));
        assertEquals("State date is different", state.getDate(), stateInfo.get("date"));
        assertEquals("State reason is different", state.getReason(), stateInfo.get("reason"));
    }
    
    public void testActivityPropertyInJson() throws Exception {
        ActivityProperty ap = Fixtures.createActivityProperty("URI","text","activity defination");
        JSONObject apJson = ScheduleRepresentationHelper.createJSONActivityProperty(ap);
        assertEquals("Namespace is different", ap.getNamespace(), apJson.get("namespace"));
        assertEquals("Name is different", ap.getName(), apJson.get("name"));
        assertEquals("Value is different", ap.getValue(), apJson.get("value"));
    }
    
    public void testActivityInJson() throws Exception {
        activity.setProperties(properties);
        JSONObject activityJson = ScheduleRepresentationHelper.createJSONActivity(activity);
        assertEquals("No of elements are different", 3, activityJson.length());
        assertEquals("Activity name is different", activity.getName(), activityJson.get("name"));
        assertEquals("Activity Type is different", activity.getType().getName(), activityJson.get("type"));
        assertEquals("no of elements is different", 2,((JSONArray)activityJson.get("properties")).length());
    }

    public void testActivityWhenPropertiesAreEmpty() throws Exception {
        JSONObject activityJson = ScheduleRepresentationHelper.createJSONActivity(activity);
        assertEquals("No of elements are different", 2, activityJson.length());
        assertTrue(activityJson.isNull("properties"));
    }

    public void testScheduledActivityInJson() throws Exception {
        sa.setGridId("1111");
        JSONObject jsonSA = ScheduleRepresentationHelper.createJSONScheduledActivity(sa);
        assertEquals("no of elements is different", 8, jsonSA.length());
        assertEquals("Grid Id is different", sa.getGridId(), jsonSA.get("id"));
        assertEquals("Study is different", sa.getScheduledStudySegment().getStudySegment().
                getEpoch().getPlannedCalendar().getStudy().getAssignedIdentifier(), jsonSA.get("study"));
        assertEquals("Study Segment is different", sa.getScheduledStudySegment().getName(), jsonSA.get("study_segment"));
        assertEquals("Ideal Date is different", sa.getIdealDate(), jsonSA.get("ideal_date"));
        assertEquals("Planned day is different", sa.getPlannedActivity().getDay(), jsonSA.get("plan_day"));
    }

    public void testScheduledActivityWhenNoGridId() throws Exception {
        JSONObject jsonSA = ScheduleRepresentationHelper.createJSONScheduledActivity(sa);
        assertEquals("no of elements is different", 7, jsonSA.length());
        assertTrue(jsonSA.isNull("id"));
    }

    public void testScheduleDayWiseActivitiesInJson() throws Exception {
        JSONObject jsonScheduleActivities =  ScheduleRepresentationHelper.createJSONScheduledActivities(true, scheduledActivities);
        assertEquals("no of elements is different", 2,jsonScheduleActivities.length());
        assertEquals("has no hidden activities", true, jsonScheduleActivities.get("hidden_activities"));
    }

    public void testWhenHiddenActivitiesIsNull() throws Exception {
        JSONObject jsonScheduleActivities =  ScheduleRepresentationHelper.createJSONScheduledActivities(null, scheduledActivities);
        assertTrue(jsonScheduleActivities.isNull("hidden_activities"));
        assertEquals("no of elements is different", 1,jsonScheduleActivities.length());
    }

    public void testScheduledStudySegmentsInJson() throws Exception {
        JSONObject jsonSegment = ScheduleRepresentationHelper.createJSONStudySegment(scheduledSegment);
        assertEquals("has different name", scheduledSegment.getName(), jsonSegment.get("name"));
        JSONObject jsonRange = (JSONObject)(jsonSegment.get("range"));
        assertEquals("different start date", dayFormatter.format(scheduledSegment.getDateRange().getStart()), jsonRange.get("start_date"));
        assertEquals("different stop date", dayFormatter.format(scheduledSegment.getDateRange().getStop()), jsonRange.get("stop_date"));
        JSONObject jsonPlannedSegmentInfo = (JSONObject)(jsonSegment.get("planned"));
        JSONObject jsonPlannedSegment = (JSONObject)(jsonPlannedSegmentInfo.get("segment"));
        assertEquals("segment has different name", scheduledSegment.getStudySegment().getName(), jsonPlannedSegment.get("name"));
        assertEquals("segment has different Id", scheduledSegment.getStudySegment().getId(), jsonPlannedSegment.get("id"));
        JSONObject jsonEpoch =  (JSONObject)(jsonPlannedSegmentInfo.get("epoch"));
        assertEquals("Epoch has different name", scheduledSegment.getStudySegment().getEpoch().getName(), jsonEpoch.get("name"));
        assertEquals("Epoch has different Id", scheduledSegment.getStudySegment().getEpoch().getId(), jsonEpoch.get("id"));
        JSONObject jsonStudy =  (JSONObject)(jsonPlannedSegmentInfo.get("study"));
        assertEquals("Study doesn't match", scheduledSegment.getStudySegment().getEpoch().getPlannedCalendar()
                                            .getStudy().getAssignedIdentifier(), jsonStudy.get("assigned_identifier"));
    }
}
