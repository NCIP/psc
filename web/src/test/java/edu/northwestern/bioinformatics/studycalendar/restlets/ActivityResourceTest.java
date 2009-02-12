package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.core.ServicedFixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import static org.easymock.classextension.EasyMock.expect;
import org.restlet.data.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Saurabh Agrarwal
 */
public class ActivityResourceTest extends ResourceTestCase<ActivityResource> {
    public static final String SOURCE_NAME = "House of Activities";
    public static final String SOURCE_NAME_ENCODED = "House%20of%20Activities";

    public static final String ACTIVITY_NAME = "Activities";

    private ActivityDao activityDao;

    private Activity activity;

    private PlannedActivityDao plannedActivityDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        activityDao = registerDaoMockFor(ActivityDao.class);
        plannedActivityDao = registerDaoMockFor(PlannedActivityDao.class);
        request.getAttributes().put(UriTemplateParameters.ACTIVITY_SOURCE_NAME.attributeName(), SOURCE_NAME_ENCODED);
        request.getAttributes().put(UriTemplateParameters.ACTIVITY_CODE.attributeName(), ACTIVITY_NAME);

        activity = ServicedFixtures.createNamedInstance(ACTIVITY_NAME, Activity.class);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected ActivityResource createResource() {
        ActivityResource resource = new ActivityResource();
        resource.setActivityDao(activityDao);
        resource.setXmlSerializer(xmlSerializer);
        resource.setPlannedActivityDao(plannedActivityDao);
        return resource;
    }

    public void testGetAndPutAndDeleteAllowed() throws Exception {
        assertAllowedMethods("PUT", "GET", "DELETE");
    }

    public void testGetXmlForExistingActivity() throws Exception {
        expectFoundActivity(activity);
        expectObjectXmlized(activity);

        doGet();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertResponseIsCreatedXml();
    }

    public void testGetXmlForNonExistentActivityIs404() throws Exception {
        expectFoundActivity(null);

        doGet();

        assertEquals("Result not 'not found'", 404, response.getStatus().getCode());
    }

    public void testPutExistingActivity() throws Exception {
        Activity newActivity = new Activity();
        expectFoundActivity(activity);
        expectReadXmlFromRequestAs(newActivity);
        expectObjectXmlized(newActivity);

        activityDao.save(activity);
        doPut();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertResponseIsCreatedXml();
    }

    public void testDeleteExistingActivityWhichIsNotusedAnyWhere() throws Exception {
        expectFoundActivity(activity);
        expectActivityUsedByPlannedCalendar(activity, false);
        activityDao.delete(activity);
        doDelete();

        assertEquals("Result not success", 200, response.getStatus().getCode());
//        assertResponseIsCreatedXml();
    }

    public void testDeleteExistingActivityWhichIsused() throws Exception {
        expectFoundActivity(activity);
        expectActivityUsedByPlannedCalendar(activity, true);
        doDelete();

        assertEquals("Result is success", 400, response.getStatus().getCode());
//        assertResponseIsCreatedXml();
    }

    public void testPutNewXml() throws Exception {
        expectFoundActivity(null);
        expectObjectXmlized(activity);
        expectReadXmlFromRequestAs(activity);

        activityDao.save(activity);
        doPut();

        assertResponseStatus(Status.SUCCESS_CREATED);
        assertResponseIsCreatedXml();
    }

    private void expectFoundActivity(Activity expectedActivity) {
        expect(activityDao.getByCodeAndSourceName(ACTIVITY_NAME, SOURCE_NAME)).andReturn(expectedActivity);
    }

    private void expectActivityUsedByPlannedCalendar(Activity expectedActivity, boolean isExcepted) {
        if (isExcepted) {
            List<PlannedActivity> plannedActivities = new ArrayList<PlannedActivity>();
            plannedActivities.add(new PlannedActivity());
            expect(plannedActivityDao.getPlannedActivitiesForActivity(expectedActivity.getId())).andReturn(plannedActivities);
        } else {
            expect(plannedActivityDao.getPlannedActivitiesForActivity(expectedActivity.getId())).andReturn(null);

        }
    }

}
