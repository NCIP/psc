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


    public void testGetXmlForAllActivities() throws Exception {
        List<Source> sources = new ArrayList<Source>();
        Source source = new Source();
        sources.add(source);
        expect(sourceDao.getAll()).andReturn(sources);

        expect(xmlSerializer.createDocumentString(sources)).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }


}
