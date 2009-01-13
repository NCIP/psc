package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import static edu.northwestern.bioinformatics.studycalendar.test.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.xml.CapturingStudyCalendarXmlFactoryStub;
import edu.northwestern.bioinformatics.studycalendar.xml.domain.Registration;
import edu.nwu.bioinformatics.commons.DateUtils;
import org.acegisecurity.Authentication;
import org.acegisecurity.providers.TestingAuthenticationToken;
import static org.easymock.EasyMock.expect;
import org.restlet.data.Method;
import org.restlet.data.Status;

import static java.util.Calendar.APRIL;
import java.util.Collection;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class RegistrationsResourceTest extends AuthorizedResourceTestCase<RegistrationsResource> {
    private static final String STUDY_IDENTIFIER = "EC golf";
    private static final String STUDY_IDENTIFIER_ENCODED = "EC+golf";
    private static final String SITE_NAME = "AgU";

    private Study study;
    private Site site;
    private StudySite studySite;

    private StudyDao studyDao;
    private SubjectDao subjectDao;
    private SiteDao siteDao;
    private SubjectService subjectService;
    private CapturingStudyCalendarXmlFactoryStub<StudySubjectAssignment> assignmentSerializerStub;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        study = createBasicTemplate();
        study.setAssignedIdentifier(STUDY_IDENTIFIER);
        site = createNamedInstance(SITE_NAME, Site.class);
        studySite = createStudySite(study, site);

        studyDao = registerDaoMockFor(StudyDao.class);
        subjectDao = registerDaoMockFor(SubjectDao.class);
        siteDao = registerDaoMockFor(SiteDao.class);
        subjectService = registerMockFor(SubjectService.class);
        assignmentSerializerStub = new CapturingStudyCalendarXmlFactoryStub<StudySubjectAssignment>();

        request.getAttributes().put(UriTemplateParameters.STUDY_IDENTIFIER.attributeName(), STUDY_IDENTIFIER_ENCODED);
        request.getAttributes().put(UriTemplateParameters.SITE_IDENTIFIER.attributeName(), SITE_NAME);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected RegistrationsResource createAuthorizedResource() {
        RegistrationsResource res = new RegistrationsResource();
        res.setStudyDao(studyDao);
        res.setSiteDao(siteDao);
        res.setSubjectService(subjectService);
        res.setSubjectDao(subjectDao);
        res.setXmlSerializer(xmlSerializer);
        return res;
    }

    ////// GET

    @SuppressWarnings({ "unchecked", "RawUseOfParameterizedType" })
    public void testGetXmlForKnownStudySite() throws Exception {
        studySite.addStudySubjectAssignment(createAssignment());
        studySite.addStudySubjectAssignment(createAssignment());

        expectResolvedStudyAndSite(study, site);
        getResource().setAssignmentXmlSerializer(assignmentSerializerStub);
        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsStubbedXml();

        Object serialized = assignmentSerializerStub.getLastObjectStringified();
        assertNotNull("Stringified object is null", serialized);
        assertTrue("Serialized object should have been a collection", serialized instanceof Collection);
        assertTrue("Serialized collection contents should be registrations",
            ((Collection) serialized).iterator().next() instanceof StudySubjectAssignment);
        assertEquals("Wrong number of entries in serialized collection", 2, ((Collection) serialized).size());
    }

    private StudySubjectAssignment createAssignment() {
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        assignment.setStudySite(studySite);
        ScheduledCalendar cal = new ScheduledCalendar();
        assignment.setScheduledCalendar(cal);
        ScheduledStudySegment seg = new ScheduledStudySegment();
        cal.getScheduledStudySegments().add(seg);
        seg.setStudySegment(study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0));
        return assignment;
    }

    public void testGet404sOnMissingStudy() throws Exception {
        expectResolvedStudyAndSite(null, site);
        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void testGet404sOnMissingSite() throws Exception {
        expectResolvedStudyAndSite(study, null);
        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void testGet404sOnMissingStudySite() throws Exception {
        study.getStudySites().clear();
        site.getStudySites().clear();
        expectResolvedStudyAndSite(study, site);
        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void testGetWithAuthorizedRole() {
        assertRolesAllowedForMethod(Method.GET, Role.SUBJECT_COORDINATOR);
    }

    ////// POST

    public void testPost404sOnMissingStudySite() throws Exception {
        study.getStudySites().clear();
        site.getStudySites().clear();
        expectResolvedStudyAndSite(study, site);

        doPost();

        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void testPostAddsAssignment() throws Exception {
        Date expectedDate = DateUtils.createDate(2005, APRIL, 5);
        StudySegment expectedSegment = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);
        Subject expectedSubject = setId(4, new Subject());
        String expectedAssignmentId = "DC";
        Registration posted = Registration.create(expectedSegment, expectedDate, expectedSubject, expectedAssignmentId);
        posted.setSubjectCoordinator(new User());

        expectResolvedStudyAndSite(study, site);
        expectReadXmlFromRequestAs(posted);
        expect(subjectDao.getAssignment(expectedSubject,study,site)).andReturn(null);
        expect(subjectService.assignSubject(expectedSubject, studySite, expectedSegment, expectedDate,
            expectedAssignmentId, null, posted.getSubjectCoordinator())).andReturn(setGridId(expectedAssignmentId, new StudySubjectAssignment()));

        doPost();

        assertResponseStatus(Status.REDIRECTION_SEE_OTHER);
        assertEquals(ROOT_URI + "/studies/EC+golf/schedules/DC",
            response.getLocationRef().getTargetRef().toString());
    }

    public void testPostAddsAssignmentWithoutSubjectCoordinator() throws Exception {
        Date expectedDate = DateUtils.createDate(2005, APRIL, 5);
        StudySegment expectedSegment = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);
        Subject expectedSubject = setId(7, new Subject());
        String expectedAssignmentId = "DC";
        Registration posted = Registration.create(expectedSegment, expectedDate, expectedSubject, expectedAssignmentId);
        User subjCoord = createNamedInstance("Subject Coord", User.class);
        Authentication auth = new TestingAuthenticationToken(subjCoord, null, null);
        request.getAttributes().put(PscGuard.AUTH_TOKEN_ATTRIBUTE_KEY, auth);

        expectResolvedStudyAndSite(study, site);
        expectReadXmlFromRequestAs(posted);
        expect(subjectDao.getAssignment(expectedSubject,study,site)).andReturn(null);
        expect(subjectService.assignSubject(expectedSubject, studySite, expectedSegment, expectedDate,
            expectedAssignmentId, null, subjCoord)).andReturn(setGridId(expectedAssignmentId, new StudySubjectAssignment()));

        doPost();

        assertResponseStatus(Status.REDIRECTION_SEE_OTHER);
        assertEquals(ROOT_URI + "/studies/EC+golf/schedules/DC",
            response.getLocationRef().getTargetRef().toString());
    }

    public void testPutWithAuthorizedRole() {
        assertRolesAllowedForMethod(Method.POST, Role.SUBJECT_COORDINATOR);
    }

    ////// Helper Methods

    private void expectResolvedStudyAndSite(Study expectedStudy, Site expectedSite) {
        expect(studyDao.getByAssignedIdentifier(STUDY_IDENTIFIER)).andReturn(expectedStudy);
        expect(siteDao.getByAssignedIdentifier(SITE_NAME)).andReturn(expectedSite);
    }
}
