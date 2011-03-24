package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import com.csvreader.CsvWriter;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import edu.northwestern.bioinformatics.studycalendar.restlets.PscMetadataService;
import org.apache.commons.lang.StringUtils;
import org.restlet.representation.OutputRepresentation;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.restlets.AbstractPscResource.getApiDateFormat;

/**
 * @author Nataliya Shurupova
 */
public class ScheduledActivityReportCsvRepresentation extends OutputRepresentation {
    private List<ScheduledActivitiesReportRow> allRows;
    private char delimiter;
    private static final String[] HEADERS = new String[] {
        "Activity Name", "Activity Status", "Scheduled Date", "Last Change Reason", "Details", "Condition", "Labels",
        "Ideal Date", "Subject Name", "Patient Id", "Study Subject Id", "Responsible User",
        "Study", "Site"
    };

    public ScheduledActivityReportCsvRepresentation(
        List<ScheduledActivitiesReportRow> allRows, char delimiter
    ) {
        super(PscMetadataService.TEXT_CSV);
        this.allRows = allRows;
        this.delimiter = delimiter;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        CsvWriter writer = new CsvWriter(outputStream, delimiter, Charset.forName("UTF-8"));
        try {
            writer.writeRecord(HEADERS);
            if (allRows != null) {
                for (ScheduledActivitiesReportRow row: allRows) {
                    createCSVRow(writer, row);
                }
            }
            writer.close();
        } catch (IOException e) {
            throw new StudyCalendarSystemException("Error when building CSV in memory", e);
        }
    }

    public void createCSVRow(CsvWriter writer, ScheduledActivitiesReportRow row) throws IOException {
       writer.writeRecord(new String[] {
           row.getScheduledActivity().getActivity().getName(),
           row.getScheduledActivity().getCurrentState().getMode().getDisplayName(),
           getApiDateFormat().format(row.getScheduledActivity().getActualDate()),
           row.getScheduledActivity().getCurrentState().getReason(),
           row.getScheduledActivity().getDetails(),
           row.getScheduledActivity().getPlannedActivity().getCondition(),
           StringUtils.join(row.getScheduledActivity().getLabels().iterator(), ' '),
           getApiDateFormat().format(row.getScheduledActivity().getIdealDate()),
           row.getSubject().getFullName(),
           row.getSubject().getPersonId(),
           row.getStudySubjectId(),
           row.getResponsibleUser() == null ? null : row.getResponsibleUser().getLoginName(),
           row.getStudy().getName(),
           row.getSite().getName()
       });
    }
}
