package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import com.csvreader.CsvWriter;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import edu.northwestern.bioinformatics.studycalendar.restlets.PscMetadataService;
import org.apache.commons.lang.StringUtils;
import org.restlet.resource.OutputRepresentation;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.restlets.AbstractPscResource.getApiDateFormat;

/**
 * @author Nataliya Shurupova
 */
public class ReportCsvRepresentation extends OutputRepresentation {

    private List<ScheduledActivitiesReportRow> allRows;
    private char delimeter;
    private static final String[] ROW_COLUMNS = new String[] {"Activity Name","Activity Status","Scheduled Date","Details","Condition","Labels","Ideal Date",
                "Subject Name","Patient Id","Study Subject Id","Subject Coorinator Name","Study","Site"};

    public ReportCsvRepresentation(List<ScheduledActivitiesReportRow> allRows, char delimeter) {
        super(PscMetadataService.TEXT_CSV);
        this.allRows = allRows;
        this.delimeter = delimeter;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        StringWriter out = new StringWriter();
        String response = generateDocumentString(out, delimeter);
        byte[] array = response.getBytes();
        outputStream.write(array);
        outputStream.flush();
    }

    public String generateDocumentString(StringWriter stringWriter, char delimiter) {
        CsvWriter writer = new CsvWriter(stringWriter, delimiter);
        try {
            writer.writeRecord(ROW_COLUMNS);
            if (allRows != null) {
                for (ScheduledActivitiesReportRow row: allRows) {
                    createCSVRow(writer, row);
                }
            }
            writer.close();
        } catch (IOException e) {
            throw new StudyCalendarSystemException("Error when building CSV in memory", e);
        }

        return stringWriter.toString();
    }

    public void createCSVRow(CsvWriter writer, ScheduledActivitiesReportRow row) throws IOException {
       writer.writeRecord(new String[] {
           row.getScheduledActivity().getActivity().getName(),
           row.getScheduledActivity().getCurrentState().getMode().getDisplayName(),
           getApiDateFormat().format(row.getScheduledActivity().getActualDate()),
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
