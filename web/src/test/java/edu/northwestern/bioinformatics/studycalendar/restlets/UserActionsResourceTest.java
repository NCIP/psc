package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;

public class UserActionsResourceTest extends AuthorizedResourceTestCase<UserActionsResource> {
    private PscUserService pscUserService;
    private static final String USERNAME = "TestUser";
    private PscUser user;

//    public void setUp() throws Exception {
//        super.setUp();
//        pscUserService = registerMockFor(PscUserService.class);
//        user = createPscUser(USERNAME);
//        request.getAttributes().put(UriTemplateParameters.USERNAME.attributeName(), USERNAME);
//    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected UserActionsResource createAuthorizedResource() {
        return new UserActionsResource();
    }

    public void testPostAllowed() throws Exception {
        assertAllowedMethods("POST");
    }

    public void test201WhenAddingUserAction() throws Exception {
        request.setEntity(new JsonRepresentation(new JSONObject()));
        doPost();
        assertResponseStatus(Status.SUCCESS_CREATED);
    }

}
