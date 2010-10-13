package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.service.StudySiteService;
import org.restlet.data.Method;
import org.restlet.data.Status;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class StudySiteResourceTest extends AuthorizedResourceTestCase<StudySiteResource> {
    private static final String STUDY_IDENTIFIER = "ETC";
    private static final String SITE_IDENTIFIER = "Northwestern University";
    private static final String SITE_IDENTIFIER_ENCODED = "Northwestern%20University";

    private StudyDao studyDao;
    private SiteDao siteDao;

    private Study study;
    private Site site;
    private StudySite studySite;
    private StudySiteDao studySiteDao;
    private StudySiteService studySiteService;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        study = createBasicTemplate();
        study.setAssignedIdentifier(STUDY_IDENTIFIER);
        site = createNamedInstance(SITE_IDENTIFIER, Site.class);

        studyDao = registerDaoMockFor(StudyDao.class);
        siteDao = registerDaoMockFor(SiteDao.class);
        studySiteDao = registerDaoMockFor(StudySiteDao.class);
        studySiteService = registerMockFor(StudySiteService.class);

        request.getAttributes().put(UriTemplateParameters.STUDY_IDENTIFIER.attributeName(), STUDY_IDENTIFIER);
        request.getAttributes().put(UriTemplateParameters.SITE_IDENTIFIER.attributeName(), SITE_IDENTIFIER_ENCODED);
        request.setResourceRef("studies/ETC/sites/Northwestern+University");
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected StudySiteResource createAuthorizedResource() {
        StudySiteResource res = new StudySiteResource();
        res.setXmlSerializer(xmlSerializer);
        res.setStudyDao(studyDao);
        res.setSiteDao(siteDao);
        res.setStudySiteDao(studySiteDao);
        res.setStudySiteService(studySiteService);
        return res;
    }

    public void testAllMethodsAllowed() throws Exception {
        expectLinked();
        replayMocks();
        assertAllowedMethods("GET", "PUT", "DELETE");
    }

    public void testGetWithAuthorizedRoles() {
        expectLinked();
        replayMocks();
        assertRolesAllowedForMethod(Method.GET,
            STUDY_SITE_PARTICIPATION_ADMINISTRATOR,
                STUDY_QA_MANAGER,
                STUDY_SUBJECT_CALENDAR_MANAGER,
                DATA_READER);
    }

    public void testPutWithAuthorizedRoles() {
        expectLinked();
        replayMocks();
        assertRolesAllowedForMethod(Method.PUT,
            STUDY_SITE_PARTICIPATION_ADMINISTRATOR);
    }

    public void testDeleteWithAuthorizedRoles() {
        expectLinked();
        replayMocks();
        assertRolesAllowedForMethod(Method.DELETE,
            STUDY_SITE_PARTICIPATION_ADMINISTRATOR);
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

    ////// PUT

    @SuppressWarnings({ "unchecked" })
    public void testPutCreatesStudySiteIfNotExists() throws Exception {
        StudySite unlinkedSS = createUnlinkedStudySite();

        expectRequestHasIgnoredEntity();
        expectResolvedStudyAndSite(study, site);
        expect(studySiteService.resolveStudySite(unlinkedSS)).andReturn(unlinkedSS);
        expectDeserializeEntity(unlinkedSS);
        expectSaveNewStudySite(unlinkedSS);
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
        expect(studyDao.getByAssignedIdentifier(STUDY_IDENTIFIER)).andReturn(expectedStudy);
        expect(siteDao.getByAssignedIdentifier(SITE_IDENTIFIER)).andReturn(expectedSite);
        if (expectedStudy != null && expectedSite != null) {
            expect(studySiteService.getStudySite(STUDY_IDENTIFIER, SITE_IDENTIFIER)).andReturn(studySite);
        } else {
            expect(studySiteService.getStudySite(STUDY_IDENTIFIER, SITE_IDENTIFIER)).andReturn(null);
        }
    }

    private void expectRequestHasIgnoredEntity() {
        request.setEntity(MOCK_XML_REP);
    }

    private void expectDeserializeEntity(StudySite studySite) throws Exception {
        expect(xmlSerializer.readDocument(MOCK_XML_REP.getStream())).andReturn(studySite);
    }

    private void expectSaveNewStudySite(StudySite unlinkedSS) {
        studySiteDao.save(unlinkedSS);
    }

    private void expectStudySiteDeleted() {
        studySiteDao.delete(studySite);
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
