package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import static edu.northwestern.bioinformatics.studycalendar.domain.Role.SUBJECT_COORDINATOR;

import java.util.Set;

import org.restlet.data.Status;
import static org.easymock.EasyMock.expect;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;

/**
 * @author Jalpa Patel
 */
public class UserRoleResourceTest extends ResourceTestCase<UserRoleResource> {
    private UserService userService;
    private static final String USERNAME = "subjectCo";
    private static final String ROLENAME = "Subject coordinator";
    private Set<UserRole> userRoles;
    private User user;
    public void setUp() throws Exception {
        super.setUp();
        userService = registerMockFor(UserService.class);
        request.getAttributes().put(UriTemplateParameters.USERNAME.attributeName(), USERNAME);
        request.getAttributes().put(UriTemplateParameters.ROLENAME.attributeName(), ROLENAME);
        Role role = SUBJECT_COORDINATOR;
        user = Fixtures.createUser(USERNAME, role);
        Fixtures.setUserRoles(user,role);
        userRoles = user.getUserRoles();
        PscGuard.setCurrentAuthenticationToken(request, new UsernamePasswordAuthenticationToken(USERNAME, USERNAME, new Role[] { Role.SUBJECT_COORDINATOR}));
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected UserRoleResource createResource() {
        UserRoleResource resource = new UserRoleResource();
        resource.setUserService(userService);
        resource.setXmlSerializer(xmlSerializer);
        return resource;
    }
    
    public void testGetAllowed() throws Exception {
        assertAllowedMethods("GET");
    }

    public void testGetUserRoleForUser() throws Exception {
        PscGuard.setCurrentAuthenticationToken(request, new UsernamePasswordAuthenticationToken(USERNAME, USERNAME, new Role[] { Role.SUBJECT_COORDINATOR}));
        expect(userService.getUserByName(USERNAME)).andReturn(user);
        expect(xmlSerializer.createDocumentString(userRoles.iterator().next())).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

    public void test404ForNonExistentRoleForUser() throws Exception {
        PscGuard.setCurrentAuthenticationToken(request, new UsernamePasswordAuthenticationToken(USERNAME, USERNAME, new Role[] { Role.SUBJECT_COORDINATOR}));
        request.getAttributes().put(UriTemplateParameters.ROLENAME.attributeName(),"Site coordinator");
        expect(userService.getUserByName(USERNAME)).andReturn(user);

        doGet();
        assertEquals("Result not 'not found'", 404, response.getStatus().getCode());
    }

    public void test403ForUnauthorisedUser() throws Exception {
        PscGuard.setCurrentAuthenticationToken(request, new UsernamePasswordAuthenticationToken("subjectCo1", "subjectCo1", new Role[] { Role.SUBJECT_COORDINATOR}));
        expect(userService.getUserByName(USERNAME)).andReturn(user);
        request.getAttributes().put(UriTemplateParameters.ROLENAME.attributeName(),"Subject coordinator");

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_FORBIDDEN);
    }
    
    public void test404ForNonExistentRolesForSysAdmin() throws Exception {
        PscGuard.setCurrentAuthenticationToken(request, new UsernamePasswordAuthenticationToken(USERNAME, USERNAME, new Role[] { Role.SYSTEM_ADMINISTRATOR}));
        request.getAttributes().put(UriTemplateParameters.ROLENAME.attributeName(),"Site coordinator");
        expect(userService.getUserByName(USERNAME)).andReturn(user);

        doGet();
        assertEquals("Result not 'not found'", 404, response.getStatus().getCode());
    }
}
