/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.DateFormat;
import edu.northwestern.bioinformatics.studycalendar.tools.MutableRange;
import org.codehaus.jackson.JsonGenerator;

import java.io.IOException;
import java.util.Date;
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
        MutableRange<Date> actualActivityDate= filters.getActualActivityDate();
        if (actualActivityDate != null ) {
            if (actualActivityDate.getStart() != null) {
                JacksonTools.nullSafeWriteStringField(generator, "start_date", getApiDateFormat().format(actualActivityDate.getStart()));
            }
            if (actualActivityDate.getStop() != null) {
                JacksonTools.nullSafeWriteStringField(generator, "end_date", getApiDateFormat().format(actualActivityDate.getStop()));
            }
        }
        if (filters.getIdealDate() != null) {
            if (filters.getIdealDate().getStart() != null) {
                JacksonTools.nullSafeWriteStringField(generator, "start_ideal_date", getApiDateFormat().format(filters.getIdealDate().getStart()));
            }

            if (filters.getIdealDate().getStop() != null) {
                JacksonTools.nullSafeWriteStringField(generator, "end_ideal_date", getApiDateFormat().format(filters.getIdealDate().getStop()));
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
            if (row.getScheduledActivity().getCurrentState().getWithTime()) {
                JacksonTools.nullSafeWritePrimitiveField(generator, "activity_time", DateFormat.generateTimeFromDate(row.getScheduledActivity().getCurrentState().getDate()));
            }
            JacksonTools.nullSafeWriteStringField(generator, "activity_type", row.getScheduledActivity().getActivity().getType().getName());
            JacksonTools.nullSafeWriteStringField(generator, "activity_status", row.getScheduledActivity().getCurrentState().getMode().getDisplayName());
            JacksonTools.nullSafeWriteStringField(generator, "scheduled_date", getApiDateFormat().format(row.getScheduledActivity().getActualDate()));
            JacksonTools.nullSafeWriteStringField(generator, "last_change_reason", row.getScheduledActivity().getCurrentState().getReason());
            JacksonTools.nullSafeWriteStringField(generator, "ideal_date", getApiDateFormat().format(row.getScheduledActivity().getIdealDate()));
            JacksonTools.nullSafeWriteStringField(generator, "details", row.getScheduledActivity().getDetails());
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
            if (row.getScheduledActivity() != null && row.getScheduledActivity().getScheduledStudySegment() != null) {
                ScheduledStudySegment seg = row.getScheduledActivity().getScheduledStudySegment();
                generator.writeObjectFieldStart("scheduled_study_segment");
                    JacksonTools.nullSafeWriteStringField(generator, "grid_id", seg.getGridId());
                    JacksonTools.nullSafeWriteDateField(generator, "start_date", seg.getStartDate());
                    JacksonTools.nullSafeWritePrimitiveField(generator, "start_day", seg.getStartDay());
                generator.writeEndObject();
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

