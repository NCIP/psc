package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.ReportService;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import org.easymock.IArgumentMatcher;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createActivityType;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static org.easymock.EasyMock.expect;

/**
 * @author Nataliya Shurupova
 */
public class ReportResourceTest extends AuthorizedResourceTestCase<ReportsResource> {
    private static final Collection<String> FILTER_PROPERTIES = new HashSet<String>();
    static {
        BeanWrapperImpl filter = new BeanWrapperImpl(new ScheduledActivitiesReportFilters());
        for (PropertyDescriptor descriptor : filter.getPropertyDescriptors()) {
            if (descriptor.getWriteMethod() != null) {
                FILTER_PROPERTIES.add(descriptor.getName());
            }
        }
        FILTER_PROPERTIES.remove("actualActivityDate");
        FILTER_PROPERTIES.add("actualActivityDate.start");
        FILTER_PROPERTIES.add("actualActivityDate.stop");
    }

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

    public void testGetFilterForRangeOfDates() throws Exception {
        FilterParameters.START_DATE.putIn(request, "2010-03-01");
        FilterParameters.END_DATE.putIn(request, "2010-03-08");
        ScheduledActivitiesReportFilters actualFilter = getResource().getFilters();
        assertNotNull("No filters built", actualFilter);
        assertNotNull("Filter doesn't contain the actual activity date range", actualFilter.getActualActivityDate());
        assertDayOfDate("Incorrect start date", 2010, Calendar.MARCH, 1, actualFilter.getActualActivityDate().getStart());
        assertDayOfDate("Incorrect stop date", 2010, Calendar.MARCH, 8, actualFilter.getActualActivityDate().getStop());
    }

    public void testGetFilterForEndDate() throws Exception {
        FilterParameters.END_DATE.putIn(request, "2010-03-08");
        assertOnlyFilterIs("actualActivityDate.stop", DateTools.createDate(2010, Calendar.MARCH, 8, 0, 0, 0));
    }

    public void testGetFilterForStartDate() throws Exception {
        FilterParameters.START_DATE.putIn(request, "2010-03-01");
        assertOnlyFilterIs("actualActivityDate.start", DateTools.createDate(2010, Calendar.MARCH, 1, 0, 0, 0));
    }

    public void testGetFilterForLabel() throws Exception {
        FilterParameters.LABEL.putIn(request, "labelA");
        assertOnlyFilterIs("label", "labelA");
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
        // TODO: this is very wrong (#1143)
        FilterParameters.STATE.putIn(request, "1");
        assertOnlyFilterIs("currentStateMode", ScheduledActivityMode.SCHEDULED);
    }

    public void testGetFilterForStudyAssignedIdentifier() throws Exception {
        FilterParameters.STUDY.putIn(request, "EG 1701");
        assertOnlyFilterIs("studyAssignedIdentifier", "EG 1701");
    }

    public void testGetFilterForSiteAssignedIdendtifier() throws Exception {
        FilterParameters.SITE.putIn(request, "Belgium, man");
        assertOnlyFilterIs("siteName", "Belgium, man");
    }

    public void testGetFilterForActivityType() throws Exception {
        // TODO: this is very wrong (#1143)
        ActivityType type = Fixtures.setId(111, createActivityType("Measure"));
        FilterParameters.ACTIVITY_TYPE.putIn(request, "111");
        expect(activityTypeDao.getById(type.getId())).andReturn(type);
        replayMocks();
        assertOnlyFilterIs("activityType", type);
        verifyMocks();
    }

    private void assertOnlyFilterIs(String filterProperty, Object expectedValue) throws ResourceException {
        BeanWrapper filterBean = new BeanWrapperImpl(getResource().getFilters());
        assertEquals("Filter property " + filterProperty + " not set to expected value",
            expectedValue, filterBean.getPropertyValue(filterProperty));
        for (String otherProperty : FILTER_PROPERTIES) {
            if (!filterProperty.equals(otherProperty)) {
                assertNull("Expected filter property " + otherProperty + " to be null",
                    filterBean.getPropertyValue(otherProperty));
            }
        }
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
//            if (expected.getSubjectCoordinator()!=null && actualFilter.getSubjectCoordinator()!=null){
//                assertNotEquals("mismatched current state mode", expected.getSubjectCoordinator().equals(actualFilter.getSubjectCoordinator()));
//            }
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
