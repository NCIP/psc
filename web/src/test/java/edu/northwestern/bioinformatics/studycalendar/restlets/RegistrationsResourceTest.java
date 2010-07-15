package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.service.RegistrationService;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.xml.CapturingStudyCalendarXmlFactoryStub;
import edu.northwestern.bioinformatics.studycalendar.xml.domain.Registration;
import edu.nwu.bioinformatics.commons.DateUtils;
import org.restlet.data.Method;
import org.restlet.data.Status;

import java.util.Collection;
import java.util.Date;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Role.SUBJECT_COORDINATOR;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static java.util.Calendar.APRIL;
import static org.easymock.EasyMock.expect;

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
    private Subject existedSubject;
    private StudySubjectAssignment ssa;

    private StudyDao studyDao;
    private SiteDao siteDao;
    private SubjectService subjectService;
    private RegistrationService registrationService;
    private CapturingStudyCalendarXmlFactoryStub<StudySubjectAssignment> assignmentSerializerStub;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        study = createBasicTemplate();
        study.setAssignedIdentifier(STUDY_IDENTIFIER);
        site = createNamedInstance(SITE_NAME, Site.class);
        studySite = createStudySite(study, site);
        existedSubject = createSubject("firstName", "lastName");
        existedSubject.setPersonId("123");

        studyDao = registerDaoMockFor(StudyDao.class);
        ssa = createAssignment();
        ssa.setStudySite(createStudySite(study, site));
        ssa.setSubject(existedSubject);
        siteDao = registerDaoMockFor(SiteDao.class);
        subjectService = registerMockFor(SubjectService.class);
        registrationService = registerMockFor(RegistrationService.class);
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
        res.setRegistrationService(registrationService);
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
        assertLegacyRolesAllowedForMethod(Method.GET, Role.SUBJECT_COORDINATOR);
    }

    public void testGetAndPutAndDeleteAllowed() throws Exception {
        assertAllowedMethods("POST", "GET");
    }

    public void testGetWithAuthorizedRoles() {
        assertRolesAllowedForMethod(Method.GET,
            STUDY_TEAM_ADMINISTRATOR,
            STUDY_SUBJECT_CALENDAR_MANAGER,
            DATA_READER);
    }

    public void testPostWithAuthorizedRoles() {
        assertRolesAllowedForMethod(Method.POST,
            STUDY_SUBJECT_CALENDAR_MANAGER);
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
        User user = Fixtures.createUser("subjectCo", SUBJECT_COORDINATOR);
        user.getUserRole(SUBJECT_COORDINATOR).addStudySite(studySite);
        posted.setSubjectCoordinator(user);

        expectResolvedStudyAndSite(study, site);
        expectReadXmlFromRequestAs(posted);
        expectResolvedRegistration(posted);
        expect(subjectService.assignSubject(expectedSubject, studySite, expectedSegment, expectedDate,
            expectedAssignmentId, null, posted.getSubjectCoordinator())).andReturn(setGridId(expectedAssignmentId, new StudySubjectAssignment()));

        doPost();

        assertResponseStatus(Status.SUCCESS_CREATED);
        assertEquals(ROOT_URI + "/studies/EC+golf/schedules/DC",
            response.getLocationRef().getTargetRef().toString());
    }

    public void testPostAddsAssignmentWithoutSubjectCoordinatorName() throws Exception {
        Date expectedDate = DateUtils.createDate(2005, APRIL, 5);
        StudySegment expectedSegment = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);
        Subject expectedSubject = setId(7, new Subject());
        String expectedAssignmentId = "DC";
        Registration posted = Registration.create(expectedSegment, expectedDate, expectedSubject, expectedAssignmentId);
        User subjCoord = Fixtures.createUser("subjectCo", SUBJECT_COORDINATOR);
        subjCoord.getUserRole(SUBJECT_COORDINATOR).addStudySite(studySite);
        setLegacyCurrentUser(subjCoord);

        expectResolvedStudyAndSite(study, site);
        expectReadXmlFromRequestAs(posted);
        expectResolvedRegistration(posted);
        expect(subjectService.assignSubject(expectedSubject, studySite, expectedSegment, expectedDate,
            expectedAssignmentId, null, subjCoord)).andReturn(setGridId(expectedAssignmentId, new StudySubjectAssignment()));

        doPost();

        assertResponseStatus(Status.SUCCESS_CREATED);
        assertEquals(ROOT_URI + "/studies/EC+golf/schedules/DC",
            response.getLocationRef().getTargetRef().toString());
    }

    public void testPostAddsAssignmentWithExistedSubject() throws Exception {
        Date expectedDate = DateUtils.createDate(2005, APRIL, 5);
        StudySegment expectedSegment = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);
        String expectedAssignmentId = "DC";
        Registration posted = Registration.create(expectedSegment, expectedDate, existedSubject, expectedAssignmentId);
        User subjCoord = Fixtures.createUser("subjectCo", SUBJECT_COORDINATOR);
        subjCoord.getUserRole(SUBJECT_COORDINATOR).addStudySite(studySite);
        setLegacyCurrentUser(subjCoord);

        expectResolvedStudyAndSite(study, site);
        expectReadXmlFromRequestAs(posted);
        expectResolvedRegistration(posted);
        expect(subjectService.assignSubject(existedSubject, studySite, expectedSegment, expectedDate,
            expectedAssignmentId, null, subjCoord)).andReturn(setGridId(expectedAssignmentId, new StudySubjectAssignment()));

        doPost();

        assertResponseStatus(Status.SUCCESS_CREATED);
        assertEquals(ROOT_URI + "/studies/EC+golf/schedules/DC",
            response.getLocationRef().getTargetRef().toString());
    }

    public void testPost403WhenStudyNotVisibleToSubjectCoordinator() throws Exception {
        Date expectedDate = DateUtils.createDate(2005, APRIL, 5);
        StudySegment expectedSegment = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);
        Subject expectedSubject = setId(7, new Subject());
        String expectedAssignmentId = "DC";
        Registration posted = Registration.create(expectedSegment, expectedDate, expectedSubject, expectedAssignmentId);

        User subjCoord = Fixtures.createUser("subjectCo", SUBJECT_COORDINATOR);
        posted.setSubjectCoordinator(subjCoord);
        expectResolvedStudyAndSite(study, site);
        expectReadXmlFromRequestAs(posted);
        expectResolvedRegistration(posted);
        doPost();
        assertResponseStatus(Status.CLIENT_ERROR_FORBIDDEN);
    }

    public void testPutWithAuthorizedRole() {
        assertLegacyRolesAllowedForMethod(Method.POST, Role.SUBJECT_COORDINATOR);
    }

    ////// Helper Methods

    private void expectResolvedStudyAndSite(Study expectedStudy, Site expectedSite) {
        expect(studyDao.getByAssignedIdentifier(STUDY_IDENTIFIER)).andReturn(expectedStudy);
        expect(siteDao.getByAssignedIdentifier(SITE_NAME)).andReturn(expectedSite);
    }

    private void expectResolvedRegistration(Registration registration) {
        expect(registrationService.resolveRegistration(registration)).andReturn(registration);
    }
}
