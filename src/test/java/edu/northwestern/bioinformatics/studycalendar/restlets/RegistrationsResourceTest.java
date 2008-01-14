package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.nwu.bioinformatics.commons.DateUtils;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.xml.domain.Registration;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;

import java.util.Date;
import java.util.Collection;
import static java.util.Calendar.APRIL;

import static org.easymock.EasyMock.expect;
import org.restlet.data.Status;

/**
 * @author Rhett Sutphin
 */
public class RegistrationsResourceTest extends ResourceTestCase<RegistrationsResource> {
    private static final String STUDY_IDENTIFIER = "EC golf";
    private static final String STUDY_IDENTIFIER_ENCODED = "EC+golf";
    private static final String SITE_NAME = "AgU";

    private Study study;
    private Site site;
    private StudySite studySite;

    private StudyDao studyDao;
    private SiteDao siteDao;
    private SubjectService subjectService;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        study = createBasicTemplate();
        study.setAssignedIdentifier(STUDY_IDENTIFIER);
        site = createNamedInstance(SITE_NAME, Site.class);
        studySite = createStudySite(study, site);

        studyDao = registerDaoMockFor(StudyDao.class);
        siteDao = registerDaoMockFor(SiteDao.class);
        subjectService = registerMockFor(SubjectService.class);

        request.getAttributes().put(UriTemplateParameters.STUDY_IDENTIFIER.attributeName(), STUDY_IDENTIFIER_ENCODED);
        request.getAttributes().put(UriTemplateParameters.SITE_NAME.attributeName(), SITE_NAME);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected RegistrationsResource createResource() {
        RegistrationsResource res = new RegistrationsResource();
        res.setStudyDao(studyDao);
        res.setSiteDao(siteDao);
        res.setSubjectService(subjectService);
        res.setXmlSerializer(xmlSerializer);
        return res;
    }

    ////// GET

    @SuppressWarnings({ "unchecked", "RawUseOfParameterizedType" })
    public void testGetXmlForKnownStudySite() throws Exception {
        studySite.addStudySubjectAssignment(createAssignment());
        studySite.addStudySubjectAssignment(createAssignment());

        expectResolvedStudyAndSite(study, site);
        getResource().setXmlSerializer(xmlSerializerStub);
        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsStubbedXml();

        Object serialized = xmlSerializerStub.getLastObjectStringified();
        assertNotNull("Stringified object is null", serialized);
        assertTrue("Serialized object should have been a collection", serialized instanceof Collection);
        assertTrue("Serialized collection contents should be registrations",
            ((Collection) serialized).iterator().next() instanceof Registration);
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
        Subject expectedSubject = new Subject();
        String expectedAssignmentId = "DC";
        Registration posted = Registration.create(expectedSegment, expectedDate, expectedSubject, expectedAssignmentId);
        posted.setSubjectCoordinator(new User());

        expectResolvedStudyAndSite(study, site);
        expectReadXmlFromRequestAs(posted);
        expect(subjectService.assignSubject(expectedSubject, studySite, expectedSegment, expectedDate,
            expectedAssignmentId, posted.getSubjectCoordinator())).andReturn(setGridId(expectedAssignmentId, new StudySubjectAssignment()));

        doPost();

        assertResponseStatus(Status.REDIRECTION_SEE_OTHER);
        assertEquals(BASE_URI + "studies/EC+golf/schedules/DC",
            response.getLocationRef().getTargetRef().toString());
    }

    private void expectResolvedStudyAndSite(Study expectedStudy, Site expectedSite) {
        expect(studyDao.getByAssignedIdentifier(STUDY_IDENTIFIER)).andReturn(expectedStudy);
        expect(siteDao.getByName(SITE_NAME)).andReturn(expectedSite);
    }
}
