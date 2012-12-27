/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import org.restlet.data.Status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory.createPscUser;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationScopeMappings.createSuiteRoleMembership;
import static org.easymock.EasyMock.expect;

/**
 * @author Jalpa Patel
 */
public class UserRolesResourceTest extends AuthorizedResourceTestCase<UserRolesResource> {
    private PscUserService pscUserService;
    private static final String USERNAME = "TestUser";
    private PscUser user;

    public void setUp() throws Exception {
        super.setUp();
        pscUserService = registerMockFor(PscUserService.class);
        user = createPscUser(USERNAME);
        user.getMemberships().put(SuiteRole.STUDY_CREATOR, 
                createSuiteRoleMembership(PscRole.STUDY_CREATOR));
        user.getMemberships().put(SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER,
                createSuiteRoleMembership(PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER));
        request.getAttributes().put(UriTemplateParameters.USERNAME.attributeName(), USERNAME);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected UserRolesResource createAuthorizedResource() {
        UserRolesResource resource = new UserRolesResource();
        resource.setPscUserService(pscUserService);
        resource.setXmlSerializer(xmlSerializer);
        return resource;
    }

    public void testGetAllowed() throws Exception {
        assertAllowedMethods("GET");
    }

    public void test400ForNoUserName() throws Exception {
        UriTemplateParameters.USERNAME.removeFrom(request);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST, "No user name in request");
    }

    public void test400ForUnknownUser() throws Exception {
        expect(pscUserService.loadUserByUsername(USERNAME)).andReturn(null);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown user TestUser");
    }

    public void testGetUserRolesForUser() throws Exception {
        setCurrentUser(createPscUser(USERNAME));
        expect(pscUserService.loadUserByUsername(USERNAME)).andReturn(user);
        expect(xmlSerializer.createDocumentString(user.getMemberships().values())).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

    public void testGetUserRolesForUserAdmin() throws Exception {
        setCurrentUser(createPscUser("userAdmin", PscRole.USER_ADMINISTRATOR));
        expect(pscUserService.loadUserByUsername(USERNAME)).andReturn(user);
        expect(xmlSerializer.createDocumentString(user.getMemberships().values())).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

    public void testGetUserRolesForSystemAdminWhenUserIsUserAdmin() throws Exception {
        setCurrentUser(createPscUser("systemAdmin",PscRole.SYSTEM_ADMINISTRATOR));
        user.getMemberships().put(SuiteRole.USER_ADMINISTRATOR, createSuiteRoleMembership(PscRole.USER_ADMINISTRATOR));
        expect(pscUserService.loadUserByUsername(USERNAME)).andReturn(user);
        expect(xmlSerializer.createDocumentString(Arrays.asList(user.getMembership(PscRole.USER_ADMINISTRATOR)))).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

    public void testGetUserRolesForSystemAdminWhenUserIsUserAdminAndSysAdmin() throws Exception {
        setCurrentUser(createPscUser("systemAdmin",PscRole.SYSTEM_ADMINISTRATOR));
        user.getMemberships().put(SuiteRole.USER_ADMINISTRATOR, createSuiteRoleMembership(PscRole.USER_ADMINISTRATOR));
        user.getMemberships().put(SuiteRole.SYSTEM_ADMINISTRATOR, createSuiteRoleMembership(PscRole.SYSTEM_ADMINISTRATOR));
        expect(pscUserService.loadUserByUsername(USERNAME)).andReturn(user);
        Collection<SuiteRoleMembership> expectedMemberships = new ArrayList<SuiteRoleMembership>();
        expectedMemberships.add(user.getMembership(PscRole.USER_ADMINISTRATOR));
        expectedMemberships.add(user.getMembership(PscRole.SYSTEM_ADMINISTRATOR));
        expect(xmlSerializer.createDocumentString(expectedMemberships)).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

    public void testGetUserRolesForSystemAdminWhenUserIsNotUserAdmin() throws Exception {
        setCurrentUser(createPscUser("systemAdmin",PscRole.SYSTEM_ADMINISTRATOR));
        expect(pscUserService.loadUserByUsername(USERNAME)).andReturn(user);        

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_FORBIDDEN, "systemAdmin has insufficient privilege");
    }

    public void test403ForUnauthorisedUser() throws Exception {
        setCurrentUser(createPscUser("otherUser",PscRole.STUDY_CREATOR));
        expect(pscUserService.loadUserByUsername(USERNAME)).andReturn(user);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_FORBIDDEN, "otherUser has insufficient privilege");
    }

}
