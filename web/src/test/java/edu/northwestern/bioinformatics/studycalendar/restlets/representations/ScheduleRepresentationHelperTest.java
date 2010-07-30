package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityProperty;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Canceled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserStudySubjectAssignmentRelationship;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import gov.nih.nci.cabig.ctms.lang.NowFactory;
import gov.nih.nci.cabig.ctms.lang.StaticNowFactory;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.AuthorizationScopeMappings.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import static gov.nih.nci.cabig.ctms.lang.DateTools.*;
import static org.easymock.EasyMock.*;


/**
 * @author Jalpa Patel
 */
public class ScheduleRepresentationHelperTest extends JsonRepresentationTestCase{
    private ScheduledActivity sa;
    private ScheduledActivityState state;
    private Activity activity;
    private List<ActivityProperty> properties;
    private List<ScheduledActivity> scheduledActivities;
    private ScheduledCalendar scheduledCalendar = new ScheduledCalendar();
    private StringWriter out;
    private Epoch epoch;
    private PlannedCalendar calendar;
    private ScheduledStudySegment scheduledSegment;

    private ScheduleRepresentationHelper scheduleRepresentationHelper;
    private TemplateService templateService;
    private JsonGenerator generator;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void setUp() throws Exception {
        super.setUp();

        NowFactory nowFactory = new StaticNowFactory();
        templateService = registerMockFor(TemplateService.class);
        Study study = createSingleEpochStudy("S", "Treatment");
        Site site = createSite("site");
        Subject subject = createSubject("First", "Last");

        calendar = new PlannedCalendar();
        epoch = Epoch.create("Treatment", "A", "B", "C");
        epoch.setGridId("E");
        calendar.addEpoch(epoch);
        calendar.setGridId("calendarGridId");
        calendar.setId(15);
        study.setPlannedCalendar(calendar);

        StudySegment studySegment = createNamedInstance("Screening", StudySegment.class);
        studySegment.setGridId("S");
        studySegment.setId(100);
        studySegment.setEpoch(epoch);
        Period p = createPeriod(3, 7, 1);
        studySegment.addPeriod(p);
        PlannedActivity pa = createPlannedActivity(activity, 4);
        p.addPlannedActivity(pa);
        StudySubjectAssignment ssa = createAssignment(study,site,subject);
        ssa.setSubjectCoordinator(Fixtures.createUser("subjCoord1", Role.SUBJECT_COORDINATOR));
        state = new Scheduled();
        state.setDate(DateTools.createDate(2009, Calendar.APRIL, 3));
        state.setReason("Just moved by 4 days");
        sa = createScheduledActivity(pa, 2009, Calendar.APRIL, 7, state);
        sa.setGridId("1111");
        sa.setIdealDate(DateTools.createDate(2009, Calendar.APRIL, 7));
        sa.setScheduledStudySegment(scheduledSegment);

        out = new StringWriter();
        generator = new JsonFactory().createJsonGenerator(out);

        ActivityType activityType = createActivityType("Type1");
        activity = createActivity("activity1", activityType);

        properties = new ArrayList<ActivityProperty>();
        ActivityProperty prop1 = createActivityProperty("URI", "text", "activity defination");
        properties.add(prop1);
        properties.add(createActivityProperty("URI", "template", "activity uri"));
        SortedSet<String> labels = new TreeSet<String>();
        labels.add("label1");
        labels.add("label2");
        sa.setLabels(labels);
        sa.setActivity(activity);
        scheduledActivities =  new ArrayList<ScheduledActivity>();
        scheduledActivities.add(sa);

        scheduledSegment = createScheduledStudySegment(studySegment, DateTools.createDate(2009, Calendar.APRIL, 3));
        scheduledSegment.setGridId("GRID-SEG");
        scheduledCalendar.addStudySegment(scheduledSegment);
        scheduledCalendar.setAssignment(setGridId("GRID-ASSIGN", ssa));
        sa.setScheduledStudySegment(scheduledSegment);

        ssa.getScheduledCalendar().addStudySegment(createScheduledStudySegment(createDate(2006, Calendar.APRIL, 1), 365));

        PscUser user = AuthorizationObjectFactory.createPscUser("jo",
            createSuiteRoleMembership(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forAllStudies().forAllSites());
        UserStudySubjectAssignmentRelationship rel = new UserStudySubjectAssignmentRelationship(user, ssa);

        scheduleRepresentationHelper = new ScheduleRepresentationHelper(Arrays.asList(rel), nowFactory, templateService);
    }

    public void testStateInfoInJson() throws Exception {
        generator.writeStartObject();
        generator.writeFieldName("activities");
            replayMocks();
                ScheduleRepresentationHelper.createJSONStateInfo(generator, state);
            verifyMocks();
        generator.writeEndObject();
        JSONObject stateInfo = outputAsObject();

        assertEquals("State mode is different", "scheduled", stateInfo.getJSONObject("activities").get("name"));
        assertEquals("State date is different", "2009-04-03", stateInfo.getJSONObject("activities").get("date"));
        assertEquals("State reason is different", state.getReason(), stateInfo.getJSONObject("activities").get("reason"));
    }

    public void testStateContainsNoDate() throws Exception {
        ScheduledActivityState saState = new Canceled();
        generator.writeStartObject();
        generator.writeFieldName("activities");
            replayMocks();
                ScheduleRepresentationHelper.createJSONStateInfo(generator, saState);
            verifyMocks();
        generator.writeEndObject();
        JSONObject stateInfo = outputAsObject();
        assertEquals("State mode is different", "canceled", stateInfo.getJSONObject("activities").get("name"));
        assertFalse("State date should not be present", stateInfo.getJSONObject("activities").has("date"));
    }

    public void testActivityPropertyInJson() throws Exception {
        ActivityProperty ap = createActivityProperty("URI","text","activity defination");
        generator.writeStartObject();
        generator.writeFieldName("activities");
            replayMocks();
                ScheduleRepresentationHelper.createJSONActivityProperty(generator, ap);
            verifyMocks();
        generator.writeEndObject();
        JSONObject apJson = outputAsObject();

        assertEquals("Namespace is different", ap.getNamespace(), apJson.getJSONObject("activities").get("namespace"));
        assertEquals("Name is different", ap.getName(), apJson.getJSONObject("activities").get("name"));
        assertEquals("Value is different", ap.getValue(), apJson.getJSONObject("activities").get("value"));
    }

    public void testActivityInJson() throws Exception {
        activity.setProperties(properties);
        generator.writeStartObject();
        generator.writeFieldName("activities");
            replayMocks();
                ScheduleRepresentationHelper.createJSONActivity(generator, activity);
            verifyMocks();
        generator.writeEndObject();
        JSONObject activityJson = outputAsObject();
        assertEquals("No of elements are different", 3, activityJson.getJSONObject("activities").length());
        assertEquals("Activity name is different", activity.getName(), activityJson.getJSONObject("activities").get("name"));
        assertEquals("Activity Type is different", activity.getType().getName(), activityJson.getJSONObject("activities").get("type"));
        assertEquals("no of elements is different", 2,((JSONArray)activityJson.getJSONObject("activities").get("properties")).length());
    }

    public void testActivityWhenPropertiesAreEmpty() throws Exception {
        generator.writeStartObject();
        generator.writeFieldName("activities");
            replayMocks();
                ScheduleRepresentationHelper.createJSONActivity(generator, activity);
            verifyMocks();
        generator.writeEndObject();
        JSONObject activityJson = outputAsObject();
        assertEquals("No of elements are different", 2, activityJson.getJSONObject("activities").length());
        assertTrue(activityJson.getJSONObject("activities").isNull("properties"));
    }

    public void testScheduledActivityInJson() throws Exception {

        generator.writeStartObject();
        generator.writeFieldName("activities");
            expect(templateService.findAncestor(scheduledSegment.getStudySegment(), PlannedCalendar.class)).andReturn(calendar);
            replayMocks();
                scheduleRepresentationHelper.createJSONScheduledActivity(generator, sa);
            verifyMocks();
        generator.writeEndObject();
        JSONObject jsonSA = outputAsObject();

        assertEquals("Grid Id is different", sa.getGridId(), jsonSA.getJSONObject("activities").get("id"));
        assertEquals("Study is different", sa.getScheduledStudySegment().getStudySegment().
                getEpoch().getPlannedCalendar().getStudy().getAssignedIdentifier(), jsonSA.getJSONObject("activities").get("study"));
        assertEquals("Study Segment is different", sa.getScheduledStudySegment().getName(), jsonSA.getJSONObject("activities").get("study_segment"));
        assertEquals("Ideal Date is different", "2009-04-07", jsonSA.getJSONObject("activities").get("ideal_date"));
        assertEquals("Planned day is different", "6", jsonSA.getJSONObject("activities").get("plan_day"));
    }

    public void testScheduledActivityIncludesAssignmentName() throws Exception {
        generator.writeStartObject();
        generator.writeFieldName("activities");
            expect(templateService.findAncestor(scheduledSegment.getStudySegment(), PlannedCalendar.class)).andReturn(calendar);
            replayMocks();
                scheduleRepresentationHelper.createJSONScheduledActivity(generator, sa);
            verifyMocks();
        generator.writeEndObject();
        JSONObject jsonSA = outputAsObject();
        assertEquals("Missing assignment name",
           "S" , ((JSONObject) jsonSA.getJSONObject("activities").get("assignment")).get("name"));
    }

    public void testScheduledActivityIncludesAssignmentId() throws Exception {
        generator.writeStartObject();
        generator.writeFieldName("activities");
            expect(templateService.findAncestor(scheduledSegment.getStudySegment(), PlannedCalendar.class)).andReturn(calendar);
            replayMocks();
                scheduleRepresentationHelper.createJSONScheduledActivity(generator, sa);
            verifyMocks();
        generator.writeEndObject();
        JSONObject jsonSA = outputAsObject();
        assertEquals("Missing assignment name",
            "GRID-ASSIGN", ((JSONObject) jsonSA.getJSONObject("activities").get("assignment")).get("id"));
    }

    public void testScheduledActivityCurrentState() throws Exception {
        generator.writeStartObject();
        generator.writeFieldName("activities");
            expect(templateService.findAncestor(scheduledSegment.getStudySegment(), PlannedCalendar.class)).andReturn(calendar);
            replayMocks();
                scheduleRepresentationHelper.createJSONScheduledActivity(generator, sa);
            verifyMocks();
        generator.writeEndObject();       
        JSONObject jsonSA = outputAsObject();
        JSONObject currentState = (JSONObject) jsonSA.getJSONObject("activities").get("current_state");
        assertNotNull("current state is missing", currentState);
        assertEquals("current state reason incorrect", "Just moved by 4 days", currentState.get("reason"));
        assertEquals("current state date incorrect", "2009-04-03", currentState.get("date"));
        assertEquals("current state name incorrect", "scheduled", currentState.get("name"));
    }

    public void testScheduledActivityStateHistoryContainsAllStates() throws Exception {
        generator.writeStartObject();
        generator.writeFieldName("activities");
            expect(templateService.findAncestor(scheduledSegment.getStudySegment(), PlannedCalendar.class)).andReturn(calendar);
            replayMocks();
                scheduleRepresentationHelper.createJSONScheduledActivity(generator, sa);
            verifyMocks();
        generator.writeEndObject();
        JSONObject actual = outputAsObject();
        JSONArray history = actual.getJSONObject("activities").getJSONArray("state_history");
        assertEquals(2, history.length());
    }

    public void testScheduledActivityStateHistoryStartsWithInitial() throws Exception {
      generator.writeStartObject();
        generator.writeFieldName("activities");
            expect(templateService.findAncestor(scheduledSegment.getStudySegment(), PlannedCalendar.class)).andReturn(calendar);
            replayMocks();
                scheduleRepresentationHelper.createJSONScheduledActivity(generator, sa);
            verifyMocks();
        generator.writeEndObject();
        JSONObject actual = outputAsObject();
        JSONArray history = actual.getJSONObject("activities").getJSONArray("state_history");
        JSONObject first = (JSONObject) history.get(0);
        assertEquals("Incorrect date", "2009-04-07", first.get("date"));
    }

    public void testScheduledActivityStateHistoryEndsWithCurrent() throws Exception {
      generator.writeStartObject();
        generator.writeFieldName("activities");
            expect(templateService.findAncestor(scheduledSegment.getStudySegment(), PlannedCalendar.class)).andReturn(calendar);
            replayMocks();
                scheduleRepresentationHelper.createJSONScheduledActivity(generator, sa);
            verifyMocks();
        generator.writeEndObject();
        JSONObject actual = outputAsObject();
        JSONArray history = actual.getJSONObject("activities").getJSONArray("state_history");
        JSONObject first = (JSONObject) history.get(1);
        assertEquals("Incorrect date", "2009-04-03", first.get("date"));
    }

    public void testScheduleDayWiseActivitiesInJson() throws Exception {
        generator.writeStartObject();
            generator.writeFieldName("activities");
            expect(templateService.findAncestor(scheduledSegment.getStudySegment(), PlannedCalendar.class)).andReturn(calendar);
            replayMocks();
                scheduleRepresentationHelper.createJSONScheduledActivities(generator, true, scheduledActivities);
            verifyMocks();
        generator.writeEndObject();
        JSONObject jsonScheduleActivities = outputAsObject();
        assertEquals("no of elements is different", 1,jsonScheduleActivities.length());
        assertTrue("has no hidden activities", new Boolean((String)jsonScheduleActivities.getJSONObject("activities").get("hidden_activities")));
    }


    public void testWhenHiddenActivitiesIsNull() throws Exception {
        generator.writeStartObject();
            generator.writeFieldName("activities");
            expect(templateService.findAncestor(scheduledSegment.getStudySegment(), PlannedCalendar.class)).andReturn(calendar);
            replayMocks();
                scheduleRepresentationHelper.createJSONScheduledActivities(generator, null, scheduledActivities);
            verifyMocks();
        generator.writeEndObject();
        JSONObject jsonScheduleActivities = outputAsObject();
        assertTrue(jsonScheduleActivities.isNull("hidden_activities"));
        assertEquals("no of elements is different", 1,jsonScheduleActivities.length());
    }

    public void testScheduledStudySegmentsInJson() throws Exception {
        generator.writeStartObject();
            generator.writeFieldName("study_segments");

            expect(templateService.findAncestor(scheduledSegment.getStudySegment(), Epoch.class)).andReturn(epoch);
            expect(templateService.findAncestor(scheduledSegment.getStudySegment(), PlannedCalendar.class)).andReturn(calendar);
            replayMocks();
                scheduleRepresentationHelper.createJSONStudySegment(generator, scheduledSegment);
            verifyMocks();
        generator.writeEndObject();
        JSONObject actual = outputAsObject();
        JSONObject jsonSegment = actual.getJSONObject("study_segments");
        assertEquals("has different name", scheduledSegment.getName(), jsonSegment.get("name"));
        assertEquals("missing ID", scheduledSegment.getGridId(), jsonSegment.get("id"));

        JSONObject jsonRange = (JSONObject)(jsonSegment.get("range"));
        assertEquals("different start date", "2009-04-03", jsonRange.get("start_date"));
        assertEquals("different stop date", "2009-04-09", jsonRange.get("stop_date"));

        JSONObject jsonPlannedSegmentInfo = (JSONObject)(jsonSegment.get("planned"));

        JSONObject jsonPlannedSegment = (JSONObject)(jsonPlannedSegmentInfo.get("segment"));
        assertEquals("segment has different name", scheduledSegment.getStudySegment().getName(), jsonPlannedSegment.get("name"));
        assertEquals("segment has different id", "S", jsonPlannedSegment.get("id"));

        JSONObject jsonEpoch =  (JSONObject)(jsonPlannedSegmentInfo.get("epoch"));
        assertEquals("Epoch has different name", scheduledSegment.getStudySegment().getEpoch().getName(), jsonEpoch.get("name"));
        assertEquals("Epoch has different id", "E", jsonEpoch.get("id"));
        JSONObject jsonStudy =  (JSONObject)(jsonPlannedSegmentInfo.get("study"));
        assertEquals("Study doesn't match", scheduledSegment.getStudySegment().getEpoch().getPlannedCalendar()
            .getStudy().getAssignedIdentifier(), jsonStudy.get("assigned_identifier"));
    }

    public void testScheduledSegmentIncludesAssignmentName() throws Exception {
        generator.writeStartObject();
        generator.writeFieldName("assignment");
        expect(templateService.findAncestor(scheduledSegment.getStudySegment(), Epoch.class)).andReturn(epoch);
        expect(templateService.findAncestor(scheduledSegment.getStudySegment(), PlannedCalendar.class)).andReturn(calendar);
        replayMocks();
            scheduleRepresentationHelper.createJSONStudySegment(generator, scheduledSegment);
        verifyMocks();
        generator.writeEndObject();
        JSONObject actual = outputAsObject();
        assertEquals("Missing assignment name", "Treatment: Screening",
            (actual.getJSONObject("assignment")).get("name"));
    }

    public void testCreateJSONStudySegment() throws Exception {
        generator.writeStartObject();
            generator.writeFieldName("study_segments");
            expect(templateService.findAncestor(scheduledSegment.getStudySegment(), Epoch.class)).andReturn(epoch);
            expect(templateService.findAncestor(scheduledSegment.getStudySegment(), PlannedCalendar.class)).andReturn(calendar);
        replayMocks();
            scheduleRepresentationHelper.createJSONStudySegment(generator, scheduledSegment);
        verifyMocks();
        generator.writeEndObject();
        JSONObject actual = outputAsObject();
        assertTrue("Missing key", actual.has("study_segments"));
        assertTrue("Missing properties", actual.getJSONObject("study_segments").has("id"));
        assertTrue("Missing properties", actual.getJSONObject("study_segments").has("name"));
    }

    public void testScheduledSegmentIncludesAssignmentId() throws Exception {
        generator.writeStartObject();
        generator.writeFieldName("study_segments");
        expect(templateService.findAncestor(scheduledSegment.getStudySegment(), Epoch.class)).andReturn(epoch);
        expect(templateService.findAncestor(scheduledSegment.getStudySegment(), PlannedCalendar.class)).andReturn(calendar);
        replayMocks();
            scheduleRepresentationHelper.createJSONStudySegment(generator, scheduledSegment);
        verifyMocks();
        generator.writeEndObject();
        JSONObject actual = outputAsObject();
        assertEquals("Missing assignment id", "GRID-ASSIGN",
            ((JSONObject) actual.getJSONObject("study_segments").get("assignment")).get("id"));
    }

    public void testDetailsInJson() throws Exception {
        sa.setDetails("Detail");
        generator.writeStartObject();
        generator.writeFieldName("activities");
        expect(templateService.findAncestor(scheduledSegment.getStudySegment(), PlannedCalendar.class)).andReturn(calendar);
        replayMocks();
            scheduleRepresentationHelper.createJSONScheduledActivity(generator, sa);
        verifyMocks();
        generator.writeEndObject();
        JSONObject actual = outputAsObject();
        assertEquals("Missing details", "Detail", actual.getJSONObject("activities").get("details"));
    }

    public void testMissingDetailsInJson() throws Exception {
        generator.writeStartObject();
        generator.writeFieldName("activities");
        expect(templateService.findAncestor(scheduledSegment.getStudySegment(), PlannedCalendar.class)).andReturn(calendar);
        replayMocks();
            scheduleRepresentationHelper.createJSONScheduledActivity(generator, sa);
        verifyMocks();
        generator.writeEndObject();
        JSONObject actual = outputAsObject();
        assertTrue(actual.getJSONObject("activities").isNull("details"));
    }

    public void testConditionalInJson() throws Exception {
        sa.getPlannedActivity().setCondition("Conditional Details");
        generator.writeStartObject();
        generator.writeFieldName("activities");
        expect(templateService.findAncestor(scheduledSegment.getStudySegment(), PlannedCalendar.class)).andReturn(calendar);
        replayMocks();
            scheduleRepresentationHelper.createJSONScheduledActivity(generator, sa);
        verifyMocks();
        generator.writeEndObject();
        JSONObject actual = outputAsObject();
        assertEquals("Missing conditions", "Conditional Details", actual.getJSONObject("activities").get("condition"));
    }

    public void testLabelsInJson() throws Exception {
        generator.writeStartObject();
        generator.writeFieldName("activities");
        expect(templateService.findAncestor(scheduledSegment.getStudySegment(), PlannedCalendar.class)).andReturn(calendar);
        replayMocks();
            scheduleRepresentationHelper.createJSONScheduledActivity(generator, sa);
        verifyMocks();
        generator.writeEndObject();
        JSONObject actual = outputAsObject();
        assertEquals("Missing labels", "label1 label2", actual.getJSONObject("activities").get("labels"));
    }

    private JSONObject outputAsObject() throws IOException {
        generator.close();
        try {
            return new JSONObject(out.toString());
        } catch (JSONException e) {
            log.info("Generated JSON: {}", out.toString());
            fail("Generated JSON is not valid: " + e.getMessage());
            return null; // Unreachable
        }
    }
}
