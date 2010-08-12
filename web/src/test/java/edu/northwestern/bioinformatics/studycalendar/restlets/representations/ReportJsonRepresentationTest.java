package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;

/**
 * @author Nataliya Shurupova
 */
public class ReportJsonRepresentationTest extends JsonRepresentationTestCase {

    private List<ScheduledActivitiesReportRow> allRows;
    private ScheduledActivitiesReportFilters filters;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Site site = createSite("Mayo");
        Study study = createNamedInstance("Whatever Study", Study.class);
        ScheduledActivityState saState = new Scheduled();

        ScheduledActivitiesReportRow row1 = new ScheduledActivitiesReportRow();
        row1.setId(1001);
        row1.setScheduledActivity(
            addLabels(createScheduledActivity("activity1", 2009, 11, 12, saState), "L1", "L2"));
        row1.setSubjectCoordinatorName("mayo mayo");
        row1.setSubject(createSubject("subject", "one"));
        row1.setSite(site);
        row1.setStudy(study);

        ScheduledActivitiesReportRow row2 = new ScheduledActivitiesReportRow();
        row2.setId(1002);
        row2.setScheduledActivity(
            addLabels(createScheduledActivity("activity2", 2009, 10, 15, saState), "L2"));
        row2.setSubjectCoordinatorName("mayo mayo");
        row2.setSubject(createSubject("subject", "two"));
        row2.setSite(site);
        row2.setStudy(study);

        allRows = new ArrayList<ScheduledActivitiesReportRow>();
        allRows.add(row1);
        allRows.add(row2);

        filters = new ScheduledActivitiesReportFilters();
        filters.setSubjectCoordinator(createUser("mayo mayo"));
        filters.setCurrentStateMode(saState.getMode());
        filters.setActivityType(createActivityType("activityType"));
        filters.setCurrentStateMode(ScheduledActivityMode.SCHEDULED);
    }

    public void testFilterKey() throws Exception {
        JSONObject actual = writeAndParseObject(actual());
        assertNotNull("Missing key filters", actual.getJSONObject("filters"));
    }

    public void testMissingFiltersValues() throws Exception {
        JSONObject actual = writeAndParseObject(new ReportJsonRepresentation(null, allRows, false));
        assertNotNull("Missing key filters", actual.getJSONObject("filters"));
        JSONObject filters = actual.getJSONObject("filters");
        assertEquals("Filter is empty", 0, filters.length());
    }

    public void testAllRowsIncluded() throws Exception {
        JSONObject actual = writeAndParseObject(actual());
        JSONArray rows = actual.getJSONArray("rows");
        assertEquals("Wrong number of rows", 2, rows.length());
    }

    public void testFilterValue() throws Exception {
        JSONObject actual = writeAndParseObject(actual());
        assertEquals("activity_type has different value ", "activityType",
            actual.getJSONObject("filters").get("activity_type"));
        assertEquals("Responsible_user has different value", "mayo mayo",
            actual.getJSONObject("filters").get("responsible_user"));
        assertEquals("State has different value", "Scheduled",
            actual.getJSONObject("filters").get("state"));
    }

    private ReportJsonRepresentation actual() {
        return new ReportJsonRepresentation(filters, allRows, false);
    }

    public void testIdealDateIncluded() throws Exception {
        assertEquals("Wrong date", "2009-12-10", writeAndGetRow(0).getString("ideal_date"));
    }

    public void testScheduledDateIncluded() throws Exception {
        assertEquals("Wrong date", "2009-12-12", writeAndGetRow(0).getString("scheduled_date"));
    }

    public void testStudyIncluded() throws Exception {
        assertEquals("Wrong study", "Whatever Study", writeAndGetRow(0).getString("study"));
    }

    public void testSiteIncluded() throws Exception {
        assertEquals("Wrong site", "Mayo", writeAndGetRow(0).getString("site"));
    }

    public void testLabelCSVIncluded() throws Exception {
        assertEquals("Wrong labels", "L1, L2", writeAndGetRow(0).getString("label"));
    }

    public void testActivityNameIncluded() throws Exception {
        assertEquals("Wrong activity name", "activity1",
            writeAndGetRow(0).getString("activity_name"));
    }

    public void testSubjectCoordinatorNameIncluded() throws Exception {
        assertEquals("Wrong coordinator", "mayo mayo",
            writeAndGetRow(0).getString("subject_coordinator_name"));
    }

    public void testSubjectNameIncluded() throws Exception {
        assertEquals("Wrong subject name", "subject one",
            writeAndGetRow(0).getString("subject_name"));
    }

    private JSONObject writeAndGetRow(int rowIndex) throws IOException, JSONException {
        JSONObject report = writeAndParseObject(actual());
        return report.getJSONArray("rows").getJSONObject(rowIndex);
    }
}
