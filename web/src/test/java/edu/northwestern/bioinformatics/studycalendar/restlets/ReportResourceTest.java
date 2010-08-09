package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportRowDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Conditional;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.service.AuthorizationService;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import org.easymock.IArgumentMatcher;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static org.easymock.EasyMock.expect;

/**
 * @author Nataliya Shurupova
 */
public class ReportResourceTest extends AuthorizedResourceTestCase<ReportsResource>{

    private ScheduledActivitiesReportRowDao scheduledActivitiesReportRowDao;
    private UserDao userDao;
    private ActivityTypeDao activityTypeDao;
    private ScheduledActivitiesReportFilters filters;
    private List<ScheduledActivitiesReportRow> rows;

    private Study studyA, studyB, studyZ;
    private Site siteA, siteB, siteZ;

    private StudySite ssAa, ssBb, ssZz;

    private User user;
    private StudyDao studyDao;
    private List<Study> studies, ownedStudies;
    private AuthorizationService authorizationService;
    private List<StudySite> ownedStudySites;


    @Override
    protected void setUp() throws Exception {
        super.setUp();

        scheduledActivitiesReportRowDao = registerDaoMockFor(ScheduledActivitiesReportRowDao.class);
        userDao = registerDaoMockFor(UserDao.class);
        activityTypeDao = registerDaoMockFor(ActivityTypeDao.class);

        filters = new ScheduledActivitiesReportFilters();

        siteA = Fixtures.createSite("Site for whatever study");
        siteA.setId(500);
        studyA = createNamedInstance("Whatever Study", Study.class);
        studyA.setId(1001);
        ScheduledActivityState saState = new Scheduled();

        ScheduledActivitiesReportRow row1 = new ScheduledActivitiesReportRow();
        row1.setId(1001);
        ScheduledActivity activity1 = Fixtures.createScheduledActivity("activity1 ", 2010, 03, 02, saState);
        SortedSet<String> labels1 = new TreeSet<String>();
        labels1.add("label1");
        activity1.setLabels(labels1);
        row1.setScheduledActivity(activity1);
        row1.setSubjectCoordinatorName("mayo mayo");
        row1.setSubject(Fixtures.createSubject("subject", "one"));
        row1.setSite(siteA);
        row1.setStudy(studyA);

        ScheduledActivitiesReportRow row2 = new ScheduledActivitiesReportRow();
        row2.setId(1002);
        ScheduledActivity activity2 = Fixtures.createScheduledActivity("activity2 ", 2010, 03, 04, saState);
        SortedSet<String> labels2 = new TreeSet<String>();
        labels2.add("label2");
        row2.setScheduledActivity(activity2);
        row2.setSubjectCoordinatorName("mayo mayo");
        row2.setSubject(Fixtures.createSubject("subject", "two"));
        row2.setSite(siteA);
        row2.setStudy(studyA);

        rows = new ArrayList<ScheduledActivitiesReportRow>();
        rows.add(row1);
        rows.add(row2);

        studyB  = createNamedInstance("B", Study.class);
        studyB.setId(1001);
        studyZ  = createNamedInstance("Z", Study.class);
        studyZ.setId(1002);

        siteB = createNamedInstance("b", Site.class);
        siteB.setId(10001);
        siteZ = createNamedInstance("z", Site.class);
        siteZ.setId(10002);

        ssAa  = createStudySite(studyA, siteA);
        ssBb  = createStudySite(studyB, siteB);
        ssZz  = createStudySite(studyZ, siteZ);

        user = createUser("jimbo", Role.SITE_COORDINATOR, Role.SUBJECT_COORDINATOR);
        user.getUserRole(Role.SITE_COORDINATOR).addSite(siteB);
        user.getUserRole(Role.SUBJECT_COORDINATOR).addSite(siteZ);
        SecurityContextHolderTestHelper.setSecurityContext(user, null);

        studies = new ArrayList<Study>();
        studies.add(studyA);
        studies.add(studyB);
        studies.add(studyZ);

        ownedStudies = new ArrayList<Study>();
        ownedStudies.add(studyB);
        ownedStudies.add(studyZ);
        studyDao = registerDaoMockFor(StudyDao.class);

        ownedStudySites = new ArrayList<StudySite>();
        ownedStudySites.add(ssBb);
        ownedStudySites.add(ssZz);

        applicationSecurityManager.setUserService(registerMockFor(UserService.class));
        authorizationService = registerMockFor(AuthorizationService.class);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected ReportsResource createAuthorizedResource() {
        ReportsResource resource = new ReportsResource();
        resource.setUserDao(userDao);
        resource.setActivityTypeDao(activityTypeDao);
        resource.setScheduledActivitiesReportRowDao(scheduledActivitiesReportRowDao);
        resource.setAuthorizationService(authorizationService);
        resource.setApplicationSecurityManager(applicationSecurityManager);
        resource.setStudyDao(studyDao);
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

    public void testFilteredRowsForUserWithoutRightStudySitePermissions() throws Exception {
        FilterParameters.STATE.putIn(request, "1");

        filters = getResource().getFilters();
        User userOne = getLegacyCurrentUser();
        List<UserRole> expectedUserRoles = Arrays.asList(
                createUserRole(userOne, Role.SITE_COORDINATOR, siteB),
                createUserRole(userOne, Role.SUBJECT_COORDINATOR, siteZ)
            );
        userOne.setUserRoles(new HashSet<UserRole>(expectedUserRoles));
        setLegacyCurrentUser(userOne);

        expect(studyDao.getAll()).andReturn(studies);
        expect(authorizationService.filterStudiesForVisibility(studies, userOne.getUserRole(Role.SUBJECT_COORDINATOR))).andReturn(ownedStudies);
        expect(authorizationService.filterStudySitesForVisibilityFromStudiesList(ownedStudies, userOne.getUserRole(Role.SUBJECT_COORDINATOR))).andReturn(ownedStudySites);
        replayMocks();
        List<ScheduledActivitiesReportRow> filteredRows = getResource().filteredRows(rows);
        verifyMocks();
        assertEquals("The filtered rows are not empty", 0, filteredRows.size());
    }

    public void testFilteredRowsForUserWithPartialStudySitePermissions() throws Exception {
        FilterParameters.STUDY.putIn(request, "1001");

        filters = getResource().getFilters();
        User userOne = getLegacyCurrentUser();
        List<UserRole> expectedUserRoles = Arrays.asList(
                createUserRole(userOne, Role.SITE_COORDINATOR, siteB),
                createUserRole(userOne, Role.SUBJECT_COORDINATOR, siteZ)
            );
        userOne.setUserRoles(new HashSet<UserRole>(expectedUserRoles));
        setLegacyCurrentUser(userOne);
        ScheduledActivitiesReportRow row3 = new ScheduledActivitiesReportRow();
        row3.setId(1003);
        ScheduledActivityState sa3State = new Conditional();
        ScheduledActivity activity3 = Fixtures.createScheduledActivity("activity2 ", 2010, 03, 04, sa3State);
        row3.setScheduledActivity(activity3);
        row3.setSubjectCoordinatorName(userOne.getName());
        row3.setSubject(Fixtures.createSubject("subject", "two"));
        row3.setSite(siteB);
        row3.setStudy(studyB);
        rows.add(row3);

        expect(studyDao.getAll()).andReturn(studies);
        expect(authorizationService.filterStudiesForVisibility(studies, userOne.getUserRole(Role.SUBJECT_COORDINATOR))).andReturn(ownedStudies);
        expect(authorizationService.filterStudySitesForVisibilityFromStudiesList(ownedStudies, userOne.getUserRole(Role.SUBJECT_COORDINATOR))).andReturn(ownedStudySites);
        replayMocks();
        List<ScheduledActivitiesReportRow> filteredRows = getResource().filteredRows(rows);
        verifyMocks();
        assertEquals("The filtered rows have more than ONE element", 1, filteredRows.size());
        assertEquals("The first element does not belong to StudyB ", studyB, filteredRows.get(0).getStudy());
        assertEquals("The first element does not belong to SiteB ", siteB, filteredRows.get(0).getSite());
    }

    public void test200ForSupportedCSVMediaType() throws Exception {
        FilterParameters.STATE.putIn(request, "1");
        filters = getResource().getFilters();
        User userOne = getLegacyCurrentUser();
        List<UserRole> expectedUserRoles = Arrays.asList(
                createUserRole(userOne, Role.SITE_COORDINATOR, siteB),
                createUserRole(userOne, Role.SUBJECT_COORDINATOR, siteZ)
            );
        userOne.setUserRoles(new HashSet<UserRole>(expectedUserRoles));
        setLegacyCurrentUser(userOne);
        expect(scheduledActivitiesReportRowDao.search(eqFilters(filters))).andReturn(rows);
        expect(studyDao.getAll()).andReturn(studies);
        expect(authorizationService.filterStudiesForVisibility(studies, userOne.getUserRole(Role.SUBJECT_COORDINATOR))).andReturn(ownedStudies);
        expect(authorizationService.filterStudySitesForVisibilityFromStudiesList(ownedStudies, userOne.getUserRole(Role.SUBJECT_COORDINATOR))).andReturn(ownedStudySites);
        request.getResourceRef().addQueryParameter("state", "1");
        request.getClientInfo().setAcceptedMediaTypes(Arrays.asList(new Preference<MediaType>(PscMetadataService.TEXT_CSV)));
        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void test200ForSupportedJSONMediaType() throws Exception {
        FilterParameters.STATE.putIn(request, "1");
        filters = getResource().getFilters();
        User userOne = getLegacyCurrentUser();
        List<UserRole> expectedUserRoles = Arrays.asList(
                createUserRole(userOne, Role.SITE_COORDINATOR, siteB),
                createUserRole(userOne, Role.SUBJECT_COORDINATOR, siteZ)
            );
        userOne.setUserRoles(new HashSet<UserRole>(expectedUserRoles));
        setLegacyCurrentUser(userOne);
        expect(scheduledActivitiesReportRowDao.search(eqFilters(filters))).andReturn(rows);
        expect(studyDao.getAll()).andReturn(studies);
        expect(authorizationService.filterStudiesForVisibility(studies, userOne.getUserRole(Role.SUBJECT_COORDINATOR))).andReturn(ownedStudies);
        expect(authorizationService.filterStudySitesForVisibilityFromStudiesList(ownedStudies, userOne.getUserRole(Role.SUBJECT_COORDINATOR))).andReturn(ownedStudySites);
        request.getResourceRef().addQueryParameter("state", "1");
        request.getClientInfo().setAcceptedMediaTypes(Arrays.asList(new Preference<MediaType>(MediaType.APPLICATION_JSON)));
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
        expect(scheduledActivitiesReportRowDao.search(eqFilters(filters))).andReturn(rows);
        User user = getLegacyCurrentUser();

        List<UserRole> expectedUserRoles = Arrays.asList(
                createUserRole(user, Role.SITE_COORDINATOR, siteB),
                createUserRole(user, Role.SUBJECT_COORDINATOR, siteZ)
            );
        user.setUserRoles(new HashSet<UserRole>(expectedUserRoles));
        setLegacyCurrentUser(user);
        expect(studyDao.getAll()).andReturn(studies);
        expect(authorizationService.filterStudiesForVisibility(studies, user.getUserRole(Role.SUBJECT_COORDINATOR))).andReturn(ownedStudies);
        expect(authorizationService.filterStudySitesForVisibilityFromStudiesList(ownedStudies, user.getUserRole(Role.SUBJECT_COORDINATOR))).andReturn(ownedStudySites);
        makeRequestType(MediaType.APPLICATION_JSON);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertEquals("Result is not of right content type", MediaType.APPLICATION_JSON, response.getEntity().getMediaType());
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
