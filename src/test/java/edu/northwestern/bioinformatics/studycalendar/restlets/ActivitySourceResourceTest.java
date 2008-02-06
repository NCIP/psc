package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import static org.easymock.classextension.EasyMock.expect;
import org.restlet.data.Status;

/**
 * @author Rhett Sutphin
 */
public class ActivitySourceResourceTest extends ResourceTestCase<ActivitySourceResource> {
    public static final String SOURCE_NAME = "House of Activities";
    public static final String SOURCE_NAME_ENCODED = "House%20of%20Activities";

    private SourceDao sourceDao;

    private Source source;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        sourceDao = registerDaoMockFor(SourceDao.class);
        request.getAttributes().put(UriTemplateParameters.ACTIVITY_SOURCE_NAME.attributeName(), SOURCE_NAME_ENCODED);

        source = Fixtures.createNamedInstance(SOURCE_NAME, Source.class);
    }

    @Override
    protected ActivitySourceResource createResource() {
        ActivitySourceResource resource = new ActivitySourceResource();
        resource.setSourceDao(sourceDao);
        resource.setXmlSerializer(xmlSerializer);
        return resource;
    }

    public void testGetAndPutAllowed() throws Exception {
        assertAllowedMethods("PUT", "GET");
    }

    public void testGetXmlForExistingSource() throws Exception {
        expectFoundSource(source);
        expectObjectXmlized(source);

        doGet();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertResponseIsCreatedXml();
    }

    public void testGetXmlForNonExistentSourceIs404() throws Exception {
        expectFoundSource(null);

        doGet();

        assertEquals("Result not 'not found'", 404, response.getStatus().getCode());
    }


    public void testPutExistingSource() throws Exception {
        Source newSource = new Source();
        expectFoundSource(source);
        expectReadXmlFromRequestAs(newSource);
        expectObjectXmlized(newSource);

        doPut();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertResponseIsCreatedXml();
    }

    public void testPutNewXml() throws Exception {
        expectFoundSource(null);
        expectObjectXmlized(source);
        expectReadXmlFromRequestAs(source);

        sourceDao.save(source);
        doPut();

        assertResponseStatus(Status.SUCCESS_CREATED);
        assertResponseIsCreatedXml();
    }


    private void expectFoundSource(Source expectedSource) {
        expect(sourceDao.getByName(SOURCE_NAME)).andReturn(expectedSource);
    }
}
