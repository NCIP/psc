package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.ReportService;
import org.easymock.IArgumentMatcher;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Status;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static org.easymock.EasyMock.expect;

/**
 * @author Nataliya Shurupova
 */
public class ReportResourceTest extends AuthorizedResourceTestCase<ReportsResource> {
    private ActivityTypeDao activityTypeDao;
    private ReportService reportService;

    private ScheduledActivitiesReportFilters filters;
    private List<ScheduledActivitiesReportRow> rows;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        reportService = registerMockFor(ReportService.class);
        activityTypeDao = registerDaoMockFor(ActivityTypeDao.class);
        filters = new ScheduledActivitiesReportFilters();

        ScheduledActivitiesReportRow row1 = new ScheduledActivitiesReportRow(); row1.setId(1001);
        ScheduledActivitiesReportRow row2 = new ScheduledActivitiesReportRow(); row2.setId(1002);

        rows = Arrays.asList(row1, row2);

        SecurityContextHolderTestHelper.setUserAndReturnMembership("jo", PscRole.DATA_READER);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected ReportsResource createAuthorizedResource() {
        ReportsResource resource = new ReportsResource();
        resource.setActivityTypeDao(activityTypeDao);
        resource.setReportService(reportService);
        return resource;
    }

    public void testGetAllowed() throws Exception {
        assertAllowedMethods("GET");
    }

    public void testGetWithAuthorizedRoles() {
        assertRolesAllowedForMethod(Method.GET,
            STUDY_SUBJECT_CALENDAR_MANAGER,
            STUDY_TEAM_ADMINISTRATOR,
            DATA_READER);
    }

    public void test200ForSupportedCSVMediaType() throws Exception {
        FilterParameters.STATE.putIn(request, "1");
        filters = getResource().getFilters();
        expect(reportService.searchScheduledActivities(eqFilters(filters))).andReturn(rows);
        request.getResourceRef().addQueryParameter("state", "1");
        makeRequestType(PscMetadataService.TEXT_CSV);
        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void test200ForSupportedJSONMediaType() throws Exception {
        FilterParameters.STATE.putIn(request, "1");
        filters = getResource().getFilters();
        expect(reportService.searchScheduledActivities(eqFilters(filters))).andReturn(rows);
        request.getResourceRef().addQueryParameter("state", "1");
        makeRequestType(MediaType.APPLICATION_JSON);
        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testGetFilterForRandOfDates() throws Exception {
        String startDateString = "2010-03-01";
        String endDateString = "2010-03-08";
        Date startDate = ReportsResource.getApiDateFormat().parse(startDateString);
        Date endDate = ReportsResource.getApiDateFormat().parse(endDateString);
        FilterParameters.START_DATE.putIn(request, startDateString);
        FilterParameters.END_DATE.putIn(request, endDateString);
        ScheduledActivitiesReportFilters actualFilter = getResource().getFilters();
        assertNotNull("Actual Filter is Null", actualFilter);
        assertNotNull("Filter doesn't contain the actual activity date ", actualFilter.getActualActivityDate());
        assertEquals("Actual Filter doesn't contain end date", startDate, actualFilter.getActualActivityDate().getStart());
        assertEquals("Actual Filter doesn't contain end date", endDate, actualFilter.getActualActivityDate().getStop());
    }

    public void testGetFilterForEndDate() throws Exception {
        String endDateString = "2010-03-08";
        Date endDate = ReportsResource.getApiDateFormat().parse(endDateString);
        FilterParameters.END_DATE.putIn(request, endDateString);
        ScheduledActivitiesReportFilters actualFilter = getResource().getFilters();
        assertNotNull("Actual Filter is Null", actualFilter);
        assertEquals("Actual Filter doesn't contain end date", endDate, actualFilter.getActualActivityDate().getStop());
        assertNull("Actual Filter has label", actualFilter.getLabel());
        assertNull("Actual Filter has actual activity start date", actualFilter.getActualActivityDate().getStart());
        assertNull("Actual Filter has site name", actualFilter.getSiteName());
        assertNull("Actual Filter has study assigned identifier", actualFilter.getStudyAssignedIdentifier());
        assertNull("Actual Filter has activity type", actualFilter.getActivityType());
        assertNull("Actual Filter has subject coordinator", actualFilter.getSubjectCoordinator());
        assertNull("Actual Filter has activity state", actualFilter.getCurrentStateMode());
    }

    public void testGetFilterForStartDate() throws Exception {
        String startDateString = "2010-03-01";
        Date startDate = ReportsResource.getApiDateFormat().parse(startDateString);
        FilterParameters.START_DATE.putIn(request, startDateString);
        ScheduledActivitiesReportFilters actualFilter = getResource().getFilters();
        assertNotNull("Actual Filter is Null", actualFilter);
        assertEquals("Actual Filter doesn't contain startDate", startDate, actualFilter.getActualActivityDate().getStart());
        assertNull("Actual Filter has label", actualFilter.getLabel());
        assertNull("Actual Filter has actual activity end date", actualFilter.getActualActivityDate().getStop());
        assertNull("Actual Filter has site name", actualFilter.getSiteName());
        assertNull("Actual Filter has study assigned identifier", actualFilter.getStudyAssignedIdentifier());
        assertNull("Actual Filter has activity type", actualFilter.getActivityType());
        assertNull("Actual Filter has subject coordinator", actualFilter.getSubjectCoordinator());
        assertNull("Actual Filter has activity state", actualFilter.getCurrentStateMode());
    }

    public void testGetFilterForLabel() throws Exception {
        String label = "labelA";
        FilterParameters.LABEL.putIn(request, label);
        ScheduledActivitiesReportFilters actualFilter = getResource().getFilters();
        assertNotNull("Actual Filter is Null", actualFilter);
        assertEquals("Actual Filter doesn't contain label", label, actualFilter.getLabel());
        assertNull("Actual Filter has actual activity start date", actualFilter.getActualActivityDate().getStart());
        assertNull("Actual Filter has actual activity end date", actualFilter.getActualActivityDate().getStop());
        assertNull("Actual Filter has site name", actualFilter.getSiteName());
        assertNull("Actual Filter has study assigned identifier", actualFilter.getStudyAssignedIdentifier());
        assertNull("Actual Filter has activity type", actualFilter.getActivityType());
        assertNull("Actual Filter has subject coordinator", actualFilter.getSubjectCoordinator());
        assertNull("Actual Filter has activity state", actualFilter.getCurrentStateMode());
    }
    /* TODO: #1111
    public void testGetFilterForResponsibleUser() throws Exception {
        String responsibleUserId = "2";
        User responsibleUser = Fixtures.createUser("mayo mayo");
        responsibleUser.setId(new Integer(responsibleUserId));
        FilterParameters.RESPONSIBLE_USER.putIn(request, responsibleUserId);
        expect(userDao.getById(new Integer(responsibleUserId))).andReturn(responsibleUser);
        replayMocks();
            ScheduledActivitiesReportFilters actualFilter = getResource().getFilters();
        verifyMocks();
        assertNotNull("Actual Filter is Null", actualFilter);
        assertSame("Actual Filter doesn't contain responsible user", responsibleUser, actualFilter.getSubjectCoordinator());
        assertNull("Actual Filter has actual activity start date", actualFilter.getActualActivityDate().getStart());
        assertNull("Actual Filter has actual activity end date", actualFilter.getActualActivityDate().getStop());
        assertNull("Actual Filter has site name", actualFilter.getSiteName());
        assertNull("Actual Filter has study assigned identifier", actualFilter.getStudyAssignedIdentifier());
        assertNull("Actual Filter has activity type", actualFilter.getActivityType());
        assertNull("Actual Filter has label", actualFilter.getLabel());
        assertNull("Actual Filter has activity state", actualFilter.getCurrentStateMode());
    }
    */
    public void testGetFilterForCurrentState() throws Exception {
        String currentState = "1";
        ScheduledActivityMode activityState = ScheduledActivityMode.SCHEDULED;
        FilterParameters.STATE.putIn(request, currentState);
        ScheduledActivitiesReportFilters actualFilter = getResource().getFilters();
        assertNotNull("Actual Filter is Null", actualFilter);
        assertSame("Actual Filter doesn't contain activity state", activityState, actualFilter.getCurrentStateMode());
        assertNull("Actual Filter has actual activity start date", actualFilter.getActualActivityDate().getStart());
        assertNull("Actual Filter has actual activity end date", actualFilter.getActualActivityDate().getStop());
        assertNull("Actual Filter has site name", actualFilter.getSiteName());
        assertNull("Actual Filter has study assigned identifier", actualFilter.getStudyAssignedIdentifier());
        assertNull("Actual Filter has activity type", actualFilter.getActivityType());
        assertNull("Actual Filter has label", actualFilter.getLabel());
        assertNull("Actual Filter has subject coordinator", actualFilter.getSubjectCoordinator());
    }

    public void testGetFilterForStudyAssignedIdentifier() throws Exception {
        String studyId = "101";
        FilterParameters.STUDY.putIn(request, studyId);
        ScheduledActivitiesReportFilters actualFilter = getResource().getFilters();
        assertNotNull("Actual Filter is Null", actualFilter);
        assertEquals("Actual Filter doesn't contain study", studyId, actualFilter.getStudyAssignedIdentifier());
        assertNull("Actual Filter has actual activity start date", actualFilter.getActualActivityDate().getStart());
        assertNull("Actual Filter has actual activity end date", actualFilter.getActualActivityDate().getStop());
        assertNull("Actual Filter has activity type", actualFilter.getActivityType());
        assertNull("Actual Filter has site name", actualFilter.getSiteName());
        assertNull("Actual Filter has current state mode", actualFilter.getCurrentStateMode());
        assertNull("Actual Filter has label", actualFilter.getLabel());
        assertNull("Actual Filter has subject coordinator", actualFilter.getSubjectCoordinator());
    }

    public void testGetFilterForSiteAssignedIdendtifier() throws Exception {
        String siteId = "11001";
        FilterParameters.SITE.putIn(request, siteId);
        ScheduledActivitiesReportFilters actualFilter = getResource().getFilters();
        assertNotNull("Actual Filter is Null", actualFilter);
        assertEquals("Actual Filter doesn't contain site name", siteId, actualFilter.getSiteName());
        assertNull("Actual Filter has actual activity start date", actualFilter.getActualActivityDate().getStart());
        assertNull("Actual Filter has actual activity end date", actualFilter.getActualActivityDate().getStop());
        assertNull("Actual Filter has activity type", actualFilter.getActivityType());
        assertNull("Actual Filter has study assigned identifier", actualFilter.getStudyAssignedIdentifier());
        assertNull("Actual Filter has current state mode", actualFilter.getCurrentStateMode());
        assertNull("Actual Filter has label", actualFilter.getLabel());
        assertNull("Actual Filter has subject coordinator", actualFilter.getSubjectCoordinator());
    }

    public void testGetFilterForActivityType() throws Exception {
        String activityTypeString = "111";
        ActivityType activityType = new ActivityType(activityTypeString);
        activityType.setId(new Integer(activityTypeString));
        FilterParameters.ACTIVITY_TYPE.putIn(request, activityTypeString);
        expect(activityTypeDao.getById(new Integer(activityType.getId()))).andReturn(activityType);
        replayMocks();
        ScheduledActivitiesReportFilters actualFilter = getResource().getFilters();
        verifyMocks();
        assertNotNull("Actual Filter is Null", actualFilter);
        assertSame("Actual Filter doesn't contain activity type", activityType, actualFilter.getActivityType());
        assertNull("Actual Filter has actual activity start date", actualFilter.getActualActivityDate().getStart());
        assertNull("Actual Filter has actual activity end date", actualFilter.getActualActivityDate().getStop());
        assertNull("Actual Filter has site name", actualFilter.getSiteName());
        assertNull("Actual Filter has study assigned identifier", actualFilter.getStudyAssignedIdentifier());
        assertNull("Actual Filter has current state mode", actualFilter.getCurrentStateMode());
        assertNull("Actual Filter has label", actualFilter.getLabel());
        assertNull("Actual Filter has subject coordinator", actualFilter.getSubjectCoordinator());
    }

    public void testGetJSONRepresentation() throws Exception {
        FilterParameters.START_DATE.putIn(request, "2010-03-01");
        FilterParameters.END_DATE.putIn(request, "2010-03-05");
        FilterParameters.STATE.putIn(request, "1");

        filters = getResource().getFilters();
        expect(reportService.searchScheduledActivities(eqFilters(filters))).andReturn(rows);

        makeRequestType(MediaType.APPLICATION_JSON);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertEquals("Result is not of right content type",
            MediaType.APPLICATION_JSON, response.getEntity().getMediaType());
    }

    public void testGetFilters() throws Exception {
        FilterParameters.START_DATE.putIn(request, "2009-05-01");
        FilterParameters.END_DATE.putIn(request, "2009-06-25");
        FilterParameters.STATE.putIn(request, "1");
        ScheduledActivitiesReportFilters filters1 = getResource().getFilters();
        assertNotNull("State filter is null" , filters1.getCurrentStateMode().getName());
        assertNotNull("Date range filter is null" , filters1.getActualActivityDate());
    }

    private void makeRequestType(MediaType requestType) {
        request.getClientInfo().setAcceptedMediaTypes(Arrays.asList(new Preference<MediaType>(requestType)));
    }

     ////// Custom Matchers
    public static ScheduledActivitiesReportFilters eqFilters(ScheduledActivitiesReportFilters in) {
        org.easymock.EasyMock.reportMatcher(new FilterMatcher(in));
        return null;
    }

    public static class FilterMatcher implements IArgumentMatcher {
        private ScheduledActivitiesReportFilters expected;

        public FilterMatcher(ScheduledActivitiesReportFilters expected) {
            this.expected = expected;
        }
       
        public boolean matches(Object actual) {
            if (!(actual instanceof ScheduledActivitiesReportFilters)) {
                return false;
            }
            ScheduledActivitiesReportFilters actualFilter = (ScheduledActivitiesReportFilters)actual;
            if (expected.getActivityType() != null && actualFilter.getActivityType() != null) {
                assertNotEquals("mismatched activity type", expected.getActivityType().equals(actualFilter.getActivityType()));
            }
            if (expected.getActualActivityDate() != null && actualFilter.getActualActivityDate() != null) {
                assertNotEquals("mismatched actual activity date", expected.getActualActivityDate().equals(actualFilter.getActualActivityDate()));
            }
            if (expected.getCurrentStateMode()!=null && actualFilter.getCurrentStateMode()!=null){
                assertNotEquals("mismatched current state mode", expected.getCurrentStateMode().equals(actualFilter.getCurrentStateMode()));
            }
            if (expected.getLabel()!=null && actualFilter.getLabel()!=null){
                assertNotEquals("mismatched current state mode", expected.getLabel().equals(actualFilter.getLabel()));
            }
            if (expected.getSiteName()!=null && actualFilter.getSiteName()!=null){
                assertNotEquals("mismatched current state mode", expected.getSiteName().equals(actualFilter.getSiteName()));
            }
            if (expected.getStudyAssignedIdentifier()!=null && actualFilter.getStudyAssignedIdentifier()!=null){
                assertNotEquals("mismatched current state mode", expected.getStudyAssignedIdentifier().equals(actualFilter.getStudyAssignedIdentifier()));
            }
            if (expected.getSubjectCoordinator()!=null && actualFilter.getSubjectCoordinator()!=null){
                assertNotEquals("mismatched current state mode", expected.getSubjectCoordinator().equals(actualFilter.getSubjectCoordinator()));
            }
            return true;
        }

        public void appendTo(StringBuffer buffer) {
            buffer.append("eqFilters(");
            buffer.append(expected.getClass().getName());
            buffer.append(" filter \"");
            buffer.append(expected);
            buffer.append("\")");
        }
    }
}
