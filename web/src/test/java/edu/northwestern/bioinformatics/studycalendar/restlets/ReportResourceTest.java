package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportRowDao;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import static org.easymock.EasyMock.expect;
import org.easymock.IArgumentMatcher;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.data.Preference;
import java.util.*;

/**
 * @author Nataliya Shurupova
 */
public class ReportResourceTest extends AuthorizedResourceTestCase<ReportsResource>{

    private ScheduledActivitiesReportRowDao scheduledActivitiesReportRowDao;
    private UserDao userDao;
    private ActivityTypeDao activityTypeDao;
    private ScheduledActivitiesReportFilters filters;
    private List<ScheduledActivitiesReportRow> rows;

    private Study study;
    private Site site;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        scheduledActivitiesReportRowDao = registerDaoMockFor(ScheduledActivitiesReportRowDao.class);
        userDao = registerDaoMockFor(UserDao.class);
        activityTypeDao = registerDaoMockFor(ActivityTypeDao.class);

        filters = new ScheduledActivitiesReportFilters();

        site = Fixtures.createSite("Site for whatever study");
        site.setId(500);
        study = createNamedInstance("Whatever Study", Study.class);
        study.setId(1001);
        ScheduledActivityState saState = new Scheduled();

        ScheduledActivitiesReportRow row1 = new ScheduledActivitiesReportRow();
        row1.setId(1001);
        row1.setLabel("label1");
        row1.setScheduledActivity(Fixtures.createScheduledActivity("activity1 ", 2010, 03, 02, saState));
        row1.setSubjectCoordinatorName("mayo mayo");
        row1.setSubject(Fixtures.createSubject("subject", "one"));
        row1.setSite(site);
        row1.setStudy(study);

        ScheduledActivitiesReportRow row2 = new ScheduledActivitiesReportRow();
        row2.setId(1002);
        row2.setLabel("label2");
        row2.setScheduledActivity(Fixtures.createScheduledActivity("activity2 ", 2010, 03, 04, saState));
        row2.setSubjectCoordinatorName("mayo mayo");
        row2.setSubject(Fixtures.createSubject("subject", "two"));
        row2.setSite(site);
        row2.setStudy(study);

        rows = new ArrayList<ScheduledActivitiesReportRow>();
        rows.add(row1);
        rows.add(row2);
    }


    @Override
    @SuppressWarnings({ "unchecked" })
    protected ReportsResource createAuthorizedResource() {
        ReportsResource resource = new ReportsResource();
        resource.setUserDao(userDao);
        resource.setActivityTypeDao(activityTypeDao);
        resource.setScheduledActivitiesReportRowDao(scheduledActivitiesReportRowDao);
        return resource;
    }

    public void testGetAllowed() throws Exception {
        assertAllowedMethods("GET");
    }

    public void testGetJSONRepresentation() throws Exception {
        FilterParameters.START_DATE.putIn(request, "2010-03-01");
        FilterParameters.END_DATE.putIn(request, "2010-03-05");
        FilterParameters.STATE.putIn(request, "1");

        filters = getResource().getFilters();
        expect(scheduledActivitiesReportRowDao.search(eqFilters(filters))).andReturn(rows);
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
