package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createBasicTemplate;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudySubjectAssignmentXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.service.AuthorizationService;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;

import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;


import static org.easymock.EasyMock.expect;
import org.restlet.data.Status;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import gov.nih.nci.cabig.ctms.lang.NowFactory;
import gov.nih.nci.cabig.ctms.lang.StaticNowFactory;
import gov.nih.nci.cabig.ctms.lang.DateTools;


/**
 * @author Jalpa Patel
 */
public class SubjectCentricScheduleResourceTest extends AuthorizedResourceTestCase<SubjectCentricScheduleResource>{
    private SubjectDao subjectDao;
    private AuthorizationService authorizationService;
    private static final String SUBJECT_IDENTIFIER = "1111";
    private Subject subject;
    private List<StudySubjectAssignment> studySubjectAssignments = new ArrayList<StudySubjectAssignment>();
    private NowFactory nowFactory;

    public void setUp() throws Exception {
        super.setUp();
        xmlSerializer = registerMockFor(StudySubjectAssignmentXmlSerializer.class);
        authorizationService = registerMockFor(AuthorizationService.class);
        subjectDao = registerDaoMockFor(SubjectDao.class);
        subject = createSubject("1111", "Perry", "Duglas", createDate(1980, Calendar.JANUARY, 15, 0, 0, 0), Gender.MALE);
        subject.setId(11);
        Study study = createBasicTemplate();
        Site site = createSite("NU");
        StudySubjectAssignment studySubjectAssignment = createAssignment(study,site,subject);
        studySubjectAssignment.getScheduledCalendar().addStudySegment(
            createScheduledStudySegment(DateTools.createDate(2006, Calendar.APRIL, 1), 365));
        studySubjectAssignments.add(studySubjectAssignment);
        ((StudySubjectAssignmentXmlSerializer)xmlSerializer).setSubjectCentric(true);
        request.getAttributes().put(UriTemplateParameters.SUBJECT_IDENTIFIER.attributeName(), SUBJECT_IDENTIFIER);
        nowFactory = new StaticNowFactory();

    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected SubjectCentricScheduleResource createAuthorizedResource() {
        SubjectCentricScheduleResource resource = new SubjectCentricScheduleResource();
        resource.setXmlSerializer(xmlSerializer);
        resource.setSubjectDao(subjectDao);
        resource.setAuthorizationService(authorizationService);
        resource.setNowFactory(nowFactory);
        return resource;
    }

    public void testGetAllowed() throws Exception {
        assertAllowedMethods("GET");
    }

    public void testGetScheduledCalendarXml() throws Exception {
        expectGetCurrentUser();
        expect(subjectDao.findSubjectByPersonId(SUBJECT_IDENTIFIER)).andReturn(subject);
        expect(authorizationService.filterAssignmentsForVisibility
                (studySubjectAssignments,getCurrentUser())).andReturn(studySubjectAssignments);
        expect(xmlSerializer.createDocumentString(studySubjectAssignments)).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

    public void testGet404WhenUnknownSubject() throws Exception {
        expect(subjectDao.findSubjectByPersonId(SUBJECT_IDENTIFIER)).andReturn(null);
        expect(subjectDao.getByGridId(SUBJECT_IDENTIFIER)).andReturn(null);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void testGet400WhenNoSubjectIdentifierInRequest() throws Exception {
        request.getAttributes().put(UriTemplateParameters.SUBJECT_IDENTIFIER.attributeName(), null);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    public void testGetJSONRepresentation() throws Exception {
        request.getAttributes().put(UriTemplateParameters.SUBJECT_IDENTIFIER.attributeName()+ ".json",SUBJECT_IDENTIFIER);
        expectGetCurrentUser();
        expect(subjectDao.findSubjectByPersonId(SUBJECT_IDENTIFIER)).andReturn(subject);
        expect(authorizationService.filterAssignmentsForVisibility
                (studySubjectAssignments,getCurrentUser())).andReturn(studySubjectAssignments);
        List<Preference<MediaType>> contentType = new ArrayList<Preference<MediaType>>();
        contentType.add(new Preference<MediaType>(MediaType.APPLICATION_JSON));
        request.getClientInfo().setAcceptedMediaTypes(contentType);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertEquals("Result is not of right content type", MediaType.APPLICATION_JSON, response.getEntity().getMediaType());
    }

}
