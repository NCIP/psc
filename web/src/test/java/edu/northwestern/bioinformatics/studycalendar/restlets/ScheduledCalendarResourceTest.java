package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.NextStudySegmentMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.xml.domain.NextScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.NextScheduledStudySegmentXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.ScheduledCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.ScheduledStudySegmentXmlSerializer;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
import static org.easymock.EasyMock.expect;

/**
 * @author John Dzak
 */
public class ScheduledCalendarResourceTest extends AuthorizedResourceTestCase<ScheduledCalendarResource> {
    private static final String STUDY_IDENTIFIER_ENCODED = "EC+golf";
    private static final String ASSIGNMENT_IDENTIFIER = "assignment-grid-0";

    private ScheduledCalendar calendar;
    private StudySubjectAssignment assigment;
    private ScheduledCalendarXmlSerializer serializer;
    private StudyDao studyDao;
    private StudySubjectAssignmentDao studySubjectAssignmentDao;
    private ScheduledStudySegmentXmlSerializer scheduledSegmentSerializer;
    private NextScheduledStudySegmentXmlSerializer nextScheduledStudySegmentSerializer;
    private SubjectService subjectService;
    private TemplateService templateService;
    private Study study;
    private ScheduleService scheduleService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        serializer = registerMockFor(ScheduledCalendarXmlSerializer.class);
        subjectService = registerMockFor(SubjectService.class);
        templateService = registerMockFor(TemplateService.class);
        scheduleService = registerMockFor(ScheduleService.class);
        scheduledSegmentSerializer = registerMockFor(ScheduledStudySegmentXmlSerializer.class);
        studyDao = registerDaoMockFor(StudyDao.class);
        studySubjectAssignmentDao = registerDaoMockFor(StudySubjectAssignmentDao.class);
        nextScheduledStudySegmentSerializer = registerMockFor(NextScheduledStudySegmentXmlSerializer.class);

        calendar = new ScheduledCalendar();
        Subject subject = Fixtures.createSubject("Perry", "Duglas");
        study = Fixtures.createSingleEpochStudy("EC golf", "Treatment");
        Site site = Fixtures.createSite("NU");
        StudySubjectAssignment assignment = Fixtures.createAssignment(study,site,subject);
        assignment.setScheduledCalendar(calendar);
        assigment = setGridId(ASSIGNMENT_IDENTIFIER, assignment);

        request.getAttributes().put(UriTemplateParameters.STUDY_IDENTIFIER.attributeName(), STUDY_IDENTIFIER_ENCODED);
        request.getAttributes().put(UriTemplateParameters.ASSIGNMENT_IDENTIFIER.attributeName(), ASSIGNMENT_IDENTIFIER);
    }

    @Override
    protected ScheduledCalendarResource createAuthorizedResource() {
        ScheduledCalendarResource resource = new ScheduledCalendarResource();
        resource.setXmlSerializer(serializer);
        resource.setSubjectService(subjectService);
        resource.setTemplateService(templateService);
        resource.setStudySubjectAssignmentDao(studySubjectAssignmentDao);
        resource.setStudyDao(studyDao);
        resource.setScheduledStudySegmentXmlSerializer(scheduledSegmentSerializer);
        resource.setNextScheduledStudySegmentXmlSerializer(nextScheduledStudySegmentSerializer);
        resource.setScheduleService(scheduleService);
        return resource;
    }

    public void testGetAndPutAndDeleteAllowed() throws Exception {
        expectResolvedSubjectAssignment();
        expectSerializeScheduledCalendar();
        replayMocks();
        assertAllowedMethods("POST", "GET");
    }

    public void testSubjectRolesMayView() {
        expectResolvedSubjectAssignment();
        expectSerializeScheduledCalendar();
        replayMocks();
        assertRolesAllowedForMethod(Method.GET,
                STUDY_SUBJECT_CALENDAR_MANAGER,
                STUDY_TEAM_ADMINISTRATOR,
                DATA_READER);
    }

    public void testOnlySscmMayPost() {
        expectResolvedSubjectAssignment();
        expectSerializeScheduledCalendar();
        replayMocks();
        assertRolesAllowedForMethod(Method.POST,
            STUDY_SUBJECT_CALENDAR_MANAGER);
    }

    ////// GET tests

    public void testGetXmlForScheduledStudies() throws IOException {
        expectResolvedSubjectAssignment();
        expectSerializeScheduledCalendar();
        doGet();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertResponseIsCreatedXml();
    }

    public void testGetXmlForNoStudy() throws Exception {
        expect(studyDao.getByAssignedIdentifier("EC golf")).andReturn(null);

        doGet();
        assertEquals("Result not 404", 404, response.getStatus().getCode());
    }

    public void testGetICSCalendarForScheduledStudies() throws IOException {
        expectResolvedSubjectAssignment();
        request.setResourceRef(String.format("%s/studies/%s/schedules/%s.ics", ROOT_URI, STUDY_IDENTIFIER_ENCODED, ASSIGNMENT_IDENTIFIER));
        request.getAttributes().put(UriTemplateParameters.ASSIGNMENT_IDENTIFIER.attributeName() + ".ics", ASSIGNMENT_IDENTIFIER);

        List<Preference<MediaType>> preferences = new ArrayList<Preference<MediaType>>();
        preferences.add(new Preference<MediaType>(MediaType.TEXT_CALENDAR));
        request.getClientInfo().setAcceptedMediaTypes(preferences);

        doGet();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertEquals("Result is not right content type", MediaType.TEXT_CALENDAR, response.getEntity().getMediaType());
    }

    ////// POST tests

    public void testPostXmlForScheduledSegment() throws Exception {
        NextScheduledStudySegment nextSgmtSchdScheduled = new NextScheduledStudySegment();
        StudySegment studySegment = createNamedInstance("Screening", StudySegment.class);
        nextSgmtSchdScheduled.setStudySegment(studySegment);
        nextSgmtSchdScheduled.setStartDate(createDate(Calendar.JANUARY, 13, 2007));
        nextSgmtSchdScheduled.setMode(NextStudySegmentMode.IMMEDIATE);

        ScheduledStudySegment schSegment = new ScheduledStudySegment();
        expect(templateService.findStudy(studySegment)).andReturn(study);
        expectResolvedSubjectAssignment();
        expectReadXmlFromRequestAs(nextSgmtSchdScheduled);
        expectResolveNextScheduledStudySegment(nextSgmtSchdScheduled);
        expect(subjectService.scheduleStudySegment(assigment, nextSgmtSchdScheduled.getStudySegment(), nextSgmtSchdScheduled.getStartDate(), nextSgmtSchdScheduled.getMode()))
                .andReturn(schSegment);
        expectObjectXmlized(schSegment);
        doPost();
        assertEquals("Result not success", 201, response.getStatus().getCode());
    }

    public void testPostXmlForUnmatchedScheduledSegmentForStudy() throws Exception {
        NextScheduledStudySegment nextSgmtSchdScheduled = new NextScheduledStudySegment();
        StudySegment studySegment = createNamedInstance("Screening", StudySegment.class);
        nextSgmtSchdScheduled.setStudySegment(studySegment);
        nextSgmtSchdScheduled.setStartDate(createDate(Calendar.JANUARY, 13, 2007));
        nextSgmtSchdScheduled.setMode(NextStudySegmentMode.IMMEDIATE);

        expect(templateService.findStudy(studySegment)).andReturn(new Study());
        expectResolvedSubjectAssignment();
        expectReadXmlFromRequestAs(nextSgmtSchdScheduled);
        expectResolveNextScheduledStudySegment(nextSgmtSchdScheduled);
        doPost();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    public void test400ForUnsupportedEntityContentType() throws Exception {
        request.setEntity("wrong type", MediaType.TEXT_PLAIN);
        NextScheduledStudySegment nextSgmtSchdScheduled = new NextScheduledStudySegment();
        StudySegment studySegment = createNamedInstance("Screening", StudySegment.class);
        nextSgmtSchdScheduled.setStudySegment(studySegment);
        expectResolvedSubjectAssignment();

        doPost();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    ////// Expect methods

    private void expectResolvedSubjectAssignment() {
        expect(studyDao.getByAssignedIdentifier("EC golf")).andReturn(study);
        expect(studySubjectAssignmentDao.getByGridId(ASSIGNMENT_IDENTIFIER)).andReturn(assigment);
    }

    private void expectSerializeScheduledCalendar() {
        expect(serializer.createDocumentString(calendar)).andReturn(MOCK_XML);
    }

    private void expectResolveNextScheduledStudySegment(NextScheduledStudySegment nextSgmtSchdScheduled){
        expect(scheduleService.resolveNextScheduledStudySegment(nextSgmtSchdScheduled)).andReturn(nextSgmtSchdScheduled);
    }

    ////// Helper methods

    public StudySubjectAssignment createAssignment(ScheduledCalendar calendar) {
        Subject subject = Fixtures.createSubject("Perry", "Duglas");
        Study study = Fixtures.createSingleEpochStudy("Study", "Treatment");
        Site site = Fixtures.createSite("NU");
        StudySubjectAssignment assignment = Fixtures.createAssignment(study,site,subject);
        assignment.setScheduledCalendar(calendar);
        return assignment;
    }

    protected void expectReadXmlFromRequestAs(NextScheduledStudySegment expectedRead) throws Exception {
        final InputStream in = registerMockFor(InputStream.class);
        request.setEntity(new InputRepresentation(in, MediaType.TEXT_XML));

        expect(nextScheduledStudySegmentSerializer.readDocument(in)).andReturn(expectedRead);
    }

    @SuppressWarnings({"unchecked"})
    protected void expectObjectXmlized(ScheduledStudySegment schdSegment) {
        expect(scheduledSegmentSerializer.createDocumentString(schdSegment)).andReturn(MOCK_XML);
    }
}
