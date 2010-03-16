package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;

import java.util.List;
import java.util.ArrayList;
import java.io.StringWriter;

import junit.framework.TestCase;

/**
 * @author Nataliya Shurupova
 */
public class ReportCsvRepresentationTest extends TestCase {

    private List<ScheduledActivitiesReportRow> allRows;
    private ScheduledActivitiesReportFilters filters;

    private Study study;
    private Site site;
    private ScheduledActivitiesReportRow row1, row2;
    private static final String CSV_ROW_HEADER = "Activity Name,Activity Status,Scheduled Date,Ideal Date,Label,Subject Name,Subject Id,Subject Coorinator Name,study,site";

    public void setUp() throws Exception {
        super.setUp();

        site = Fixtures.createSite("Site for whatever study");
        study = createNamedInstance("Whatever Study", Study.class);
        ScheduledActivityState saState = new Scheduled();

        row1 = new ScheduledActivitiesReportRow();
        row1.setId(1001);
        row1.setLabel("label1");
        row1.setScheduledActivity(Fixtures.createScheduledActivity("activity1 ", 2009, 11, 12, saState));
        row1.setSubjectCoordinatorName("mayo mayo");
        row1.setSubject(Fixtures.createSubject("subject", "one"));
        row1.setSite(site);
        row1.setStudy(study);

        row2 = new ScheduledActivitiesReportRow();
        row2.setId(1002);
        row2.setLabel("label2");
        row2.setScheduledActivity(Fixtures.createScheduledActivity("activity2 ", 2009, 10, 15, saState));
        row2.setSubjectCoordinatorName("mayo mayo");
        row2.setSubject(Fixtures.createSubject("subject", "two"));
        row2.setSite(site);
        row2.setStudy(study);

        allRows = new ArrayList<ScheduledActivitiesReportRow>();
        allRows.add(row1);
        allRows.add(row2);

        filters = new ScheduledActivitiesReportFilters();
        filters.setSubjectCoordinator(Fixtures.createUser("mayo mayo"));
        filters.setCurrentStateMode(saState.getMode());
        filters.setActivityType(Fixtures.createActivityType("activityType"));
        filters.setCurrentStateMode(ScheduledActivityMode.SCHEDULED);
    }

    public void testCSV() throws Exception {
        ReportCsvRepresentation rr = new ReportCsvRepresentation(allRows, ',');
        String csvDocument = rr.generateDocumentString(new StringWriter(), ',');
        String[] rows = csvDocument.split("\n");
        assertEquals("Wrong amount of filter rows", CSV_ROW_HEADER, rows[0]);
        String csvRepForActivityOne = "activity1,Scheduled,2009-12-12,2009-12-10,label1,subject one,,mayo mayo,Whatever Study,Site for whatever study";
        String csvRepForActivityTwo = "activity2,Scheduled,2009-11-15,2009-11-13,label2,subject two,,mayo mayo,Whatever Study,Site for whatever study";
        assertEquals("Wrong description for row1 ", csvRepForActivityOne, rows[1]);
        assertEquals("Wrong description for row2 ", csvRepForActivityTwo, rows[2]);
        assertEquals("There are more rows in the document ", 3, rows.length);
    }

}