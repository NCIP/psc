/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Gender;
import edu.northwestern.bioinformatics.studycalendar.domain.Notification;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Status;

import java.util.Arrays;
import java.util.Calendar;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createBasicTemplate;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
import static org.easymock.EasyMock.expect;

/**
 * @author Jalpa Patel
 */
public class NotificationsResourceTest extends AuthorizedResourceTestCase<NotificationsResource> {

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
    protected NotificationsResource createAuthorizedResource() {
        NotificationsResource resource = new NotificationsResource();
        resource.setStudySubjectAssignmentDao(studySubjectAssignmentDao);
        resource.setXmlSerializer(xmlSerializer);
        return resource;
    }

    public void testGetAllowed() throws Exception {
        expectFoundStudySubjectAssignment(studySubjectAssignment);
        replayMocks();

        assertAllowedMethods("GET");
    }

    public void testGetWithAuthorizedRoles() {
        expectFoundStudySubjectAssignment(studySubjectAssignment);
        replayMocks();
        assertRolesAllowedForMethod(Method.GET,
            STUDY_SUBJECT_CALENDAR_MANAGER,
            STUDY_TEAM_ADMINISTRATOR,
            DATA_READER);
    }

    public void testGetNotificationsXml() throws Exception {
        expectFoundStudySubjectAssignment(studySubjectAssignment);
        expect(xmlSerializer.createDocumentString(studySubjectAssignment.getNotifications())).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

    public void testGet404WhenUnknownSubjectAssignment() throws Exception {
        expectFoundStudySubjectAssignment(null);
        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void testGet400WhenNoSubjectAssignmentIdentifierInRequest() throws Exception {
        UriTemplateParameters.ASSIGNMENT_IDENTIFIER.removeFrom(request);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    public void testGetJSONRepresentation() throws Exception {
        expectFoundStudySubjectAssignment(studySubjectAssignment);
        requestJson();
        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertEquals("Result is not of right content type", MediaType.APPLICATION_JSON, response.getEntity().getMediaType());
    }
    
    private void requestJson() {
        request.getClientInfo().setAcceptedMediaTypes(Arrays.asList(new Preference<MediaType>(MediaType.APPLICATION_JSON)));
    }


    private void expectFoundStudySubjectAssignment(StudySubjectAssignment expectedStudySubjectAssignmnent) {
        expect(studySubjectAssignmentDao.getByGridId(ASSIGNMENT_IDENTIFIER)).andReturn(expectedStudySubjectAssignmnent).anyTimes();
    }
}
