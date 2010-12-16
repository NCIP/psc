package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.PscUserBuilder;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.UserListJsonRepresentation;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.VisibleAuthorizationInformation;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Variant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory.createCsmUser;
import static java.util.Arrays.asList;
import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class UsersResourceTest extends AuthorizedResourceTestCase<UsersResource> {
    private PscUserService pscUserService;
    private List<User> stubCsmUsers;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        pscUserService = registerMockFor(PscUserService.class);

        stubCsmUsers = asList(
            createCsmUser(1, "arthur"),
            createCsmUser(4, "brian"),
            createCsmUser(9, "charles")
        );
        QueryParameters.Q.putIn(request, "a");
        expect(pscUserService.search("a")).andStubReturn(stubCsmUsers);

        setCurrentUser(
            new PscUserBuilder("yvette").add(PscRole.USER_ADMINISTRATOR).forAllSites().toUser());
    }

    @Override
    protected UsersResource createAuthorizedResource() {
        UsersResource res = new UsersResource();
        res.setPscUserService(pscUserService);
        return res;
    }

    public void testAllowsGetOnly() throws Exception {
        assertAllowedMethods("GET");
    }

    public void testAllowsUserAdminsAndSysAdminsOnly() throws Exception {
        assertRolesAllowedForMethod(Method.GET,
            PscRole.USER_ADMINISTRATOR, PscRole.SYSTEM_ADMINISTRATOR);
    }

    public void testAvailableAsJsonOnly() throws Exception {
        doInitOnly();
        List<Variant> actual = getResource().getVariants();
        assertEquals("Wrong number of variants: " + actual, 1, actual.size());
        assertEquals("Wrong variant", MediaType.APPLICATION_JSON, actual.get(0).getMediaType());
    }

    public void testGetAllUsersWorks() throws Exception {
        QueryParameters.Q.removeFrom(request);
        expect(pscUserService.search(null)).andReturn(stubCsmUsers);

        UserListJsonRepresentation actual = doGetAndReturnRepresentation();
        assertEquals("Wrong total", (Object) stubCsmUsers.size(), actual.getTotal());
        assertEquals("Wrong offset", (Object) 0, actual.getOffset());
        assertNull("Wrong limit", actual.getLimit());
        assertEquals("Wrong number of users", stubCsmUsers.size(), actual.getUsers().size());
    }

    public void testNonNumericLimitIsBadRequest() throws Exception {
        QueryParameters.LIMIT.putIn(request, "foo");
        doGet();
        assertEquals(400, response.getStatus().getCode());
    }

    public void testNonPositiveLimitIsBadRequest() throws Exception {
        QueryParameters.LIMIT.putIn(request, "0");
        doGet();
        assertEquals(400, response.getStatus().getCode());
    }

    public void testNonNumericOffsetIsBadRequest() throws Exception {
        QueryParameters.LIMIT.putIn(request, "2");
        QueryParameters.OFFSET.putIn(request, "foo");
        doGet();
        assertEquals(400, response.getStatus().getCode());
    }

    public void testNegativeOffsetIsBadRequest() throws Exception {
        QueryParameters.LIMIT.putIn(request, "5");
        QueryParameters.OFFSET.putIn(request, "-2");
        doGet();
        assertEquals(400, response.getStatus().getCode());
    }

    public void testLimitAloneIsUsed() throws Exception {
        QueryParameters.LIMIT.putIn(request, "2");

        UserListJsonRepresentation actual = doGetAndReturnRepresentation();
        assertEquals("Wrong total", (Object) stubCsmUsers.size(), actual.getTotal());
        assertEquals("Wrong offset", (Object) 0, actual.getOffset());
        assertEquals("Wrong limit", (Object) 2, actual.getLimit());
        assertUsernames("Wrong users included", asList("arthur", "brian"), actual);
    }

    public void testOffsetAloneIsBadRequest() throws Exception {
        QueryParameters.OFFSET.putIn(request, "5");
        doGet();
        assertEquals(400, response.getStatus().getCode());
    }

    public void testOffsetPlusLimitIsUsed() throws Exception {
        QueryParameters.LIMIT.putIn(request, "1");
        QueryParameters.OFFSET.putIn(request, "2");

        UserListJsonRepresentation actual = doGetAndReturnRepresentation();
        assertEquals("Wrong total", (Object) stubCsmUsers.size(), actual.getTotal());
        assertEquals("Wrong offset", (Object) 2, actual.getOffset());
        assertEquals("Wrong limit", (Object) 1, actual.getLimit());
        assertUsernames("Wrong users included", asList("charles"), actual);
    }

    public void testOffsetPastEndIsBadRequest() throws Exception {
        QueryParameters.LIMIT.putIn(request, "5");
        QueryParameters.OFFSET.putIn(request, Integer.toString(stubCsmUsers.size()));
        doGet();
        assertEquals(400, response.getStatus().getCode());
    }

    public void testLimitGreaterThanTotalIsAcceptable() throws Exception {
        QueryParameters.LIMIT.putIn(request, "8");
        UserListJsonRepresentation actual = doGetAndReturnRepresentation();
        assertEquals("Wrong total", (Object) stubCsmUsers.size(), actual.getTotal());
        assertEquals("Wrong offset", (Object) 0, actual.getOffset());
        assertEquals("Wrong limit", (Object) 8, actual.getLimit());
        assertUsernames("Wrong users included", asList("arthur", "brian", "charles"), actual);
    }

    public void testOffsetZeroForEmptyResultIsOkay() throws Exception {
        QueryParameters.Q.replaceIn(request, "zed");
        expect(pscUserService.search("zed")).andReturn(Collections.<User>emptyList());
        QueryParameters.LIMIT.putIn(request, "3");
        QueryParameters.OFFSET.putIn(request, "0");
        doGetAndReturnRepresentation();
    }

    public void testQValueUsed() throws Exception {
        QueryParameters.Q.replaceIn(request, "jo");
        expect(pscUserService.search("jo")).andReturn(asList(createCsmUser("jo")));
        doGetAndReturnRepresentation();
    }

    public void testBriefDoesNotLoadRoles() throws Exception {
        UserListJsonRepresentation actual = doGetAndReturnRepresentation();
        // pscUserService.getPscUsers not called, plus
        assertEquals("Wrong number of users", stubCsmUsers.size(), actual.getUsers().size());
        assertEquals("Wrong user", "arthur", actual.getUsers().get(0).getUsername());
        assertFalse("Should not tell rep to include roles", actual.getIncludeRoles());
    }

    public void testBriefTrueIsAccepted() throws Exception {
        QueryParameters.BRIEF.putIn(request, "true");
        doGetAndReturnRepresentation(); // asserts success
    }

    public void testBriefNonbooleanIsBadRequest() throws Exception {
        QueryParameters.BRIEF.putIn(request, "foo");
        doGet();
        assertEquals(400, response.getStatus().getCode());
    }

    public void testFullLoadsRolesForCurrentPageOnly() throws Exception {
        QueryParameters.BRIEF.putIn(request, "false");
        QueryParameters.LIMIT.putIn(request, "2");
        List<User> expectedSublist = asList(stubCsmUsers.get(0), stubCsmUsers.get(1));
        List<PscUser> expected = wrapCsmUsers(expectedSublist);
        expect(pscUserService.getPscUsers(expectedSublist, false)).andReturn(expected);
        expect(pscUserService.getVisibleAuthorizationInformationFor(getCurrentUser())).
            andReturn(new VisibleAuthorizationInformation());

        UserListJsonRepresentation actual = doGetAndReturnRepresentation();
        assertSame("Wrong users returned", expected, actual.getUsers());
        assertTrue("Should tell rep to include roles", actual.getIncludeRoles());
    }

    public void testFullSetsProperSiteAndStudyLimitsFromCurrentUser() throws Exception {
        Site nu = Fixtures.createSite("NU", "IL036");
        Study a = Fixtures.createBasicTemplate("A"),
            b = Fixtures.createBasicTemplate("B"),
            c = Fixtures.createBasicTemplate("C");

        VisibleAuthorizationInformation expected = new VisibleAuthorizationInformation();
        expected.setSites(asList(nu));
        expected.setStudiesForTemplateManagement(asList(a, b));
        expected.setStudiesForSiteParticipation(asList(c, b));
        expect(pscUserService.getVisibleAuthorizationInformationFor(getCurrentUser())).
            andReturn(expected);

        expect(pscUserService.getPscUsers(stubCsmUsers, false)).
            andReturn(wrapCsmUsers(stubCsmUsers));

        QueryParameters.BRIEF.putIn(request, "false");
        UserListJsonRepresentation actual = doGetAndReturnRepresentation();

        assertEquals("Wrong sites", asList(nu), actual.getVisibleSites());
        assertEquals("Wrong managed studies", asList("A", "B"),
            actual.getVisibleManagedStudyIdentifiers());
        assertEquals("Wrong participation studies", asList("C", "B"),
            actual.getVisibleParticipatingStudyIdentifiers());
    }

    private List<PscUser> wrapCsmUsers(List<User> csmUsers) {
        List<PscUser> fullUsers = new ArrayList<PscUser>(csmUsers.size());
        for (User user : csmUsers) {
            fullUsers.add(new PscUser(user, Collections.<SuiteRole, SuiteRoleMembership>emptyMap()));
        }
        return fullUsers;
    }

    private UserListJsonRepresentation doGetAndReturnRepresentation() {
        doGet();
        assertEquals("Not a success", 200, response.getStatus().getCode());
        assertTrue("Representation not expected type: " + response.getEntity().getClass().getName(),
            response.getEntity() instanceof UserListJsonRepresentation);
        return (UserListJsonRepresentation) response.getEntity();
    }

    private void assertUsernames(
        String message, List<String> expectedUsernames, UserListJsonRepresentation actual
    ) {
        assertEquals(message + ": Wrong number of users: " + actual.getUsers(),
            expectedUsernames.size(), actual.getUsers().size());
        for (int i = 0; i < expectedUsernames.size(); i++) {
            String username = expectedUsernames.get(i);
            assertEquals(message + ": Mismatch at " + i,
                username, actual.getUsers().get(i).getUsername());
        }
    }
}
