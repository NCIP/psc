package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.ScheduledCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.ScheduledStudySegmentXmlSerializer;
import edu.nwu.bioinformatics.commons.DateUtils;
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
    private StudySubjectAssignmentDao studySubjectAssignmentDao;
    private ScheduledStudySegmentXmlSerializer scheduledSegmentSerializer;
    private SubjectService subjectService;

    protected void setUp() throws Exception {
        super.setUp();

        serializer = registerMockFor(ScheduledCalendarXmlSerializer.class);
        subjectService = registerMockFor(SubjectService.class);
        scheduledSegmentSerializer = registerMockFor(ScheduledStudySegmentXmlSerializer.class);
        studySubjectAssignmentDao = registerDaoMockFor(StudySubjectAssignmentDao.class);

        calendar = new ScheduledCalendar();
        assigment = setGridId(ASSIGNMENT_IDENTIFIER, createAssignment(calendar));

        request.getAttributes().put(UriTemplateParameters.STUDY_IDENTIFIER.attributeName(), STUDY_IDENTIFIER_ENCODED);
        request.getAttributes().put(UriTemplateParameters.ASSIGNMENT_IDENTIFIER.attributeName(), ASSIGNMENT_IDENTIFIER);
    }

    protected ScheduledCalendarResource createResource() {
        ScheduledCalendarResource resource = new ScheduledCalendarResource();
        resource.setXmlSerializer(serializer);
        resource.setSubjectService(subjectService);
        resource.setStudySubjectAssignmentDao(studySubjectAssignmentDao);
        resource.setScheduledStudySegmentXmlSerializer(scheduledSegmentSerializer);
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

    public void testGetAllowedOnlyForSubjectCoordinator() {
        assertRolesAllowedForMethod(Method.GET, Role.SUBJECT_COORDINATOR);
    }

    ////// POST tests
    public void testPostAllowedOnlyForSubjectCoordinator() {
        assertRolesAllowedForMethod(Method.POST, Role.SUBJECT_COORDINATOR);
    }

    public void testPostXmlForScheduledSegment() throws Exception {
        ScheduledStudySegment schSegment = new ScheduledStudySegment();
        schSegment.setStudySegment(Fixtures.createNamedInstance("Screening", StudySegment.class));
        schSegment.setStartDate(DateUtils.createDate(Calendar.JANUARY, 13, 2007));

        expectResolvedSubjectAssignment();
        expectReadXmlFromRequestAs(schSegment);
        expect(subjectService.scheduleStudySegment(assigment, schSegment.getStudySegment(), schSegment.getStartDate(), NextStudySegmentMode.PER_PROTOCOL ))
                .andReturn(schSegment);
        expectObjectXmlized(schSegment);
        doPost();
        assertEquals("Result not success", 201, response.getStatus().getCode());
    }

    ////// Expect methods
    private void expectResolvedSubjectAssignment() {
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

    protected void expectReadXmlFromRequestAs(ScheduledStudySegment expectedRead) throws Exception {
        final InputStream in = registerMockFor(InputStream.class);
        request.setEntity(new InputRepresentation(in, MediaType.TEXT_XML));

        expect(scheduledSegmentSerializer.readDocument(in)).andReturn(expectedRead);
    }

    @SuppressWarnings({ "unchecked" })
    protected void expectObjectXmlized(ScheduledStudySegment schdSegment) {
        expect(scheduledSegmentSerializer.createDocumentString(schdSegment)).andReturn(MOCK_XML);
    }
}
