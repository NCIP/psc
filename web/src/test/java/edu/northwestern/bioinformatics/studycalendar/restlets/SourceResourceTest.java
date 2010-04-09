package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.service.SourceService;

import static org.easymock.EasyMock.expect;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.json.JSONObject;

/**
 * @author Jalpa Patel
 */
public class SourceResourceTest  extends ResourceTestCase<SourceResource> {
    public static final String SOURCE_NAME = "Test_Source";
    private SourceService sourceService;
    private Source source;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        sourceService = registerMockFor(SourceService.class);
        request.getAttributes().put(UriTemplateParameters.SOURCE_NAME.attributeName(), SOURCE_NAME);
        source = createSource(SOURCE_NAME);
    }

    @Override
    protected SourceResource createResource() {
        SourceResource resource = new SourceResource();
        resource.setSourceService(sourceService);
        resource.setXmlSerializer(xmlSerializer);
        return resource;
    }

    public void testAllowed() throws Exception {
        assertAllowedMethods("GET", "PUT");
    }

    public void testGetNotificationsXml() throws Exception {
        expect(sourceService.getByName(SOURCE_NAME)).andReturn(source);
        expect(xmlSerializer.createDocumentString(source)).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

    public void testGet404WhenUnknownNotification() throws Exception {
        expect(sourceService.getByName(SOURCE_NAME)).andReturn(null);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void testPutSource() throws Exception {
        expect(sourceService.getByName(SOURCE_NAME)).andReturn(source);
        JSONObject entity = new JSONObject();
        entity.put("manual_flag", true);
        request.setEntity(new JsonRepresentation(entity));
        sourceService.makeManualTarget(source);

        doPut();
        assertEquals("Result not success", 200, response.getStatus().getCode());
    }

    public void testPutSourceWhenFlagIsNotTrue() throws Exception {
        expect(sourceService.getByName(SOURCE_NAME)).andReturn(source);
        JSONObject entity = new JSONObject();
        entity.put("manual_flag", false);
        request.setEntity(new JsonRepresentation(entity));

        doPut();
        assertEquals("Result not success", 400, response.getStatus().getCode());
        assertContains(response.getStatus().getDescription(),
            "Manual Target Flag must be true to set source as manual activity target source");
    }
}


