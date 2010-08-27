package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.StudyListJsonRepresentation;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;

import java.util.Arrays;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createBasicTemplate;
import static edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.AuthorizationScopeMappings.createSuiteRoleMembership;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSite;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory.createPscUser;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
// TODO: there are no tests for POST
public class StudiesResourceTest extends AuthorizedResourceTestCase<StudiesResource> {
    private StudyDao studyDao;
    private Study a, b;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studyDao = registerDaoMockFor(StudyDao.class);
        a = createBasicTemplate("A");
        b = createBasicTemplate("B");
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected StudiesResource createAuthorizedResource() {
        StudiesResource res = new StudiesResource();
        res.setStudyDao(studyDao);
        res.setXmlSerializer(xmlSerializer);
        return res;
    }

    public void testGetAndPostAllowed() throws Exception {
        assertAllowedMethods("GET", "POST");
    }

    public void testGetWithAuthorizedRoles() {
        assertRolesAllowedForMethod(Method.GET, PscRole.valuesWithStudyAccess());
    }

    public void testPostWithAuthorizedRoles() {
         assertRolesAllowedForMethod(Method.POST, STUDY_CALENDAR_TEMPLATE_BUILDER);
    }

    @SuppressWarnings({ "unchecked" })
    public void testAllRenderedOutInXml() throws Exception {
        List<Study> aAndB = Arrays.asList(a, b);

        expect(studyDao.searchVisibleStudies(getCurrentUser().getVisibleStudyParameters(), null)).
            andReturn(aAndB);
        expect(xmlSerializer.createDocumentString(aAndB)).andReturn(MOCK_XML);

        setAcceptedMediaTypes(MediaType.TEXT_XML);
        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

    @SuppressWarnings({ "unchecked" })
    public void testAllRenderedOutInJson() throws Exception {
        List<Study> aAndB = Arrays.asList(a, b);

        expect(studyDao.searchVisibleStudies(getCurrentUser().getVisibleStudyParameters(), null)).
            andReturn(aAndB);

        setAcceptedMediaTypes(MediaType.APPLICATION_JSON);
        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertTrue("Response entity is wrong type",
            response.getEntity() instanceof StudyListJsonRepresentation);
        assertSame("Response entity is for wrong studies", aAndB,
            ((StudyListJsonRepresentation) response.getEntity()).getStudies());
    }

    @SuppressWarnings({ "unchecked" })
    public void testSearch() throws Exception {
        List<Study> aAndB = Arrays.asList(a, b);
        expect(studyDao.searchVisibleStudies(getCurrentUser().getVisibleStudyParameters(), "foom")).
            andReturn(aAndB);

        setAcceptedMediaTypes(MediaType.APPLICATION_JSON);
        QueryParameters.Q.putIn(request, "foom");
        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testFilteredStudyForPrivilege() throws Exception {
        QueryParameters.PRIVILEGE.putIn(request, "purge");
        setAcceptedMediaTypes(MediaType.APPLICATION_JSON);
        QueryParameters.Q.putIn(request, "foom");
        Site site = createSite("RHLCCC", "IL036");
        a.addManagingSite(site);
        b.addManagingSite(new Site());
        List<Study> aAndB = Arrays.asList(a, b);
        setCurrentUser(createPscUser("jo", createSuiteRoleMembership(STUDY_QA_MANAGER).forSites(site)));
        expect(studyDao.searchVisibleStudies(getCurrentUser().getVisibleStudyParameters(), "foom")).
            andReturn(aAndB);
        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertTrue("Response entity is wrong type",
            response.getEntity() instanceof StudyListJsonRepresentation);
        assertEquals("Response entity size for studies is wrong", 1,
            ((StudyListJsonRepresentation) response.getEntity()).getStudies().size());
        assertEquals("Response entity is for wrong studies", Arrays.asList(a),
            ((StudyListJsonRepresentation) response.getEntity()).getStudies());
    }

    public void testFilteredStudyForWrongPrivilege() throws Exception {
        QueryParameters.PRIVILEGE.putIn(request, "not_develop");
        setAcceptedMediaTypes(MediaType.APPLICATION_JSON);
        QueryParameters.Q.putIn(request, "foom");
        List<Study> aAndB = Arrays.asList(a, b);
        expect(studyDao.searchVisibleStudies(getCurrentUser().getVisibleStudyParameters(), "foom")).
            andReturn(aAndB);
        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
        assertEquals("Wrong response message", "Invalid study privilege: not_develop", response.getStatus().getDescription());
    }
}
