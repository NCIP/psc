package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.AuthorizationService;
import edu.northwestern.bioinformatics.studycalendar.test.Fixtures;
import static org.easymock.classextension.EasyMock.expect;
import org.restlet.data.Status;

import java.util.Arrays;
import java.util.List;

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

    @SuppressWarnings({ "unchecked" })
    public void testAllRenderedOut() throws Exception {
        Study a = Fixtures.createBasicTemplate("A"), b = Fixtures.createBasicTemplate("B");
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
