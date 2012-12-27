/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

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
    private static final String SITE_IDENTIFIER = "NU 1368";
    private static final String SITE_IDENTIFIER_ENCODED = "NU%201368";

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
        site = createSite(null, SITE_IDENTIFIER);

        studyDao = registerDaoMockFor(StudyDao.class);
        siteDao = registerDaoMockFor(SiteDao.class);
        studySiteDao = registerDaoMockFor(StudySiteDao.class);
        studySiteService = registerMockFor(StudySiteService.class);

        request.getAttributes().put(UriTemplateParameters.STUDY_IDENTIFIER.attributeName(), STUDY_IDENTIFIER);
        request.getAttributes().put(UriTemplateParameters.SITE_IDENTIFIER.attributeName(), SITE_IDENTIFIER_ENCODED);
        request.setResourceRef("studies/ETC/sites/NU%201368");
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

        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND, "No site matching " + SITE_IDENTIFIER);
    }

    public void testGetWhenSiteExistsButNotStudy() throws Exception {
        expectResolvedStudyAndSite(null, site);

        doGet();

        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND, "No study matching " + STUDY_IDENTIFIER);
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

    public void testPutCreatesTheStudySiteIfItDoesNotExistWhenTheEntityIsMissingTheSiteIdentifier() throws Exception {
        StudySite deserialized = createUnlinkedStudySite(study, new Site());
        StudySite unlinkedSS = createUnlinkedStudySite();

        expectResolvedStudyAndSite(study, site);
        expect(studySiteService.resolveStudySite(unlinkedSS)).andReturn(unlinkedSS);
        expectDeserializeEntity(deserialized);
        expectSaveNewStudySite(unlinkedSS);
        expectObjectXmlized(unlinkedSS);

        doPut();

        assertResponseStatus(Status.SUCCESS_CREATED);
        assertResponseIsCreatedXml();
    }

    public void testPutCreatesTheStudySiteIfItDoesNotExistWhenTheEntityIsMissingTheStudyIdentifier() throws Exception {
        StudySite deserialized = createUnlinkedStudySite(new Study(), site);
        StudySite unlinkedSS = createUnlinkedStudySite();

        expectResolvedStudyAndSite(study, site);
        expect(studySiteService.resolveStudySite(unlinkedSS)).andReturn(unlinkedSS);
        expectDeserializeEntity(deserialized);
        expectSaveNewStudySite(unlinkedSS);
        expectObjectXmlized(unlinkedSS);

        doPut();

        assertResponseStatus(Status.SUCCESS_CREATED);
        assertResponseIsCreatedXml();
    }

    public void testPutFailsIfTheEntitySiteDoesNotMatchTheUriSite() throws Exception {
        StudySite deserialized = createUnlinkedStudySite(study, createSite(null, "AV 1206"));

        expectResolvedStudyAndSite(study, site);
        expectDeserializeEntity(deserialized);

        doPut();

        assertResponseStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,
            "Entity- and URI-designated sites do not match. Either make them match or omit the one in the entity.");
    }

    public void testPutFailsIfTheEntityStudyDoesNotMatchTheUriStudy() throws Exception {
        StudySite deserialized = createUnlinkedStudySite(createBasicTemplate("ETC4"), site);

        expectResolvedStudyAndSite(study, site);
        expectDeserializeEntity(deserialized);

        doPut();

        assertResponseStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,
            "Entity- and URI-designated studies do not match. Either make them match or omit the one in the entity.");
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
        expectDeserializeEntity(createUnlinkedStudySite());
        expectResolvedStudyAndSite(null, site);
        doPut();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void testPut404sWhenSiteDoesNotExist() throws Exception {
        expectDeserializeEntity(createUnlinkedStudySite());
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
        request.setEntity(MOCK_XML_REP);
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
        return createUnlinkedStudySite(study, site);
    }

    private StudySite createUnlinkedStudySite(Study st, Site si) {
        StudySite unlinkedSS = new StudySite();
        unlinkedSS.setStudy(st);
        unlinkedSS.setSite(si);
        return unlinkedSS;
    }
}
