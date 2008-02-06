package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import static org.easymock.EasyMock.expect;
import org.restlet.data.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Saurabh Agrawal
 */
public class ActivitySourcesResourceTest extends ResourceTestCase<ActivitySourcesResource> {


    private SourceDao sourceDao;


    @Override
    public void setUp() throws Exception {
        super.setUp();
        sourceDao = registerDaoMockFor(SourceDao.class);
    }

    @Override
    protected ActivitySourcesResource createResource() {
        ActivitySourcesResource resource = new ActivitySourcesResource();
        resource.setSourceDao(sourceDao);
        resource.setXmlSerializer(xmlSerializer);
        return resource;
    }

    public void testGetAllowed() throws Exception {
        assertAllowedMethods("GET");
    }

    public void testPutNotAllowed() throws Exception {
        try {
            assertAllowedMethods("PUT");
            fail("PUT should be allowed");

        }
        catch (junit.framework.AssertionFailedError error) {
            // this is the expected error.
        }
    }

    public void testGetXmlForAllActivities() throws Exception {
        List<Source> activities = new ArrayList<Source>();
        expect(sourceDao.getAll()).andReturn(activities);
        expectObjectXmlized(activities);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }


}
