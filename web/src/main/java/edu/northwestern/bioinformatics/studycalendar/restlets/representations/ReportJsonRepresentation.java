package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import static edu.northwestern.bioinformatics.studycalendar.restlets.AbstractPscResource.getApiDateFormat;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
import java.util.List;
import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;

/**
 * @author Nataliya Shurupova
 */
public class ReportJsonRepresentation extends StreamingJsonRepresentation  {
    private List<ScheduledActivitiesReportRow> allRows;
    private ScheduledActivitiesReportFilters filters;

    public ReportJsonRepresentation(ScheduledActivitiesReportFilters filters, List<ScheduledActivitiesReportRow> allRows) {
        this.filters = filters;
        this.allRows = allRows;
    }

    @Override
    public void generate(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
            generator.writeFieldName("filters");
            generator.writeStartObject();
                if (filters != null) {
                    createJSONFilters(generator, filters);
                }
            generator.writeEndObject();
            generator.writeFieldName("rows");
            generator.writeStartArray();
                if (allRows != null) {
                    for (ScheduledActivitiesReportRow row: allRows) {
                        createJSONRow(generator, row);
                    }
                }
            generator.writeEndArray();
        generator.writeEndObject();
    }

    public static void createJSONFilters(JsonGenerator generator, ScheduledActivitiesReportFilters filters) throws IOException{
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
        if (filters.getSubjectCoordinator() != null) {
            JacksonTools.nullSafeWriteStringField(generator, "responsible_user", filters.getSubjectCoordinator().getDisplayName());
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
    }

    public static void createJSONRow(JsonGenerator generator, ScheduledActivitiesReportRow row) throws IOException {
        generator.writeStartObject();
            JacksonTools.nullSafeWriteStringField(generator, "activity_name", row.getScheduledActivity().getActivity().getName());
            JacksonTools.nullSafeWriteStringField(generator, "activity_status", row.getScheduledActivity().getCurrentState().getMode().getDisplayName());
            JacksonTools.nullSafeWriteStringField(generator, "scheduled_date", getApiDateFormat().format(row.getScheduledActivity().getActualDate()));
            JacksonTools.nullSafeWriteStringField(generator, "ideal_date", getApiDateFormat().format(row.getScheduledActivity().getIdealDate()));
            JacksonTools.nullSafeWriteStringField(generator, "label", row.getLabel());
            JacksonTools.nullSafeWriteStringField(generator, "subject_name", row.getSubject().getFullName());
            JacksonTools.nullSafeWriteStringField(generator, "subject_id", row.getSubject().getPersonId());
            JacksonTools.nullSafeWriteStringField(generator, "subject_coorinator_name", row.getSubjectCoordinatorName());
            JacksonTools.nullSafeWriteStringField(generator, "study", row.getStudy().getName());
            JacksonTools.nullSafeWriteStringField(generator, "site", row.getSite().getName());
        generator.writeEndObject();
    }
}

