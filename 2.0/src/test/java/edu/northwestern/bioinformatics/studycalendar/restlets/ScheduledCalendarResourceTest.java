package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.ScheduledCalendarXmlSerializer;
import static org.easymock.EasyMock.expect;
import org.restlet.data.Method;

import java.io.IOException;

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

    protected void setUp() throws Exception {
        super.setUp();

        serializer = registerMockFor(ScheduledCalendarXmlSerializer.class);
        studySubjectAssignmentDao = registerDaoMockFor(StudySubjectAssignmentDao.class);

        calendar = new ScheduledCalendar();
        assigment = setGridId(ASSIGNMENT_IDENTIFIER, createAssignment(calendar));

        request.getAttributes().put(UriTemplateParameters.STUDY_IDENTIFIER.attributeName(), STUDY_IDENTIFIER_ENCODED);
        request.getAttributes().put(UriTemplateParameters.ASSIGNMENT_IDENTIFIER.attributeName(), ASSIGNMENT_IDENTIFIER);
    }

    protected ScheduledCalendarResource createResource() {
        ScheduledCalendarResource resource = new ScheduledCalendarResource();
        resource.setXmlSerializer(serializer);
        resource.setStudySubjectAssignmentDao(studySubjectAssignmentDao);
        return resource;
    }

    public void testGetXmlForScheduledStudies() throws IOException {
        expectResolvedSubjectAssignment();
        expectSerializeScheduledCalendar();
        doGet();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertResponseIsCreatedXml();
    }

    public void testGetAllowedForSubjectCoordinator() {
        assertRolesAllowedForMethod(Method.GET, Role.SUBJECT_COORDINATOR);
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
}
