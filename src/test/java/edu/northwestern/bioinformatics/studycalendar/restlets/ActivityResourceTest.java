package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import static org.easymock.classextension.EasyMock.expect;
import org.restlet.data.Status;

/**
 * @author Saurabh Agrarwal
 */
public class ActivityResourceTest extends ResourceTestCase<ActivityResource> {
    public static final String SOURCE_NAME = "House of Activities";
    public static final String SOURCE_NAME_ENCODED = "House%20of%20Activities";

    public static final String ACTIVITY_NAME = "Activities";

    private ActivityDao activityDao;

    private Activity activity;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        activityDao = registerDaoMockFor(ActivityDao.class);
        request.getAttributes().put(UriTemplateParameters.ACTIVITY_SOURCE_NAME.attributeName(), SOURCE_NAME_ENCODED);
        request.getAttributes().put(UriTemplateParameters.ACTIVITY_CODE.attributeName(), ACTIVITY_NAME);

        activity = Fixtures.createNamedInstance(ACTIVITY_NAME, Activity.class);
    }

    @Override
    protected ActivityResource createResource() {
        ActivityResource resource = new ActivityResource();
        resource.setActivityDao(activityDao);
        resource.setXmlSerializer(xmlSerializer);
        return resource;
    }

    public void testGetAndPutAllowed() throws Exception {
        assertAllowedMethods("PUT", "GET");
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

        doPut();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertResponseIsCreatedXml();
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
        expect(activityDao.getByCodeAndSourceName(ACTIVITY_NAME,SOURCE_NAME)).andReturn(expectedActivity);
    }
}
