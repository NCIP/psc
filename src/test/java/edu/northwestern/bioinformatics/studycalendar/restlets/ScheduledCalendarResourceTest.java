package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import static edu.northwestern.bioinformatics.studycalendar.test.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.test.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.xml.domain.NextScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.NextScheduledStudySegmentXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.ScheduledCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.ScheduledStudySegmentXmlSerializer;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
import static org.easymock.EasyMock.expect;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.resource.InputRepresentation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

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

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        serializer = registerMockFor(ScheduledCalendarXmlSerializer.class);
        subjectService = registerMockFor(SubjectService.class);
        scheduledSegmentSerializer = registerMockFor(ScheduledStudySegmentXmlSerializer.class);
        studyDao = registerDaoMockFor(StudyDao.class);
        studySubjectAssignmentDao = registerDaoMockFor(StudySubjectAssignmentDao.class);
        nextScheduledStudySegmentSerializer = registerMockFor(NextScheduledStudySegmentXmlSerializer.class);

        calendar = new ScheduledCalendar();
        assigment = setGridId(ASSIGNMENT_IDENTIFIER, createAssignment(calendar));

        request.getAttributes().put(UriTemplateParameters.STUDY_IDENTIFIER.attributeName(), STUDY_IDENTIFIER_ENCODED);
        request.getAttributes().put(UriTemplateParameters.ASSIGNMENT_IDENTIFIER.attributeName(), ASSIGNMENT_IDENTIFIER);
    }

    @Override
    protected ScheduledCalendarResource createAuthorizedResource() {
        ScheduledCalendarResource resource = new ScheduledCalendarResource();
        resource.setXmlSerializer(serializer);
        resource.setSubjectService(subjectService);
        resource.setStudySubjectAssignmentDao(studySubjectAssignmentDao);
        resource.setStudyDao(studyDao);
        resource.setScheduledStudySegmentXmlSerializer(scheduledSegmentSerializer);
        resource.setNextScheduledStudySegmentXmlSerializer(nextScheduledStudySegmentSerializer);
        return resource;
    }

    ////// GET tests

    public void testGetXmlForScheduledStudies() throws IOException {
        expectResolvedSubjectAssignment();
        expectSerializeScheduledCalendar();
        doGet();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertResponseIsCreatedXml();
    }

    public void testGetICSCalendarForScheduledStudies() throws IOException {
        expectResolvedSubjectAssignment();
        request.setResourceRef(String.format("%s/studies/%s/schedules/%s.ics", ROOT_URI, STUDY_IDENTIFIER_ENCODED, ASSIGNMENT_IDENTIFIER));
        request.getAttributes().put(UriTemplateParameters.ASSIGNMENT_IDENTIFIER.attributeName() + ".ics", ASSIGNMENT_IDENTIFIER);

        doGet();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertEquals("Result is not right content type", MediaType.TEXT_CALENDAR, response.getEntity().getMediaType());
    }

    public void testGetAllowedOnlyForSubjectCoordinator() {
        assertRolesAllowedForMethod(Method.GET, Role.SUBJECT_COORDINATOR);
    }

    ////// POST tests

    public void testPostAllowedOnlyForSubjectCoordinator() {
        assertRolesAllowedForMethod(Method.POST, Role.SUBJECT_COORDINATOR);
    }

    public void testPostXmlForScheduledSegment() throws Exception {
        NextScheduledStudySegment nextSgmtSchdScheduled = new NextScheduledStudySegment();
        nextSgmtSchdScheduled.setStudySegment(createNamedInstance("Screening", StudySegment.class));
        nextSgmtSchdScheduled.setStartDate(createDate(Calendar.JANUARY, 13, 2007));
        nextSgmtSchdScheduled.setMode(NextStudySegmentMode.IMMEDIATE);

        ScheduledStudySegment schSegment = new ScheduledStudySegment();

        expectResolvedSubjectAssignment();
        expectReadXmlFromRequestAs(nextSgmtSchdScheduled);
        expect(subjectService.scheduleStudySegment(assigment, nextSgmtSchdScheduled.getStudySegment(), nextSgmtSchdScheduled.getStartDate(), nextSgmtSchdScheduled.getMode()))
                .andReturn(schSegment);
        expectObjectXmlized(schSegment);
        doPost();
        assertEquals("Result not success", 201, response.getStatus().getCode());
    }

    ////// Expect methods

    private void expectResolvedSubjectAssignment() {
        expect(studyDao.getByAssignedIdentifier("EC golf")).andReturn(new Study());
        expect(studySubjectAssignmentDao.getByGridId(ASSIGNMENT_IDENTIFIER)).andReturn(assigment);
    }

    private void expectSerializeScheduledCalendar() {
        expect(serializer.createDocumentString(calendar)).andReturn(MOCK_XML);
    }

    ////// Helper methods

    public StudySubjectAssignment createAssignment(ScheduledCalendar calendar) {
        StudySubjectAssignment assignment = new StudySubjectAssignment();
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
