package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.service.StudySiteService;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Role.SUBJECT_COORDINATOR;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudySubjectAssignmentXmlSerializer;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createBasicTemplate;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;

import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Arrays;

import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.restlet.data.Status;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import static org.easymock.EasyMock.expect;
import org.easymock.IExpectationSetters;
import gov.nih.nci.cabig.ctms.lang.NowFactory;
import gov.nih.nci.cabig.ctms.lang.StaticNowFactory;

/**
 * @author Jalpa Patel
 */
public class SubjectCoordinatorSchedulesResourceTest extends ResourceTestCase<SubjectCoordinatorSchedulesResource>{
    private UserService userService;
    private StudySiteService studySiteService;
    private static final String USERNAME = "subjectCo";
    private User user;
    private List<StudySubjectAssignment> studySubjectAssignments = new ArrayList<StudySubjectAssignment>();
    private NowFactory nowFactory;
    private StudySite studySite;
    public void setUp() throws Exception {
        super.setUp();
        userService = registerMockFor(UserService.class);
        studySiteService = registerMockFor(StudySiteService.class);
        request.getAttributes().put(UriTemplateParameters.USERNAME.attributeName(), USERNAME);
        Role role = SUBJECT_COORDINATOR;
        user = Fixtures.createUser(USERNAME, role);
        Fixtures.setUserRoles(user,role);
        PscGuard.setCurrentAuthenticationToken(request, new UsernamePasswordAuthenticationToken(USERNAME, USERNAME, new Role[] { Role.SUBJECT_COORDINATOR}));
        nowFactory = new StaticNowFactory();
        xmlSerializer = registerMockFor(StudySubjectAssignmentXmlSerializer.class);

        Subject subject = createSubject("1111", "Perry", "Duglas", createDate(1980, Calendar.JANUARY, 15, 0, 0, 0), Gender.MALE);
        Study study = createBasicTemplate("Joe's Study");
        Site site = createSite("NU");
        studySite = createStudySite(study, site);
        StudySegment studySegment = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);
        studySegment.setName("Segment1");
        ScheduledStudySegment scheduledStudySegment = Fixtures.createScheduledStudySegment(studySegment);
        StudySubjectAssignment studySubjectAssignment = createAssignment(studySite,subject);
        studySubjectAssignment.getScheduledCalendar().addStudySegment(scheduledStudySegment);
        studySubjectAssignments.add(studySubjectAssignment);

        ((StudySubjectAssignmentXmlSerializer)xmlSerializer).setIncludeScheduledCalendar(true);
    }

    protected SubjectCoordinatorSchedulesResource createResource() {
        SubjectCoordinatorSchedulesResource resource = new SubjectCoordinatorSchedulesResource();
        resource.setUserService(userService);
        resource.setStudySiteService(studySiteService);
        resource.setXmlSerializer(xmlSerializer);
        resource.setNowFactory(nowFactory);
        return resource;
    }

    public void testGetAllowed() throws Exception {
        assertAllowedMethods("GET");
    }

    public void test400ForNoUsername() throws Exception {
        request.getAttributes().put(UriTemplateParameters.USERNAME.attributeName(), null);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    public void test404ForUnknownUser() throws Exception {
        expect(userService.getUserByName(USERNAME)).andReturn(null);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }
    
    public void testGetXmlRepresentation() throws Exception {
        user.setStudySubjectAssignments(studySubjectAssignments);
        expectGetCurrentUser();
        expect(userService.getUserByName(USERNAME)).andReturn(user);
        expect(xmlSerializer.createDocumentString(studySubjectAssignments)).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }
    public void testGetJSONRepresentation() throws Exception {
        user.setStudySubjectAssignments(studySubjectAssignments);
        request.getAttributes().put(UriTemplateParameters.USERNAME.attributeName()+".json", USERNAME);
        expectGetCurrentUser();
        expect(userService.getUserByName(USERNAME)).andReturn(user);
        makeRequestType(MediaType.APPLICATION_JSON);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertEquals("Result is not of right content type", MediaType.APPLICATION_JSON, response.getEntity().getMediaType());
    }

    public void testGetICSCalendarRepresentation() throws Exception {
        user.setStudySubjectAssignments(studySubjectAssignments);
        request.getAttributes().put(UriTemplateParameters.USERNAME.attributeName()+".ics", USERNAME);
        expectGetCurrentUser();
        expect(userService.getUserByName(USERNAME)).andReturn(user);
        makeRequestType(MediaType.TEXT_CALENDAR);
        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertEquals("Result is not of right content type", MediaType.TEXT_CALENDAR, response.getEntity().getMediaType());
    }

    public void testGetColleageSchedulesRepresentation() throws Exception {
        User colleage = makeRequestForColleageSchedules();
        studySubjectAssignments.get(0).setSubjectCoordinator(colleage);
        expectGetCurrentUser();
        expect(userService.getUserByName(colleage.getUsername())).andReturn(colleage);
        expect(studySiteService.getAllStudySitesForSubjectCoordinator(user)).andReturn(Arrays.asList(studySite));
        expect(studySiteService.getAllStudySitesForSubjectCoordinator(colleage)).andReturn(Arrays.asList(studySite));
        expect(xmlSerializer.createDocumentString(studySubjectAssignments)).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();

    }

    public void testGet404WhenNoAssignmentsFoundForColleage() throws Exception {
        User colleage = makeRequestForColleageSchedules();
        expectGetCurrentUser();
        expect(userService.getUserByName(colleage.getUsername())).andReturn(colleage);
        expect(studySiteService.getAllStudySitesForSubjectCoordinator(user)).andReturn(Arrays.asList(studySite));
        expect(studySiteService.getAllStudySitesForSubjectCoordinator(colleage)).andReturn(Arrays.asList(studySite));
        
        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    private void makeRequestType(MediaType requestType) {
        request.getClientInfo().setAcceptedMediaTypes(Arrays.asList(new Preference<MediaType>(requestType)));
    }

    private User makeRequestForColleageSchedules() {
        String colleageName = "ColleageSubjetCo";
        Role role = SUBJECT_COORDINATOR;
        User colleage = Fixtures.createUser(colleageName, role);
        Fixtures.setUserRoles(colleage,role);
        request.getAttributes().put(UriTemplateParameters.USERNAME.attributeName(), colleageName);
        return colleage;
    }

    private IExpectationSetters<User> expectGetCurrentUser() {
        return expect(userService.getUserByName(user.getName())).andReturn(user);
    }
}
