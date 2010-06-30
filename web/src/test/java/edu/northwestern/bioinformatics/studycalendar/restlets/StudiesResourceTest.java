package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.StudyListJsonRepresentation;
import edu.northwestern.bioinformatics.studycalendar.service.AuthorizationService;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.easymock.classextension.EasyMock.expect;

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

    public void testGetAndPostAllowed() throws Exception {
        assertAllowedMethods("GET", "POST");
    }

    public void testIsNotAvailableToSysadmins() throws Exception {
        Set<Role> roles = new HashSet<Role>(Arrays.asList(Role.values()));
        roles.remove(Role.SYSTEM_ADMINISTRATOR);
        assertLegacyRolesAllowedForMethod(Method.GET, roles.toArray(new Role[roles.size()]));
    }

    @SuppressWarnings({ "unchecked" })
    public void testAllRenderedOutInXml() throws Exception {
        Study a = Fixtures.createBasicTemplate("A"), b = Fixtures.createBasicTemplate("B");
        List<Study> aAndB = Arrays.asList(a, b);
        List<Study> justA = Arrays.asList(a);

        expect(studyDao.getAll()).andReturn(aAndB);
        expect(authorizationService.filterStudiesForVisibility(aAndB, getCurrentUser())).andReturn(justA);
        expect(xmlSerializer.createDocumentString(justA)).andReturn(MOCK_XML);

        setAcceptedMediaTypes(MediaType.TEXT_XML);
        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

    @SuppressWarnings({ "unchecked" })
    public void testAllRenderedOutInJson() throws Exception {
        Study a = Fixtures.createBasicTemplate("A"), b = Fixtures.createBasicTemplate("B");
        List<Study> aAndB = Arrays.asList(a, b);
        List<Study> justA = Arrays.asList(a);

        expect(studyDao.getAll()).andReturn(aAndB);
        expect(authorizationService.filterStudiesForVisibility(aAndB, getCurrentUser())).andReturn(justA);

        setAcceptedMediaTypes(MediaType.APPLICATION_JSON);
        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertTrue("Response entity is wrong type", response.getEntity() instanceof StudyListJsonRepresentation);
        assertSame("Response entity is for wrong studies", justA,
            ((StudyListJsonRepresentation) response.getEntity()).getStudies());
    }
}
