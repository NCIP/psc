package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import static org.easymock.classextension.EasyMock.expect;
import org.restlet.data.Method;
import org.restlet.data.Status;

/**
 * @author Rhett Sutphin
 */
public class StudySiteResourceTest extends AuthorizedResourceTestCase<StudySiteResource> {
    private static final String STUDY_IDENT = "ETC";
    private static final String SITE_NAME = "Northwestern University";
    private static final String SITE_NAME_ENCODED = "Northwestern%20University";

    private StudyDao studyDao;
    private SiteDao siteDao;

    private Study study;
    private Site site;
    private StudySite studySite;
    private StudyService studyService;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        study = createBasicTemplate();
        site = createNamedInstance(SITE_NAME, Site.class);

        studyDao = registerDaoMockFor(StudyDao.class);
        siteDao = registerDaoMockFor(SiteDao.class);
        studyService = registerMockFor(StudyService.class);

        request.getAttributes().put(UriTemplateParameters.STUDY_IDENTIFIER.attributeName(), STUDY_IDENT);
        request.getAttributes().put(UriTemplateParameters.SITE_NAME.attributeName(), SITE_NAME_ENCODED);
        request.setResourceRef("studies/ETC/sites/Northwestern University");
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected StudySiteResource createResource() {
        StudySiteResource res = new StudySiteResource();
        res.setXmlSerializer(xmlSerializer);
        res.setStudyDao(studyDao);
        res.setSiteDao(siteDao);
        res.setStudyService(studyService);
        return res;
    }

    public void testAllMethodsAllowed() throws Exception {
        expectResolvedStudyAndSite(null, null);
        assertAllowedMethods("GET", "PUT", "DELETE");
    }

    ////// GET

    public void testGetWhenStudyExistsButNotSite() throws Exception {
        expectResolvedStudyAndSite(study, null);

        doGet();

        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void testGetWhenSiteExistsButNotStudy() throws Exception {
        expectResolvedStudyAndSite(null, site);

        doGet();

        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void testGetWhenNeitherSiteNotStudyExist() throws Exception {
        expectResolvedStudyAndSite(null, null);

        doGet();

        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void testGetWhenStudyAndSiteExistButAreNotLinked() throws Exception {
        expectResolvedStudyAndSite(study, site);

        doGet();

        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void testGetWhenStudySiteExists() throws Exception {
        expectLinked();
        expectObjectXmlized(studySite);

        doGet();

        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

    public void testGetWithAuthorizedRole() {
        doInit();
        assertRoleIsAllowedForMethod(Role.SITE_COORDINATOR, Method.GET);
    }

    ////// PUT

    @SuppressWarnings({ "unchecked" })
    public void testPutCreatesStudySiteIfNotExists() throws Exception {
        StudySite unlinkedSS = createUnlinkedStudySite();

        expectRequestHasIgnoredEntity();
        expectResolvedStudyAndSite(study, site);
        expectDeserializeEntity(unlinkedSS);
        expectSaveNewStudySite();
        expectObjectXmlized(unlinkedSS);

        doPut();

        assertResponseStatus(Status.SUCCESS_CREATED);
        assertResponseIsCreatedXml();
    }

    public void testPutDoesNothingIfStudySiteAlreadyExists() throws Exception {
        StudySite unlinkedSS = createUnlinkedStudySite();

        expectLinked();
        expectRequestHasIgnoredEntity();
        expectDeserializeEntity(unlinkedSS);
        expectObjectXmlized(studySite);

        doPut();

        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

    public void testPut404sWhenStudyDoesNotExist() throws Exception {
        expectRequestHasIgnoredEntity();
        expectResolvedStudyAndSite(null, site);
        doPut();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void testPut404sWhenSiteDoesNotExist() throws Exception {
        expectRequestHasIgnoredEntity();
        expectResolvedStudyAndSite(study, null);
        doPut();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void testPutWithAuthorizedRole() {
        doInit();
        assertRoleIsAllowedForMethod(Role.SITE_COORDINATOR, Method.PUT);
    }

    ////// DELETE

    public void testDeleteRemovesStudyIfExistsAndNoSubjects() {
        expectRequestHasIgnoredEntity();
        expectLinked();
        expectStudySiteDeleted();

        doDelete();
        
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testDelete400sIfStudyHasSubjectsAssigned() {
        expectRequestHasIgnoredEntity();
        expectLinkedWithSubjects();
        
        doDelete();

        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    ////// HELPERS

    private void expectLinked() {
        studySite = createStudySite(study, site);
        expectResolvedStudyAndSite(study, site);
    }

    private void expectResolvedStudyAndSite(Study expectedStudy, Site expectedSite) {
        expect(studyDao.getByAssignedIdentifier(STUDY_IDENT)).andReturn(expectedStudy);
        expect(siteDao.getByAssignedIdentifier(SITE_NAME)).andReturn(expectedSite);
    }

    private void expectRequestHasIgnoredEntity() {
        request.setEntity(MOCK_XML_REP);
    }

    private void expectDeserializeEntity(StudySite studySite) throws Exception {
        expect(xmlSerializer.readDocument(MOCK_XML_REP.getStream())).andReturn(studySite);
    }

    private void expectSaveNewStudySite() {
        studyService.save(study);
    }

    private void expectStudySiteDeleted() {
        studyService.save(study);
    }

    private void expectLinkedWithSubjects() {
        expectLinked();

        StudySubjectAssignment assignment = new StudySubjectAssignment();
        assignment.setSubject(createSubject("Frank", "Drebin"));

        studySite.addStudySubjectAssignment(assignment);
    }

    private StudySite createUnlinkedStudySite() {
        StudySite unlinkedSS = new StudySite();
        unlinkedSS.setStudy(study);
        unlinkedSS.setSite(site);
        return unlinkedSS;
    }
}
