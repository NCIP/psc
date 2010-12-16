package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Gender;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudySubjectAssignmentXmlSerializer;
import gov.nih.nci.cabig.ctms.lang.NowFactory;
import gov.nih.nci.cabig.ctms.lang.StaticNowFactory;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createAssignment;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSite;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSubject;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static edu.nwu.bioinformatics.commons.DateUtils.*;
import static org.easymock.EasyMock.*;


/**
 * @author Jalpa Patel
 */
public class SubjectCentricScheduleResourceTest extends AuthorizedResourceTestCase<SubjectCentricScheduleResource>{
    private static final String SUBJECT_IDENTIFIER = "1111";

    private SubjectDao subjectDao;
    private Subject subject;
    private List<StudySubjectAssignment> studySubjectAssignments = new ArrayList<StudySubjectAssignment>();
    private NowFactory nowFactory;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        xmlSerializer = registerMockFor(StudySubjectAssignmentXmlSerializer.class);
        subjectDao = registerDaoMockFor(SubjectDao.class);
        subject = createSubject("1111", "Perry", "Duglas", createDate(1980, Calendar.JANUARY, 15, 0, 0, 0), Gender.MALE);
        subject.setId(11);
        Study study = createBasicTemplate("Joe's Study");
        Site site = createSite("NU");
        StudySubjectAssignment studySubjectAssignment = createAssignment(study,site,subject);
        study = Fixtures.createSingleEpochStudy("S", "Treatment");
        Epoch epoch = study.getPlannedCalendar().getEpochs().get(0);
        epoch.setId(11);
        StudySegment studySegment = epoch.getStudySegments().get(0);
        studySegment.setName("Segment1");
        studySegment.setId(21);
        ScheduledStudySegment scheduledStudySegment = Fixtures.createScheduledStudySegment(studySegment);
        studySubjectAssignment.getScheduledCalendar().addStudySegment(scheduledStudySegment);
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
        resource.setNowFactory(nowFactory);
        return resource;
    }

    public void testGetAllowed() throws Exception {
        assertAllowedMethods("GET");
    }
    
    public void testGetWithAuthorizedRoles() {
        assertRolesAllowedForMethod(Method.GET,
            STUDY_SUBJECT_CALENDAR_MANAGER,
            STUDY_TEAM_ADMINISTRATOR,
            DATA_READER);
    }

    public void testGetScheduledCalendarXml() throws Exception {
        expect(subjectDao.findSubjectByPersonId(SUBJECT_IDENTIFIER)).andReturn(subject);
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

    public void test403WhenUserCannotAccessSchedule() throws Exception {
        expect(subjectDao.findSubjectByPersonId(SUBJECT_IDENTIFIER)).andReturn(subject);
        setCurrentUser(AuthorizationObjectFactory.createPscUser("bad", PscRole.SYSTEM_ADMINISTRATOR));
        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_FORBIDDEN);
    }

    public void testGet400WhenNoSubjectIdentifierInRequest() throws Exception {
        UriTemplateParameters.SUBJECT_IDENTIFIER.removeFrom(request);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    public void testGetJSONRepresentation() throws Exception {
        request.getAttributes().put(UriTemplateParameters.SUBJECT_IDENTIFIER.attributeName()+ ".json",SUBJECT_IDENTIFIER);
        expect(subjectDao.findSubjectByPersonId(SUBJECT_IDENTIFIER)).andReturn(subject);
        makeRequestType(MediaType.APPLICATION_JSON);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertEquals("Result is not of right content type", MediaType.APPLICATION_JSON, response.getEntity().getMediaType());
    }

    public void testGetICSCalendarRepresentation() throws Exception {
        request.getAttributes().put(UriTemplateParameters.SUBJECT_IDENTIFIER.attributeName()+ ".ics",SUBJECT_IDENTIFIER);
        expect(subjectDao.findSubjectByPersonId(SUBJECT_IDENTIFIER)).andReturn(subject);
        makeRequestType(MediaType.TEXT_CALENDAR);
        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertEquals("Result is not of right content type", MediaType.TEXT_CALENDAR, response.getEntity().getMediaType());
    }

    private void makeRequestType(MediaType requestType) {
        request.getClientInfo().setAcceptedMediaTypes(Arrays.asList(new Preference<MediaType>(requestType)));
    }

}
