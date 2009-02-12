package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.AuthorizationService;
import edu.northwestern.bioinformatics.studycalendar.test.ServicedFixtures;
import static org.easymock.classextension.EasyMock.*;
import org.restlet.data.Method;
import org.restlet.data.Status;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
public class StudiesResourceTest extends AuthorizedResourceTestCase<StudiesResource> {
    private StudyDao studyDao;
    private AuthorizationService authorizationService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studyDao = registerDaoMockFor(StudyDao.class);
        authorizationService = registerMockFor(AuthorizationService.class);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected StudiesResource createAuthorizedResource() {
        StudiesResource res = new StudiesResource();
        res.setStudyDao(studyDao);
        res.setXmlSerializer(xmlSerializer);
        res.setAuthorizationService(authorizationService);
        return res;
    }

    public void testIsReadOnly() throws Exception {
        assertAllowedMethods("GET", "POST");
    }

    public void testIsNotAvailableToSysadmins() throws Exception {
        Set<Role> roles = new HashSet<Role>(Arrays.asList(Role.values()));
        roles.remove(Role.SYSTEM_ADMINISTRATOR);
        assertRolesAllowedForMethod(Method.GET, roles.toArray(new Role[roles.size()]));
    }

    @SuppressWarnings({ "unchecked" })
    public void testAllRenderedOut() throws Exception {
        Study a = ServicedFixtures.createBasicTemplate("A"), b = ServicedFixtures.createBasicTemplate("B");
        List<Study> aAndB = Arrays.asList(a, b);
        List<Study> justA = Arrays.asList(a);

        expectGetCurrentUser();
        expect(studyDao.getAll()).andReturn(aAndB);
        expect(authorizationService.filterStudiesForVisibility(aAndB, getCurrentUser())).andReturn(justA);
        expect(xmlSerializer.createDocumentString(justA)).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }
}
