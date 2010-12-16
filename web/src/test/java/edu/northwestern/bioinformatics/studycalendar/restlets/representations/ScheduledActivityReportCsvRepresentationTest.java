package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import junit.framework.TestCase;
import org.restlet.representation.Representation;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;

/**
 * @author Nataliya Shurupova
 */
public class ScheduledActivityReportCsvRepresentationTest extends TestCase {
    private static final String EXPECTED_CSV_ROW_HEADER = "Activity Name,Activity Status,Scheduled Date,Details,Condition,Labels,Ideal Date,Subject Name,Patient Id,Study Subject Id,Responsible User,Study,Site";

    private List<ScheduledActivitiesReportRow> allRows;
    private ScheduledActivitiesReportRow row2;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Site site = Fixtures.createSite("Site for whatever study");
        Study study = createNamedInstance("Whatever Study", Study.class);
        ScheduledActivityState saState = new Scheduled();

        ScheduledActivitiesReportRow row1 = new ScheduledActivitiesReportRow();
        row1.setId(1001);
        ScheduledActivity activity1 = Fixtures.createScheduledActivity("activity1 ", 2009, 11, 12, saState);
        SortedSet<String> labels1 = new TreeSet<String>();
        labels1.add("label1");
        activity1.setLabels(labels1);
        row1.setScheduledActivity(activity1);
        row1.setSubject(Fixtures.createSubject("subject", "one"));
        row1.setSite(site);
        row1.setStudy(study);
        row1.setResponsibleUser(AuthorizationObjectFactory.createCsmUser("wilma"));

        row2 = new ScheduledActivitiesReportRow();
        row2.setId(1002);
        ScheduledActivity activity2 = Fixtures.createScheduledActivity("activity2 ", 2009, 10, 15, saState);
        SortedSet<String> labels2 = new TreeSet<String>();
        labels2.add("label2");
        activity2.setLabels(labels2);
        row2.setScheduledActivity(activity2);
        row2.setSubject(Fixtures.createSubject("subject", "two"));
        row2.setSite(site);
        row2.setStudy(study);

        allRows = new ArrayList<ScheduledActivitiesReportRow>();
        allRows.add(row1);
        allRows.add(row2);
    }

    public void testCSV() throws Exception {
        String csvDocument = actual();
        String[] rows = csvDocument.split("\n");
        assertEquals("Wrong header", EXPECTED_CSV_ROW_HEADER, rows[0]);
        String csvRepForActivityOne = "activity1,Scheduled,2009-12-12,,,label1,2009-12-10,subject one,,,wilma,Whatever Study,Site for whatever study";
        String csvRepForActivityTwo = "activity2,Scheduled,2009-11-15,,,label2,2009-11-13,subject two,,,,Whatever Study,Site for whatever study";
        assertEquals("Wrong content for row1", csvRepForActivityOne, rows[1]);
        assertEquals("Wrong content for row2", csvRepForActivityTwo, rows[2]);
        assertEquals("There are too many rows in the document", 3, rows.length);
    }

    public void testCSVForLabelsWithCommas() throws Exception {
        row2.getScheduledActivity().getLabels().remove("label2");
        row2.getScheduledActivity().getLabels().add("labelA, labelB, labelC");
        String csvDocument = actual();
        String[] rows = csvDocument.split("\n");
        assertEquals("Wrong header", EXPECTED_CSV_ROW_HEADER, rows[0]);
        String csvRepForActivityTwo = "activity2,Scheduled,2009-11-15,,,\"labelA, labelB, labelC\",2009-11-13,subject two,,,,Whatever Study,Site for whatever study";
        assertEquals("Wrong implementation of row2", csvRepForActivityTwo, rows[2]);
        assertEquals("There are too many rows in the document", 3, rows.length);
    }

    public void testCSVForLabelsWithQuotes() throws Exception {
        row2.getScheduledActivity().getLabels().remove("label2");
        row2.getScheduledActivity().getLabels().add("\"labelA\", \"labelB\", \"labelC\"");
        String csvDocument = actual();
        String[] rows = csvDocument.split("\n");
        assertEquals("Wrong header", EXPECTED_CSV_ROW_HEADER, rows[0]);
        String csvRepForActivityTwo = "activity2,Scheduled,2009-11-15,,,\"\"\"labelA\"\", \"\"labelB\"\", \"\"labelC\"\"\",2009-11-13,subject two,,,,Whatever Study,Site for whatever study";
        assertEquals("Wrong implementation of row2", csvRepForActivityTwo, rows[2]);
        assertEquals("There are too many rows in the document", 3, rows.length);
    }

    private String actual() throws IOException {
        ScheduledActivityReportCsvRepresentation rr
            = new ScheduledActivityReportCsvRepresentation(allRows, ',');
        return writeAndReturnContents(rr);
    }

    private String writeAndReturnContents(Representation out) throws IOException {
        StringWriter sw = new StringWriter();
        out.write(sw);
        sw.close();
        return sw.toString();
    }
}