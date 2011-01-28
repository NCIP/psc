package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserStudySubjectAssignmentRelationship;
import edu.northwestern.bioinformatics.studycalendar.web.subject.MultipleAssignmentScheduleView;
import edu.northwestern.bioinformatics.studycalendar.web.subject.ScheduleDay;
import gov.nih.nci.cabig.ctms.lang.NowFactory;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerator;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;

import static edu.northwestern.bioinformatics.studycalendar.restlets.AbstractPscResource.getApiDateFormat;

/**
 * @author Jalpa Patel
 */
// TODO: this is split from the Resources that use it along an odd seam -- the two resources still have a
// bunch of duplicated code related to building the whole object
public class ScheduleRepresentationHelper extends StreamingJsonRepresentation  {
    private List<UserStudySubjectAssignmentRelationship> relatedAssignments;
    private TemplateService templateService;

    private List<ScheduledStudySegment> segments;
    private SortedMap<Date,List<ScheduledActivity>> activities;
    private MultipleAssignmentScheduleView schedule;
    private Subject subject;

    public ScheduleRepresentationHelper(List<UserStudySubjectAssignmentRelationship> assignments, NowFactory nowFactory, TemplateService templateService) {
        relatedAssignments = assignments;
        this.templateService = templateService;
        schedule = new MultipleAssignmentScheduleView(assignments, nowFactory);
    }

    public ScheduleRepresentationHelper(List<UserStudySubjectAssignmentRelationship> assignments, NowFactory nowFactory, TemplateService templateService, Subject subject ) {
        relatedAssignments = assignments;
        this.templateService = templateService;
        schedule = new MultipleAssignmentScheduleView(assignments, nowFactory);
        this.subject = subject;
    }



    public ScheduleRepresentationHelper(SortedMap<Date,List<ScheduledActivity>> activities,  List<ScheduledStudySegment> segments, TemplateService templateService ) {
        this.activities = activities;
        this.segments = segments;
        this.templateService = templateService;
    }

    @Override
    public void generate(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
            if (subject != null) {
                generator.writeFieldName("subject");
                createJSONSubject(generator, subject);
            }
            generator.writeFieldName("days");
            generator.writeStartObject();
                if (schedule != null) {
                    for (ScheduleDay scheduleDay : schedule.getDays()) {
                        if (!scheduleDay.getActivities().isEmpty()) {
                                generator.writeFieldName(getApiDateFormat().format(scheduleDay.getDate()));
                                createJSONScheduledActivities(generator, scheduleDay.getHasHiddenActivities(), scheduleDay.getActivities());
                        }
                    }
                } else {
                    for (Date date : activities.keySet()) {
                        generator.writeFieldName(getApiDateFormat().format(date));
                        createJSONScheduledActivities(generator, null, activities.get(date));
                    }
                }
            generator.writeEndObject();
            generator.writeFieldName("study_segments");
            generator.writeStartArray();
                if (schedule != null) {
                    for (UserStudySubjectAssignmentRelationship relationship: relatedAssignments) {
                        if (relationship.isVisible()) {
                            for (ScheduledStudySegment scheduledStudySegment : relationship.getAssignment().getScheduledCalendar().getScheduledStudySegments()) {
                                createJSONStudySegment(generator, scheduledStudySegment);
                            }
                        }
                    }
                } else {
                    for (ScheduledStudySegment segment : segments) {
                        createJSONStudySegment(generator, segment);
                    }
                }
            generator.writeEndArray();
        generator.writeEndObject();
    }

    public static void createJSONStateInfo(JsonGenerator generator, ScheduledActivityState state) throws IOException{
        generator.writeStartObject();
            JacksonTools.nullSafeWriteStringField(generator, "name", state.getMode().toString());
            if (state.getDate() != null) {
                JacksonTools.nullSafeWriteStringField(generator, "date", getApiDateFormat().format(state.getDate()));
            }
            JacksonTools.nullSafeWriteStringField(generator, "reason", state.getReason());
        generator.writeEndObject();
    }

    public static void createJSONActivityProperty(JsonGenerator generator, ActivityProperty ap) throws IOException {
        generator.writeStartObject();
            JacksonTools.nullSafeWriteStringField(generator, "namespace", ap.getNamespace());
            JacksonTools.nullSafeWriteStringField(generator, "name", ap.getName());
            JacksonTools.nullSafeWriteStringField(generator, "value", ap.getValue());
        generator.writeEndObject();
    }

    public static void createJSONActivity(JsonGenerator generator, Activity activity) throws IOException{
        generator.writeStartObject();
            JacksonTools.nullSafeWriteStringField(generator, "name", activity.getName());
            JacksonTools.nullSafeWriteStringField(generator, "type", activity.getType().getName());

            if (!activity.getProperties().isEmpty()) {
                generator.writeFieldName("properties");
                generator.writeStartArray();
                for (ActivityProperty ap: activity.getProperties()) {
                    createJSONActivityProperty(generator, ap);
                }
                generator.writeEndArray();
            }

        generator.writeEndObject();
    }

    public static String formatDaysFromPlan(ScheduledActivity sa) {
        DayNumber day = sa.getDayNumber();
        if (day == null) {
            return null;
        } else {
            return day.getHasCycle() ? day.toString() : "Day " + day.getDayNumber();
        }
    }

    public void createJSONScheduledActivity(JsonGenerator generator, ScheduledActivity scheduledActivity) throws IOException {
        generator.writeStartObject();
            if (scheduledActivity.getGridId()!=null) {
                JacksonTools.nullSafeWriteStringField(generator, "id", scheduledActivity.getGridId());
            }
            JacksonTools.nullSafeWriteStringField(generator, "study", templateService.findAncestor(scheduledActivity.getScheduledStudySegment().getStudySegment(), PlannedCalendar.class).getStudy().getName());
            JacksonTools.nullSafeWriteStringField(generator, "study_segment", scheduledActivity.getScheduledStudySegment().getName());
            JacksonTools.nullSafeWriteStringField(generator, "ideal_date", getApiDateFormat().format(scheduledActivity.getIdealDate()));
            if (scheduledActivity.getPlannedActivity() != null && scheduledActivity.getPlannedActivity().getPlanDay() != null) {
                JacksonTools.nullSafeWriteStringField(generator, "plan_day", scheduledActivity.getPlannedActivity().getPlanDay());
            }
            generator.writeFieldName("current_state");
            createJSONStateInfo(generator, scheduledActivity.getCurrentState());

            generator.writeFieldName("activity");
            createJSONActivity(generator, scheduledActivity.getActivity());

            if (scheduledActivity.getScheduledStudySegment().getScheduledCalendar().getAssignment() != null) {
                generator.writeFieldName("assignment");
                createJSONAssignmentProperties(generator, scheduledActivity.getScheduledStudySegment().getScheduledCalendar().getAssignment());
                JacksonTools.nullSafeWriteStringField(generator, "subject", scheduledActivity.getScheduledStudySegment().getScheduledCalendar().getAssignment().getSubject().getFullName());
                JacksonTools.nullSafeWriteStringField(generator, "study_subject_id", scheduledActivity.getScheduledStudySegment().getScheduledCalendar().getAssignment().getStudySubjectId());
            }

            JacksonTools.nullSafeWriteStringField(generator, "details", scheduledActivity.getDetails());
            if (scheduledActivity.getPlannedActivity() != null) {
                JacksonTools.nullSafeWriteStringField(generator, "condition", scheduledActivity.getPlannedActivity().getCondition());
            }
            if (!scheduledActivity.getLabels().isEmpty()) {
                JacksonTools.nullSafeWriteStringField(generator, "labels", StringUtils.join(scheduledActivity.getLabels().iterator(), ' '));
            }
            JacksonTools.nullSafeWriteStringField(generator, "formatted_plan_day", formatDaysFromPlan(scheduledActivity));


            generator.writeFieldName("state_history");
            generator.writeStartArray();
            for (ScheduledActivityState state : scheduledActivity.getAllStates()) {
                createJSONStateInfo(generator, state);
            }
            generator.writeEndArray();
        generator.writeEndObject();
    }

    private void createJSONAssignmentProperties(JsonGenerator generator, StudySubjectAssignment assignment) throws IOException {
        generator.writeStartObject();
        JacksonTools.nullSafeWriteStringField(generator, "id", assignment.getGridId());
        JacksonTools.nullSafeWriteStringField(generator, "name", assignment.getName());
        generator.writeFieldName("subject_coordinator");
            generator.writeStartObject();
                if (assignment.getStudySubjectCalendarManager() != null) {
                    JacksonTools.nullSafeWriteStringField(generator, "username", assignment.getStudySubjectCalendarManager().getLoginName());
                }
            generator.writeEndObject();
        JacksonTools.nullSafeWriteStringField(generator, "id", assignment.getGridId());

        UserStudySubjectAssignmentRelationship rel = findAssociatedRelationship(assignment);
        if (rel != null) {
            generator.writeFieldName("privileges");
            generator.writeStartArray();

                if (rel.getCanUpdateSchedule()) {
                    generator.writeString("update");
                }

            generator.writeEndArray();
        }
        generator.writeEndObject();

    }

    private UserStudySubjectAssignmentRelationship findAssociatedRelationship(StudySubjectAssignment assignment) {
        for (UserStudySubjectAssignmentRelationship r : relatedAssignments) {
            if (r.getAssignment().equals(assignment)) {
                return r;
            }
        }
        return null;
    }

    public void createJSONScheduledActivities(JsonGenerator generator, Boolean hidden_activities, Collection<ScheduledActivity> scheduledActivities) throws IOException{
        generator.writeStartObject();
            if (hidden_activities != null) {
                JacksonTools.nullSafeWriteStringField(generator, "hidden_activities", ""+ hidden_activities.toString());
            }
            generator.writeFieldName("activities");

            generator.writeStartArray();
                for (ScheduledActivity scheduledActivity : scheduledActivities) {
                    createJSONScheduledActivity(generator, scheduledActivity);
                }
            generator.writeEndArray();
        generator.writeEndObject();
    }

    public void createJSONStudySegment(JsonGenerator generator, ScheduledStudySegment segment) throws IOException {
        generator.writeStartObject();
            if (segment.getGridId() != null) {
                JacksonTools.nullSafeWriteStringField(generator,"id", segment.getGridId());
            }
            JacksonTools.nullSafeWriteStringField(generator, "name", segment.getName());
            if (segment.getScheduledCalendar().getAssignment() != null) {
                generator.writeFieldName("assignment");
                createJSONAssignmentProperties(generator, segment.getScheduledCalendar().getAssignment());
                JacksonTools.nullSafeWriteStringField(generator,"subject", segment.getScheduledCalendar().getAssignment().getSubject().getFullName());
            }

            generator.writeFieldName("range");
            generator.writeStartObject();
                JacksonTools.nullSafeWriteStringField(generator, "start_date", getApiDateFormat().format(segment.getDateRange().getStart()));
                JacksonTools.nullSafeWriteStringField(generator, "stop_date", getApiDateFormat().format(segment.getDateRange().getStop()));
            generator.writeEndObject();

            generator.writeFieldName("planned");
            generator.writeStartObject();
                generator.writeFieldName("segment");
                generator.writeStartObject();
                    JacksonTools.nullSafeWriteStringField(generator, "id", segment.getStudySegment().getGridId());
                    JacksonTools.nullSafeWriteStringField(generator, "name", segment.getStudySegment().getName());
                generator.writeEndObject();

            generator.writeFieldName("epoch");
            generator.writeStartObject();
            Epoch e = templateService.findAncestor(segment.getStudySegment(),Epoch.class);
            JacksonTools.nullSafeWriteStringField(generator, "id", e.getGridId());
            JacksonTools.nullSafeWriteStringField(generator, "name", e.getName());
            generator.writeEndObject();


            generator.writeFieldName("study");
            generator.writeStartObject();
            JacksonTools.nullSafeWriteStringField(generator, "assigned_identifier", templateService.findAncestor(segment.getStudySegment(), PlannedCalendar.class).getStudy().getAssignedIdentifier());
            generator.writeEndObject();


            generator.writeEndObject();
        generator.writeEndObject();
    }

    public void createJSONSubject(JsonGenerator generator, Subject subject) throws IOException {
        generator.writeStartObject();
        JacksonTools.nullSafeWriteStringField(generator, "first_name", subject.getFirstName());
        JacksonTools.nullSafeWriteStringField(generator, "last_name", subject.getLastName());
        JacksonTools.nullSafeWriteStringField(generator, "full_name", subject.getFullName());
        JacksonTools.nullSafeWriteStringField(generator, "last_first", subject.getLastFirst());
        if (subject.getDateOfBirth() != null) {
            generator.writeStringField(
                "birth_date", getApiDateFormat().format(subject.getDateOfBirth()));
        }
        if (subject.getGender() != null) {
            generator.writeStringField("gender", subject.getGender().getDisplayName());
        }
        generator.writeEndObject();
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
}
