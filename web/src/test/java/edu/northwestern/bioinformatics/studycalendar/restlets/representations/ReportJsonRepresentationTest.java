package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;

/**
 * @author Nataliya Shurupova
 */
public class ReportJsonRepresentationTest extends JsonRepresentationTestCase {

    private List<ScheduledActivitiesReportRow> allRows;
    private ScheduledActivitiesReportFilters filters;

    private Study study;
    private Site site;
    private ScheduledActivitiesReportRow row1, row2;

    public void setUp() throws Exception {
        super.setUp();

        site = Fixtures.createSite("Site for whatever study");
        study = createNamedInstance("Whatever Study", Study.class);
        ScheduledActivityState saState = new Scheduled();

        row1 = new ScheduledActivitiesReportRow();
        row1.setId(1001);
        ScheduledActivity activity1 = Fixtures.createScheduledActivity("activity1 ", 2009, 11, 12, saState);
        SortedSet<String> labels1 = new TreeSet<String>();
        labels1.add("label1");
        activity1.setLabels(labels1);
        row1.setScheduledActivity(activity1);
        row1.setSubjectCoordinatorName("mayo mayo");
        row1.setSubject(Fixtures.createSubject("subject", "one"));
        row1.setSite(site);
        row1.setStudy(study);

        row2 = new ScheduledActivitiesReportRow();
        row2.setId(1002);
        ScheduledActivity activity2 = Fixtures.createScheduledActivity("activity2 ", 2009, 10, 15, saState);
        SortedSet<String> labels2 = new TreeSet<String>();
        labels2.add("label2");
        activity2.setLabels(labels2);
        row2.setScheduledActivity(activity2);
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

    public void testFilterKey() throws Exception {
        JSONObject actual = writeAndParseObject(new ReportJsonRepresentation(filters, allRows, false));
        assertNotNull("Missing key filters", actual.getJSONObject("filters"));
    }

    public void testMissingFiltersValues() throws Exception {
        JSONObject actual = writeAndParseObject(new ReportJsonRepresentation(null, allRows, false));
        assertNotNull("Missing key filters", actual.getJSONObject("filters"));
        JSONObject filters = actual.getJSONObject("filters");
        assertTrue("Filter is empty", filters.length()==0);
    }

    public void testRowsKey() throws Exception {
        JSONObject actual = writeAndParseObject(new ReportJsonRepresentation(filters, allRows, false));
        assertNotNull("Missing key rows", actual.getJSONArray("rows"));
    }

    public void testFilterValue() throws Exception {
        JSONObject actual = writeAndParseObject(new ReportJsonRepresentation(filters, allRows, false));
        assertNotNull("Activity_type doesn't exist ", actual.getJSONObject("filters").get("activity_type"));
        assertEquals("activity_type has different value ", "activityType", actual.getJSONObject("filters").get("activity_type"));
        assertNotNull("Responsible_user doesn't exist", actual.getJSONObject("filters").get("responsible_user"));
        assertEquals("Responsible_user has different value", "mayo mayo", actual.getJSONObject("filters").get("responsible_user"));
        assertNotNull("State doesn't exist", actual.getJSONObject("filters").get("state"));
        assertEquals("State has different value", "Scheduled", actual.getJSONObject("filters").get("state"));
    }

    public void testRowsValueLength() throws Exception {
        JSONObject actual = writeAndParseObject(new ReportJsonRepresentation(filters, allRows, false));
        JSONArray rows = actual.getJSONArray("rows");
        assertTrue("Rows values are not = 2 ", rows.length()==2);
    }

    public void testRowsFirstValues() throws Exception {
        JSONObject actual = writeAndParseObject(new ReportJsonRepresentation(filters, allRows, false));
        JSONArray rows = actual.getJSONArray("rows");
        JSONObject first = (JSONObject) rows.get(0);
        assertNotNull("Ideal_date  doesn't exist", first.get("ideal_date"));
        assertEquals("Value for ideal_date is incorrect", "2009-12-10", first.get("ideal_date"));
        
        assertNotNull("Scheduled_date  doesn't exist", first.get("scheduled_date"));
        assertEquals("Value for scheduled_date is incorrect", "2009-12-12", first.get("scheduled_date"));

        assertNotNull("Study doesn't exist", first.get("study"));
        assertEquals("Value for study is incorrect", study.getName(), first.get("study"));

        assertNotNull("Site doesn't exist ", first.get("site"));
        assertEquals("Value for site is incorrect ", site.getName(), first.get("site"));

        assertNotNull("Label doesn't exist ", first.get("label"));
        assertEquals("Value for label is incorrect ", row1.getScheduledActivity().getLabels().first(), first.get("label"));

        assertNotNull("Activity_name doesn't exist ", first.get("activity_name"));
        assertEquals("Value for activity_name is incorrect ", row1.getScheduledActivity().getActivity().getName(), first.get("activity_name"));

        assertNotNull("Subject_coorinator_name doesn't exist ", first.get("subject_coordinator_name"));
        assertEquals("Value for subject_coorinator_name is incorrect ", row1.getSubjectCoordinatorName(), first.get("subject_coordinator_name"));

        assertNotNull("Subject_name doesn't exist ", first.get("subject_name"));
        assertEquals("Value for subject_name is incorrect ", row1.getSubject().getFullName(), first.get("subject_name"));
    }


    public void testRowsSecondValues() throws Exception {
        JSONObject actual = writeAndParseObject(new ReportJsonRepresentation(filters, allRows, false));
        JSONArray rows = actual.getJSONArray("rows");
        JSONObject first = (JSONObject) rows.get(1);
        assertNotNull("Ideal_date  doesn't exist", first.get("ideal_date"));
        assertEquals("Value for ideal_date is incorrect", "2009-11-13", first.get("ideal_date"));

        assertNotNull("Scheduled_date  doesn't exist", first.get("scheduled_date"));
        assertEquals("Value for scheduled_date is incorrect", "2009-11-15", first.get("scheduled_date"));

        assertNotNull("Study doesn't exist", first.get("study"));
        assertEquals("Value for study is incorrect", study.getName(), first.get("study"));

        assertNotNull("Site doesn't exist ", first.get("site"));
        assertEquals("Value for site is incorrect ", site.getName(), first.get("site"));

        assertNotNull("Label doesn't exist ", first.get("label"));
        assertEquals("Value for label is incorrect ", row2.getScheduledActivity().getLabels().first(), first.get("label"));

        assertNotNull("Activity_name doesn't exist ", first.get("activity_name"));
        assertEquals("Value for activity_name is incorrect ", row2.getScheduledActivity().getActivity().getName(), first.get("activity_name"));

        assertNotNull("Subject_coorinator_name doesn't exist ", first.get("subject_coorinator_name"));
        assertEquals("Value for subject_coorinator_name is incorrect ", row2.getSubjectCoordinatorName(), first.get("subject_coorinator_name"));

        assertNotNull("Subject_name doesn't exist ", first.get("subject_name"));
        assertEquals("Value for subject_name is incorrect ", row2.getSubject().getFullName(), first.get("subject_name"));
    }
}
