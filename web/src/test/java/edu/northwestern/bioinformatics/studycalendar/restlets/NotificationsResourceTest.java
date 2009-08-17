package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSubject;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSite;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createAssignment;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createBasicTemplate;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
import java.util.Calendar;
import java.util.Arrays;

import static org.easymock.EasyMock.expect;
import org.restlet.data.Status;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;

/**
 * @author Jalpa Patel
 */
public class NotificationsResourceTest extends ResourceTestCase<NotificationsResource> {

    public static final String ASSIGNMENT_IDENTIFIER = "assignment_id";

    private StudySubjectAssignmentDao studySubjectAssignmentDao;
    private StudySubjectAssignment studySubjectAssignment;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        studySubjectAssignmentDao = registerDaoMockFor(StudySubjectAssignmentDao.class);
        request.getAttributes().put(UriTemplateParameters.ASSIGNMENT_IDENTIFIER.attributeName(), ASSIGNMENT_IDENTIFIER);
        Subject subject = createSubject("1111", "Perry", "Duglas", createDate(1980, Calendar.JANUARY, 15, 0, 0, 0), Gender.MALE);
        subject.setId(11);
        Study study = createBasicTemplate("Joe's Study");
        Site site = createSite("NU");
        studySubjectAssignment = createAssignment(study,site,subject);
        studySubjectAssignment.setGridId(ASSIGNMENT_IDENTIFIER);
        ScheduledActivity scheduledActivity = Fixtures.setId(12, Fixtures.createReconsentScheduledActivity("A", 2007, Calendar.MARCH, 4));
        scheduledActivity.setGridId("SA-GRID");
        Notification notification = new Notification(scheduledActivity);
        studySubjectAssignment.addNotification(notification);
    }

    @Override
    protected NotificationsResource createResource() {
        NotificationsResource resource = new NotificationsResource();
        resource.setStudySubjectAssignmentDao(studySubjectAssignmentDao);
        resource.setXmlSerializer(xmlSerializer);
        return resource;
    }

    public void testGetAllowed() throws Exception {
        assertAllowedMethods("GET");
    }

    public void testGetNotificationsXml() throws Exception {
        expect(studySubjectAssignmentDao.getByGridId(ASSIGNMENT_IDENTIFIER)).andReturn(studySubjectAssignment);

        expect(xmlSerializer.createDocumentString(studySubjectAssignment.getNotifications())).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

    public void testGet404WhenUnknownSubjectAssignment() throws Exception {
        expect(studySubjectAssignmentDao.getByGridId(ASSIGNMENT_IDENTIFIER)).andReturn(null);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void testGet400WhenNoSubjectAssignmentIdentifierInRequest() throws Exception {
        request.getAttributes().put(UriTemplateParameters.ASSIGNMENT_IDENTIFIER.attributeName(), null);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    public void testGetJSONRepresentation() throws Exception {
        expect(studySubjectAssignmentDao.getByGridId(ASSIGNMENT_IDENTIFIER)).andReturn(studySubjectAssignment);
        requestJson();
        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertEquals("Result is not of right content type", MediaType.APPLICATION_JSON, response.getEntity().getMediaType());
    }
    
    private void requestJson() {
        request.getClientInfo().setAcceptedMediaTypes(Arrays.asList(new Preference<MediaType>(MediaType.APPLICATION_JSON)));
    }


}
