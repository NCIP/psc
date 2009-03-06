package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import static edu.northwestern.bioinformatics.studycalendar.domain.Role.*;
import static org.easymock.EasyMock.expect;
import org.restlet.data.Status;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;

import java.util.Set;

/**
 * @author Jalpa Patel
 */
public class UserRolesResourceTest extends ResourceTestCase<UserRolesResource> {
    private UserService userService;
    private static final String USERNAME = "subjectCo";
    private Set<UserRole> userRoles;
    private User user;

    public void setUp() throws Exception {
        super.setUp();
        userService = registerMockFor(UserService.class);
        request.getAttributes().put(UriTemplateParameters.USERNAME.attributeName(), USERNAME);
        Role role = SUBJECT_COORDINATOR;
        user = Fixtures.createUser(USERNAME, role);
        Fixtures.setUserRoles(user,role);
        userRoles = user.getUserRoles();
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected UserRolesResource createResource() {
        UserRolesResource resource = new UserRolesResource();
        resource.setUserService(userService);
        resource.setXmlSerializer(xmlSerializer);
        return resource;
    }

    public void testGetAllowed() throws Exception {
        assertAllowedMethods("GET");
    }

    public void testGetRolesForUser() throws Exception {
        PscGuard.setCurrentAuthenticationToken(request, new UsernamePasswordAuthenticationToken(USERNAME, USERNAME, new Role[] { Role.SUBJECT_COORDINATOR}));
        expectFoundUser(user);
        expect(xmlSerializer.createDocumentString(userRoles)).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

    public void test400ForNoUsername() throws Exception {
        request.getAttributes().put(UriTemplateParameters.USERNAME.attributeName(), null);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    public void test403ForNotAllowedUser() throws Exception {
        PscGuard.setCurrentAuthenticationToken(request, new UsernamePasswordAuthenticationToken("siteCo", "siteCo", new Role[] { Role.SITE_COORDINATOR }));
        expectFoundUser(user);
        expect(xmlSerializer.createDocumentString(userRoles)).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_FORBIDDEN);
    }

    public void testGetUserRolesForSystemAdmin() throws Exception {
        PscGuard.setCurrentAuthenticationToken(request, new UsernamePasswordAuthenticationToken("systemAdmin", "systemAdmin", new Role[] { Role.SUBJECT_COORDINATOR, Role.SYSTEM_ADMINISTRATOR }));
        expectFoundUser(user);
        expect(xmlSerializer.createDocumentString(userRoles)).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

    public void expectFoundUser(User user) {
        expect(userService.getUserByName(USERNAME)).andReturn(user);
    }
}
