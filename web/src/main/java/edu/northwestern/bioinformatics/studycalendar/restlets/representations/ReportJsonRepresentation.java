package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import org.codehaus.jackson.JsonGenerator;

import java.io.IOException;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.restlets.AbstractPscResource.getApiDateFormat;

/**
 * @author Nataliya Shurupova
 */
public class ReportJsonRepresentation extends StreamingJsonRepresentation  {
    private List<ScheduledActivitiesReportRow> allRows;
    private ScheduledActivitiesReportFilters filters;
    private int hiddenItemsCount;

    public ReportJsonRepresentation(ScheduledActivitiesReportFilters filters, List<ScheduledActivitiesReportRow> allRows, int hiddenItemsCount) {
        this.filters = filters;
        this.allRows = allRows;
        this.hiddenItemsCount = hiddenItemsCount;
    }

    @Override
    public void generate(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
            generator.writeObjectFieldStart("messages");
                if (hiddenItemsCount > 0) {
                    writeHiddenResultsMessage(generator);
                }
            generator.writeEndObject();
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

    private void writeHiddenResultsMessage(JsonGenerator generator) throws IOException {
        JacksonTools.nullSafeWriteStringField(generator, "hidden_results",
            String.format(
                "There %s %d additional result%s that you are not authorized to see.",
                hiddenItemsCount == 1 ? "is" : "are",
                hiddenItemsCount,
                hiddenItemsCount == 1 ? "" : "s"));
    }

    public static void writeFilters(JsonGenerator generator, ScheduledActivitiesReportFilters filters) throws IOException{
        if (filters.getActivityType()!= null) {
            JacksonTools.nullSafeWriteStringField(generator, "activity_type", filters.getActivityType().getName());
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
        if (filters.getCurrentStateMode() != null) {
            JacksonTools.nullSafeWriteStringField(generator, "state", filters.getCurrentStateMode().getDisplayName());
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
            JacksonTools.nullSafeWriteStringField(generator, "activity_name", row.getScheduledActivity().getActivity().getName());
            JacksonTools.nullSafeWriteStringField(generator, "activity_status", row.getScheduledActivity().getCurrentState().getMode().getDisplayName());
            JacksonTools.nullSafeWriteStringField(generator, "scheduled_date", getApiDateFormat().format(row.getScheduledActivity().getActualDate()));
            JacksonTools.nullSafeWriteStringField(generator, "ideal_date", getApiDateFormat().format(row.getScheduledActivity().getIdealDate()));
            JacksonTools.nullSafeWriteStringField(generator, "details", row.getScheduledActivity().getDetails());
            JacksonTools.nullSafeWriteStringField(generator, "condition", row.getScheduledActivity().getPlannedActivity().getCondition());

            if (!row.getScheduledActivity().getLabels().isEmpty()) {
                generator.writeArrayFieldStart("labels");
                for (String label : row.getScheduledActivity().getLabels()) {
                    generator.writeString(label);
                }
                generator.writeEndArray();
            }
            JacksonTools.nullSafeWriteStringField(generator, "subject_name", row.getSubject().getFullName());
            JacksonTools.nullSafeWriteStringField(generator, "person_id", row.getSubject().getPersonId());
            JacksonTools.nullSafeWriteStringField(generator, "study_subject_id", row.getStudySubjectId());
            JacksonTools.nullSafeWriteStringField(generator, "subject_coordinator_name", row.getSubjectCoordinatorName());
            JacksonTools.nullSafeWriteStringField(generator, "study", row.getStudy().getName());
            JacksonTools.nullSafeWriteStringField(generator, "site", row.getSite().getName());
        generator.writeEndObject();
    }
}

