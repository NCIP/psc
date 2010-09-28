package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.service.SourceService;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.BUSINESS_ADMINISTRATOR;
import static org.easymock.EasyMock.expect;

import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.json.JSONObject;

/**
 * @author Jalpa Patel
 */
public class SourceResourceTest  extends AuthorizedResourceTestCase<SourceResource> {
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
    protected SourceResource createAuthorizedResource() {
        SourceResource resource = new SourceResource();
        resource.setSourceService(sourceService);
        return resource;
    }

    public void testPutAllowed() throws Exception {
        assertAllowedMethods("PUT");
    }

    public void testPutWithAuthorizedRoles() {
        assertRolesAllowedForMethod(Method.PUT,
            BUSINESS_ADMINISTRATOR);
    }

    public void test400WhenNoSourceInRequest() throws Exception {
        request.getAttributes().put(UriTemplateParameters.SOURCE_NAME.attributeName(), null);

        doPut();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST, "No source name in the request");
    }

    public void test404WhenUnknownSource() throws Exception {
        expect(sourceService.getByName(SOURCE_NAME)).andReturn(null);

        doPut();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND, "No source found with the name Test_Source");
    }

    public void testPutSource() throws Exception {
        expect(sourceService.getByName(SOURCE_NAME)).andReturn(source);
        JSONObject entity = new JSONObject();
        entity.put("manual_target", true);
        request.setEntity(new JsonRepresentation(entity));
        sourceService.makeManualTarget(source);

        doPut();
        assertResponseStatus(Status.SUCCESS_OK);
      }

    public void testPutSourceWhenFlagIsNotTrue() throws Exception {
        expect(sourceService.getByName(SOURCE_NAME)).andReturn(source);
        JSONObject entity = new JSONObject();
        entity.put("manual_target", false);
        request.setEntity(new JsonRepresentation(entity));

        doPut();
        assertEquals("Result not success", 400, response.getStatus().getCode());
        assertContains(response.getStatus().getDescription(),
            "You may not unset the manual target field.  To set the manual target to a different source, set it to true on that source.");
    }
}


