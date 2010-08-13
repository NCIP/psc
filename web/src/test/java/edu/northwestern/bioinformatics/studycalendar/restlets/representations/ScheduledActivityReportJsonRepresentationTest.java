package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;

/**
 * @author Nataliya Shurupova
 * @author Rhett Sutphin
 */
public class ScheduledActivityReportJsonRepresentationTest extends JsonRepresentationTestCase {
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
        row1.setSubject(setGridId("GRID-1",
            createSubject("1111", "subject", "one", DateTools.createDate(1950, 4, 5))));
        row1.setSite(site);
        row1.setStudy(study);
        row1.setResponsibleUser(AuthorizationObjectFactory.createCsmUser("betsy"));

        ScheduledActivitiesReportRow row2 = new ScheduledActivitiesReportRow();
        row2.setId(1002);
        row2.setScheduledActivity(
            addLabels(createScheduledActivity("activity2", 2009, 10, 15, saState), "L2"));
        row2.setSubject(setGridId("GRID-2",
            createSubject("2222", "subject", "two", DateTools.createDate(1950, 4, 5))));
        row2.setSite(site);
        row2.setStudy(study);

        allRows = new ArrayList<ScheduledActivitiesReportRow>();
        allRows.add(row1);
        allRows.add(row2);

        filters = new ScheduledActivitiesReportFilters();
    }

    public void testFilterKey() throws Exception {
        JSONObject actual = writeAndParseObject(actual());
        assertNotNull("Missing key filters", actual.getJSONObject("filters"));
    }

    public void testMissingFiltersValues() throws Exception {
        JSONObject actual = writeAndParseObject(new ScheduledActivityReportJsonRepresentation(null, allRows));
        assertNotNull("Missing key filters", actual.getJSONObject("filters"));
        JSONObject filters = actual.getJSONObject("filters");
        assertEquals("Filter is empty", 0, filters.length());
    }

    public void testAllRowsIncluded() throws Exception {
        JSONObject actual = writeAndParseObject(actual());
        JSONArray rows = actual.getJSONArray("rows");
        assertEquals("Wrong number of rows", 2, rows.length());
    }

    public void testReturnedFiltersIncludeStudy() throws Exception {
        filters.setStudyAssignedIdentifier("AT AT");
        assertEquals("Wrong value", "AT AT", writeAndGetFilters().optString("study"));
    }

    public void testReturnedFiltersIncludeCurrentStateMode() throws Exception {
        filters.setCurrentStateMode(ScheduledActivityMode.MISSED);
        assertEquals("Wrong value", "Missed", writeAndGetFilters().optString("state"));
    }

    public void testReturnedFiltersIncludeSiteName() throws Exception {
        filters.setSiteName("France");
        assertEquals("Wrong value", "France", writeAndGetFilters().optString("site"));
    }

    public void testReturnedFiltersIncludeStartDate() throws Exception {
        filters.getActualActivityDate().setStart(DateTools.createDate(2009, Calendar.APRIL, 8));
        assertEquals("Wrong value", "2009-04-08", writeAndGetFilters().optString("start_date"));
    }

    public void testReturnedFiltersIncludeEndDate() throws Exception {
        filters.getActualActivityDate().setStop(DateTools.createDate(2009, Calendar.APRIL, 19));
        assertEquals("Wrong value", "2009-04-19", writeAndGetFilters().optString("end_date"));
    }

    public void testReturnedFiltersIncludeActivityTypes() throws Exception {
        filters.setActivityTypes(Arrays.asList(
            createActivityType("Measure"), createActivityType("Intervention")));
        JSONArray actualTypes = writeAndGetFilters().optJSONArray("activity_types");
        assertNotNull(actualTypes);
        assertEquals("Wrong number of results", 2, actualTypes.length());
        assertEquals("Wrong value 0", "Measure", actualTypes.get(0));
        assertEquals("Wrong value 1", "Intervention", actualTypes.get(1));
    }

    public void testReturnedFiltersIncludeLabel() throws Exception {
        filters.setLabel("THL");
        assertEquals("Wrong value", "THL", writeAndGetFilters().optString("label"));
    }

    public void testReturnedFiltersIncludeResponsibleUsername() throws Exception {
        filters.setResponsibleUser(AuthorizationObjectFactory.createCsmUser(7, "zap"));
        assertEquals("Wrong value", "zap", writeAndGetFilters().optString("responsible_user"));
    }

    public void testReturnedFiltersIncludePersonId() throws Exception {
        filters.setPersonId("123321");
        assertEquals("Wrong value", "123321", writeAndGetFilters().optString("person_id"));
    }

    public void testReturnedFiltersOmitsKeysForUnsetFilters() throws Exception {
        assertEquals("Should be no filters set", 0, writeAndGetFilters().length());
    }

    public void testIdealDateIncludedInData() throws Exception {
        assertEquals("Wrong date", "2009-12-10", writeAndGetRow(0).getString("ideal_date"));
    }

    public void testScheduledDateIncludedInData() throws Exception {
        assertEquals("Wrong date", "2009-12-12", writeAndGetRow(0).getString("scheduled_date"));
    }

    public void testStudyIncludedInData() throws Exception {
        assertEquals("Wrong study", "Whatever Study", writeAndGetRow(0).getString("study"));
    }

    public void testSiteIncludedInData() throws Exception {
        assertEquals("Wrong site", "Mayo", writeAndGetRow(0).getString("site"));
    }

    public void testLabelsIncludedInData() throws Exception {
        JSONArray actual = writeAndGetRow(0).optJSONArray("labels");
        assertNotNull("No labels present", actual);
        assertEquals("Wrong number of labels", 2, actual.length());
        assertEquals("Wrong first label", "L1", actual.getString(0));
        assertEquals("Wrong second label", "L2", actual.getString(1));
    }

    public void testActivityNameIncludedInData() throws Exception {
        assertEquals("Wrong activity name", "activity1",
            writeAndGetRow(0).getString("activity_name"));
    }

    public void testResponsibleUserUsernameIncludedInData() throws Exception {
        assertEquals("Wrong user", "betsy", writeAndGetRow(0).opt("responsible_user"));
    }

    public void testResponsibleUserUsernameNotIncludedInDataWhenNoResponsibleUser() throws Exception {
        assertNull("Should have no user", writeAndGetRow(1).opt("responsible_user"));
    }

    public void testSubjectNameIncludedInData() throws Exception {
        assertEquals("Wrong subject name", "subject one",
            writeAndGetRow(0).getJSONObject("subject").optString("name"));
    }

    public void testSubjectGridIdIncludedInData() throws Exception {
        assertEquals("Wrong subject grid ID", "GRID-1",
            writeAndGetRow(0).getJSONObject("subject").optString("grid_id"));
    }

    public void testSubjectPersonIdIncludedInData() throws Exception {
        assertEquals("Wrong person ID", "2222",
            writeAndGetRow(1).getJSONObject("subject").optString("person_id"));
    }

    private ScheduledActivityReportJsonRepresentation actual() {
        return new ScheduledActivityReportJsonRepresentation(filters, allRows);
    }

    private JSONObject writeAndGetFilters() throws IOException, JSONException {
        JSONObject report = writeAndParseObject(actual());
        return report.getJSONObject("filters");
    }

    private JSONObject writeAndGetRow(int rowIndex) throws IOException, JSONException {
        JSONObject report = writeAndParseObject(actual());
        return report.getJSONArray("rows").getJSONObject(rowIndex);
    }
}
