package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.NotificationDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSubject;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSite;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createAssignment;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createBasicTemplate;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;

import java.util.Calendar;
import java.util.Arrays;

import static org.easymock.EasyMock.expect;

import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.ext.json.JsonRepresentation;
import org.json.JSONObject;

/**
 * @author Jalpa Patel
 */
public class NotificationResourceTest extends AuthorizedResourceTestCase<NotificationResource> {

    public static final String NOTIFICATION_IDENTIFIER = "notification_id";

    private NotificationDao notificationDao;
    private Notification notification;

    private StudySubjectAssignment studySubjectAssignment;
    public static final String ASSIGNMENT_IDENTIFIER = "assignment_id";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        notificationDao = registerDaoMockFor(NotificationDao.class);
        request.getAttributes().put(UriTemplateParameters.ASSIGNMENT_IDENTIFIER.attributeName(), ASSIGNMENT_IDENTIFIER);
        request.getAttributes().put(UriTemplateParameters.NOTIFICATION_IDENTIFIER.attributeName(), NOTIFICATION_IDENTIFIER);
        Subject subject = createSubject("1111", "Perry", "Duglas", createDate(1980, Calendar.JANUARY, 15, 0, 0, 0), Gender.MALE);
        subject.setId(11);
        Study study = createBasicTemplate("Joe's Study");
        Site site = createSite("NU");
        studySubjectAssignment = createAssignment(study,site,subject);
        ScheduledActivity scheduledActivity = Fixtures.setId(12, edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createReconsentScheduledActivity("A", 2007, Calendar.MARCH, 4));
        scheduledActivity.setGridId("SA-GRID");
        notification = new Notification(scheduledActivity);
        notification.setGridId(NOTIFICATION_IDENTIFIER);
        studySubjectAssignment.addNotification(notification);
    }

    @Override
    protected NotificationResource createAuthorizedResource() {
        NotificationResource resource = new NotificationResource();
        resource.setNotificationDao(notificationDao);
        resource.setXmlSerializer(xmlSerializer);
        return resource;
    }

    public void testAllowed() throws Exception {
        expect(notificationDao.getByGridId(NOTIFICATION_IDENTIFIER)).andReturn(notification);
        expect(xmlSerializer.createDocumentString(notification)).andReturn(MOCK_XML);

        replayMocks();

        assertAllowedMethods("GET", "PUT");
    }

    public void testGetWithAuthorizedRoles() {
        expect(notificationDao.getByGridId(NOTIFICATION_IDENTIFIER)).andReturn(notification);
        expect(xmlSerializer.createDocumentString(notification)).andReturn(MOCK_XML);

        replayMocks();
        assertRolesAllowedForMethod(Method.GET,
            STUDY_SUBJECT_CALENDAR_MANAGER,
            STUDY_TEAM_ADMINISTRATOR,
            DATA_READER);
    }

    public void testPutWithAuthorizedRoles() {
        expect(notificationDao.getByGridId(NOTIFICATION_IDENTIFIER)).andReturn(notification);
        expect(xmlSerializer.createDocumentString(notification)).andReturn(MOCK_XML);

        replayMocks();
        assertRolesAllowedForMethod(Method.PUT, STUDY_SUBJECT_CALENDAR_MANAGER);
    }

    public void testGetNotificationsXml() throws Exception {
        expect(notificationDao.getByGridId(NOTIFICATION_IDENTIFIER)).andReturn(notification);
        expect(xmlSerializer.createDocumentString(notification)).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

    public void testGet404WhenUnknownNotification() throws Exception {
        expect(notificationDao.getByGridId(NOTIFICATION_IDENTIFIER)).andReturn(null);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void testGet400WhenNoSubjectAssignmentIdentifierInRequest() throws Exception {
        request.getAttributes().put(UriTemplateParameters.NOTIFICATION_IDENTIFIER.attributeName(), null);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    public void testGetJSONRepresentation() throws Exception {
        expect(notificationDao.getByGridId(NOTIFICATION_IDENTIFIER)).andReturn(notification);
        requestJson();
        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertEquals("Result is not of right content type", MediaType.APPLICATION_JSON, response.getEntity().getMediaType());
    }

    public void testPutNotification() throws Exception {
        request.getAttributes().put(UriTemplateParameters.NOTIFICATION_IDENTIFIER.attributeName(), NOTIFICATION_IDENTIFIER);
        expect(notificationDao.getByGridId(NOTIFICATION_IDENTIFIER)).andReturn(notification);
        JSONObject entity = new JSONObject();
        entity.put("dismissed", true);
        request.setEntity(new JsonRepresentation(entity));
        notificationDao.save(notification);

        doPut();
        assertEquals("Result not success", 200, response.getStatus().getCode());
    }

    private void requestJson() {
        request.getClientInfo().setAcceptedMediaTypes(Arrays.asList(new Preference<MediaType>(MediaType.APPLICATION_JSON)));
    }


}

