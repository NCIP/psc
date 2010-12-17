package edu.northwestern.bioinformatics.studycalendar.restlets;

public class UserActionsResourceTest extends AuthorizedResourceTestCase<UserActionsResource> {
    @Override
    @SuppressWarnings({ "unchecked" })
    protected UserActionsResource createAuthorizedResource() {
        return new UserActionsResource();
    }

    public void testPostAllowed() throws Exception {
        assertAllowedMethods("POST");
    }
}
