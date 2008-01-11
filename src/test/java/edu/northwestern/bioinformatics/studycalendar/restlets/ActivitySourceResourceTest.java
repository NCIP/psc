package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.xml.readers.ActivityXMLReader;
import static org.easymock.classextension.EasyMock.*;
import org.restlet.data.MediaType;

import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class ActivitySourceResourceTest extends ResourceTestCase<ActivitySourceResource> {
    public static final String SOURCE_NAME = "House of Activities";
    public static final String SOURCE_NAME_ENCODED = "House%20of%20Activities";

    private SourceDao sourceDao;
    private ActivityXMLReader activityXMLReader;

    private Source source;

    public void setUp() throws Exception {
        super.setUp();
        sourceDao = registerDaoMockFor(SourceDao.class);
        activityXMLReader = registerMockFor(ActivityXMLReader.class);
        request.getAttributes().put(UriTemplateParameters.SOURCE_NAME.attributeName(), SOURCE_NAME_ENCODED);

        source = Fixtures.createNamedInstance(SOURCE_NAME, Source.class);
    }

    protected ActivitySourceResource createResource() {
        ActivitySourceResource resource = new ActivitySourceResource();
        resource.setSourceDao(sourceDao);
        resource.setActivityXMLReader(activityXMLReader);
        return resource;
    }

    public void testGetAndPutAllowed() throws Exception {
        assertAllowedMethods("PUT", "GET");
    }

    public void testGetXmlForExistingSource() throws Exception {
        expect(sourceDao.getByName(SOURCE_NAME)).andReturn(source);

        doGet();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertResponseIsSourceXml();
    }

    public void testGetXmlForNonExistentSourceIs404() throws Exception {
        expect(sourceDao.getByName(SOURCE_NAME)).andReturn(null);

        doGet();

        assertEquals("Result not 'not found'", 404, response.getStatus().getCode());
    }

    private void assertResponseIsSourceXml() throws IOException {
        assertEquals("Result is not right content type", MediaType.TEXT_XML, response.getEntity().getMediaType());
        String actualEntityBody = response.getEntity().getText();
        assertContains("Entity does not appear to be proper XML for source", actualEntityBody, "name=\"House of Activities\"");
        assertContains("Entity does not appear to be proper XML for source", actualEntityBody, "<source");
    }

}
