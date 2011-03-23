package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import org.codehaus.jackson.JsonGenerator;

import java.io.IOException;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.restlets.AbstractPscResource.getApiDateFormat;

/**
 * @author Nataliya Shurupova
 * @author Rhett Sutphin
 */
public class ScheduledActivityReportJsonRepresentation extends StreamingJsonRepresentation  {
    private List<ScheduledActivitiesReportRow> allRows;
    private ScheduledActivitiesReportFilters filters;

    public ScheduledActivityReportJsonRepresentation(
        ScheduledActivitiesReportFilters filters, List<ScheduledActivitiesReportRow> allRows
    ) {
        this.filters = filters;
        this.allRows = allRows;
    }

    @Override
    public void generate(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
            generator.writeFieldName("filters");
            generator.writeStartObject();
                if (filters != null) {
                    writeFilters(generator, filters);
                }
            generator.writeEndObject();
            generator.writeFieldName("rows");
            generator.writeStartArray();
                if (allRows != null) {
                    for (ScheduledActivitiesReportRow row: allRows) {
                        writeRow(generator, row);
                    }
                }
            generator.writeEndArray();
        generator.writeEndObject();
    }

    public static void writeFilters(JsonGenerator generator, ScheduledActivitiesReportFilters filters) throws IOException{
        if (filters.getActivityTypes() != null) {
            generator.writeArrayFieldStart("activity_types");
            for (ActivityType type : filters.getActivityTypes()) {
                generator.writeString(type.getName());
            }
            generator.writeEndArray();
        }
        if (filters.getActualActivityDate() != null ) {
            if (filters.getActualActivityDate().getStart() != null) {
                JacksonTools.nullSafeWriteStringField(generator, "start_date", getApiDateFormat().format(filters.getActualActivityDate().getStart()));
            }
            if (filters.getActualActivityDate().getStop() != null) {
                JacksonTools.nullSafeWriteStringField(generator, "end_date", getApiDateFormat().format(filters.getActualActivityDate().getStop()));
            }
        }
        if (filters.getResponsibleUser() != null) {
            JacksonTools.nullSafeWriteStringField(generator, "responsible_user", filters.getResponsibleUser().getLoginName());
        }
        if (filters.getSiteName() != null) {
            JacksonTools.nullSafeWriteStringField(generator, "site", filters.getSiteName());
        }
        if (filters.getStudyAssignedIdentifier() != null) {
            JacksonTools.nullSafeWriteStringField(generator, "study", filters.getStudyAssignedIdentifier());
        }
        if (filters.getCurrentStateModes() != null) {
            generator.writeArrayFieldStart("states");
            for (ScheduledActivityMode mode : filters.getCurrentStateModes()) {
                generator.writeString(mode.getDisplayName());
            }
            generator.writeEndArray();
        }
        if (filters.getLabel() != null) {
            JacksonTools.nullSafeWriteStringField(generator, "label", filters.getLabel());
        }
        if (filters.getPersonId() != null) {
            JacksonTools.nullSafeWriteStringField(generator, "person_id", filters.getPersonId());
        }
    }

    public static void writeRow(JsonGenerator generator, ScheduledActivitiesReportRow row) throws IOException {
        generator.writeStartObject();
            JacksonTools.nullSafeWriteStringField(generator, "grid_id", row.getScheduledActivity().getGridId());
            JacksonTools.nullSafeWriteStringField(generator, "activity_name", row.getScheduledActivity().getActivity().getName());
            JacksonTools.nullSafeWriteStringField(generator, "activity_type", row.getScheduledActivity().getActivity().getType().getName());
            JacksonTools.nullSafeWriteStringField(generator, "activity_status", row.getScheduledActivity().getCurrentState().getMode().getDisplayName());
            JacksonTools.nullSafeWriteStringField(generator, "scheduled_date", getApiDateFormat().format(row.getScheduledActivity().getActualDate()));
            JacksonTools.nullSafeWriteStringField(generator, "ideal_date", getApiDateFormat().format(row.getScheduledActivity().getIdealDate()));
            JacksonTools.nullSafeWriteStringField(generator, "details", row.getScheduledActivity().getDetails());
            JacksonTools.nullSafeWriteStringField(generator, "reason", row.getScheduledActivity().getCurrentState().getReason());
            if (row.getScheduledActivity().getPlannedActivity() != null) {
                JacksonTools.nullSafeWriteStringField(generator, "condition", row.getScheduledActivity().getPlannedActivity().getCondition());
            }
            if (!row.getScheduledActivity().getLabels().isEmpty()) {
                generator.writeArrayFieldStart("labels");
                for (String label : row.getScheduledActivity().getLabels()) {
                    generator.writeString(label);
                }
                generator.writeEndArray();
            }
            JacksonTools.nullSafeWriteStringField(generator, "study_subject_id", row.getStudySubjectId());
            generator.writeObjectFieldStart("subject");
                JacksonTools.nullSafeWriteStringField(generator, "name", row.getSubject().getFullName());
                JacksonTools.nullSafeWriteStringField(generator, "person_id", row.getSubject().getPersonId());
                JacksonTools.nullSafeWriteStringField(generator, "grid_id", row.getSubject().getGridId());
            generator.writeEndObject();
            if (row.getResponsibleUser() != null) {
                JacksonTools.nullSafeWriteStringField(generator, "responsible_user",
                    row.getResponsibleUser().getLoginName());
            }
            JacksonTools.nullSafeWriteStringField(generator, "study", row.getStudy().getName());
            JacksonTools.nullSafeWriteStringField(generator, "site", row.getSite().getName());
        generator.writeEndObject();
    }
}

