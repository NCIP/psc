package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.PscUserBuilder;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;

import java.util.Calendar;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode.*;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_SUBJECT_CALENDAR_MANAGER;
import static org.easymock.EasyMock.expect;

/**
 * @author Jalpa Patel
 */
public class BatchUpdatesResourceTest extends AuthorizedResourceTestCase<BatchUpdatesResource>  {
    private ScheduledActivityDao scheduledActivityDao;
    private ScheduleService scheduleService;
    private ScheduledActivity sa1, sa2;
    private JSONObject entity = new JSONObject();
    private JSONObject responseEntity = new JSONObject();
    private JSONObject activityState1,activityState2;
    private static final String SA1_GRID = "1111";
    private static final String SA2_GRID = "2222";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        scheduledActivityDao = registerDaoMockFor(ScheduledActivityDao.class);
        scheduleService = registerMockFor(ScheduleService.class);
        sa1 = setGridId(SA1_GRID, setId(12,
            createScheduledActivity("A", 2008, Calendar.MARCH, 4)));
        sa2 = setGridId(SA2_GRID, setId(13,
            createScheduledActivity("B", 2008, Calendar.MARCH, 10)));

        Subject subject = createSubject("Perry", "Duglas");
        Study study = createBasicTemplate("Study");
        Site site = createSite("NU");
        StudySubjectAssignment studySubjectAssignment = createAssignment(study,site,subject);
        studySubjectAssignment.setGridId("ssa1111");
        ScheduledStudySegment scheduledStudySegment =  createScheduledStudySegment(DateTools.createDate(2008, Calendar.MARCH, 1), 20);
        studySubjectAssignment.getScheduledCalendar().addStudySegment(
            scheduledStudySegment);
        sa1.setScheduledStudySegment(scheduledStudySegment);
        sa2.setScheduledStudySegment(scheduledStudySegment);

        activityState1 = createJSONFormatForRequest("canceled", "2008-03-02", "Just Canceled");
        activityState2 = createJSONFormatForRequest("scheduled", "2008-03-11", "Move by 1 day");
        entity.put(SA1_GRID, activityState1);
        entity.put(SA2_GRID, activityState2);
        request.setEntity(new JsonRepresentation(entity));
    }

    @Override
    protected BatchUpdatesResource createAuthorizedResource() {
        BatchUpdatesResource resource = new BatchUpdatesResource();
        resource.setScheduledActivityDao(scheduledActivityDao);
        resource.setScheduleService(scheduleService);
        return resource;
    }

    public void testAllowedMethods() throws Exception {
        assertAllowedMethods("POST");
    }

    public void testPostWithAuthorizedRoles() {
        assertRolesAllowedForMethod(Method.POST, STUDY_SUBJECT_CALENDAR_MANAGER);
    }

    public void testPostWithInvalidActivityId() throws Exception {
        expect(scheduledActivityDao.getByGridId(SA1_GRID)).andReturn(null);
        expect(scheduledActivityDao.getByGridId(SA2_GRID)).andReturn(sa2);
        createResponseMessage(SA1_GRID, Status.CLIENT_ERROR_NOT_FOUND.getCode());
        scheduledActivityDao.save(sa2);
        createResponseMessage(SA2_GRID, Status.SUCCESS_CREATED.getCode());
        response.setEntity(new JsonRepresentation(responseEntity));

        doPost();
        assertResponseStatus(Status.SUCCESS_MULTI_STATUS);
        JSONObject responseText = new JSONObject(response.getEntity().getText());
        assertEquals("Wrong Status code for activity with Invalid gridid",
                Status.CLIENT_ERROR_NOT_FOUND.getCode(), ((JSONObject)responseText.get(SA1_GRID)).get("Status"));
        assertEquals("Wrong Status code for secaond activity",
                Status.SUCCESS_CREATED.getCode(), ((JSONObject)responseText.get(SA2_GRID)).get("Status"));

        assertStateContents(SCHEDULED, 2008, Calendar.MARCH, 11, "Move by 1 day", sa2.getCurrentState());
    }


    public void testPostForTwoAssignmentsThatBelongsToUser() throws Exception {
        String SA1_GRID ="1111111";
        String SA2_GRID ="2222222";
        ScheduledActivity sa1 = setGridId(SA1_GRID, setId(12, createScheduledActivity("A1", 2008, Calendar.MARCH, 4)));
        ScheduledActivity sa2 = setGridId(SA2_GRID, setId(13, createScheduledActivity("B2", 2008, Calendar.MARCH, 10)));

        Subject subject = createSubject("Perry", "Duglas");
        Study study = createBasicTemplate("Study");
        Study study2 = createBasicTemplate("Study2");
        Site site = createSite("NU");

        StudySubjectAssignment studySubjectAssignment1 = createAssignment(study,site,subject);
        StudySubjectAssignment studySubjectAssignment2 = createAssignment(study2,site,subject);
        studySubjectAssignment1.setGridId("ssa11111");
        studySubjectAssignment2.setGridId("ssa22222");

        ScheduledStudySegment scheduledStudySegment1 =  createScheduledStudySegment(DateTools.createDate(2008, Calendar.MARCH, 1), 21);
        ScheduledStudySegment scheduledStudySegment2 =  createScheduledStudySegment(DateTools.createDate(2008, Calendar.MARCH, 2), 22);
        studySubjectAssignment1.getScheduledCalendar().addStudySegment(scheduledStudySegment1);
        studySubjectAssignment2.getScheduledCalendar().addStudySegment(scheduledStudySegment2);
        sa1.setScheduledStudySegment(scheduledStudySegment1);
        sa2.setScheduledStudySegment(scheduledStudySegment2);

        JSONObject activityState1 = createJSONFormatForRequest("canceled", "2008-03-02", "Just Canceled");
        JSONObject activityState2 = createJSONFormatForRequest("scheduled", "2008-03-11", "Move by 1 day");
        JSONObject entity = new JSONObject();
        entity.put(SA1_GRID, activityState1);
        entity.put(SA2_GRID, activityState2);
        request.setEntity(new JsonRepresentation(entity));


        expect(scheduledActivityDao.getByGridId(SA1_GRID)).andReturn(sa1);
        expect(scheduledActivityDao.getByGridId(SA2_GRID)).andReturn(sa2);

        scheduledActivityDao.save(sa1);
        createResponseMessage(SA1_GRID, Status.SUCCESS_CREATED.getCode());
        scheduledActivityDao.save(sa2);
        createResponseMessage(SA2_GRID, Status.SUCCESS_CREATED.getCode());
        response.setEntity(new JsonRepresentation(responseEntity));

        doPost();
        assertResponseStatus(Status.SUCCESS_MULTI_STATUS);
        JSONObject responseText = new JSONObject(response.getEntity().getText());
        assertEquals("Wrong Status code for secaond activity",
                Status.SUCCESS_CREATED.getCode(), ((JSONObject)responseText.get(SA1_GRID)).get("Status"));
        assertEquals("Wrong Status code for secaond activity",
                Status.SUCCESS_CREATED.getCode(), ((JSONObject)responseText.get(SA2_GRID)).get("Status"));

        assertStateContents(SCHEDULED, 2008, Calendar.MARCH, 11, "Move by 1 day", sa2.getCurrentState());
    }


    public void testPostForTwoAssignmentsWhereOneBelongsToUser() throws Exception {
        String SA1_GRID ="1111111";
        String SA2_GRID ="2222222";
        ScheduledActivity sa1 = setGridId(SA1_GRID, setId(12, createScheduledActivity("A1", 2008, Calendar.MARCH, 4)));
        ScheduledActivity sa2 = setGridId(SA2_GRID, setId(13, createScheduledActivity("B2", 2008, Calendar.MARCH, 10)));

        Subject subject = createSubject("Perry", "Duglas");
        Study study = createBasicTemplate("Study");
        study.setAssignedIdentifier("assignedeIdForStudy");
        Study study2 = createBasicTemplate("Study2");
        study2.setAssignedIdentifier("assignedIdForStudy2");
        Site site = createSite("NU", "assignedIdForSite");

        PscUser user = new PscUserBuilder("TestUser").add(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forSites(site).forStudies(study2).toUser();
        setCurrentUser(user);

        StudySubjectAssignment studySubjectAssignment1 = setId(1, createAssignment(study,site,subject));
        StudySubjectAssignment studySubjectAssignment2 = setId(2, createAssignment(study2,site,subject));
        studySubjectAssignment1.setGridId("ssa11111");
        studySubjectAssignment2.setGridId("ssa22222");

        ScheduledStudySegment scheduledStudySegment1 =  createScheduledStudySegment(DateTools.createDate(2008, Calendar.MARCH, 1), 21);
        ScheduledStudySegment scheduledStudySegment2 =  createScheduledStudySegment(DateTools.createDate(2008, Calendar.MARCH, 2), 22);
        studySubjectAssignment1.getScheduledCalendar().addStudySegment(scheduledStudySegment1);
        studySubjectAssignment2.getScheduledCalendar().addStudySegment(scheduledStudySegment2);
        sa1.setScheduledStudySegment(scheduledStudySegment1);
        sa2.setScheduledStudySegment(scheduledStudySegment2);

        JSONObject activityState1 = createJSONFormatForRequest("canceled", "2008-03-02", "Just Canceled");
        JSONObject activityState2 = createJSONFormatForRequest("scheduled", "2008-03-11", "Move by 1 day");
        JSONObject entity = new JSONObject();
        entity.put(SA1_GRID, activityState1);
        entity.put(SA2_GRID, activityState2);
        request.setEntity(new JsonRepresentation(entity));


        expect(scheduledActivityDao.getByGridId(SA1_GRID)).andReturn(sa1);
        expect(scheduledActivityDao.getByGridId(SA2_GRID)).andReturn(sa2);

        scheduledActivityDao.save(sa2);
        createResponseMessage(SA2_GRID, Status.SUCCESS_CREATED.getCode());
        response.setEntity(new JsonRepresentation(responseEntity));

        doPost();
        assertResponseStatus(Status.SUCCESS_MULTI_STATUS);
        JSONObject responseText = new JSONObject(response.getEntity().getText());
        assertEquals("Wrong Status code for secaond activity",
                Status.CLIENT_ERROR_FORBIDDEN.getCode(), ((JSONObject)responseText.get(SA1_GRID)).get("Status"));
        assertEquals("Wrong Status code for secaond activity",
                Status.SUCCESS_CREATED.getCode(), ((JSONObject)responseText.get(SA2_GRID)).get("Status"));

        assertStateContents(SCHEDULED, 2008, Calendar.MARCH, 11, "Move by 1 day", sa2.getCurrentState());
    }

    public void test400ForUnsupportedEntityContentType() throws Exception {
        request.setEntity("id = 1111", MediaType.TEXT_PLAIN);
        doPost();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    public void testPostValidJSON() throws Exception {
        expectGetActivities();
        scheduledActivityDao.save(sa1);
        createResponseMessage(SA1_GRID, Status.SUCCESS_CREATED.getCode());
        scheduledActivityDao.save(sa2);
        createResponseMessage(SA2_GRID, Status.SUCCESS_CREATED.getCode());
        response.setEntity(new JsonRepresentation(responseEntity));
        doPost();

        assertResponseStatus(Status.SUCCESS_MULTI_STATUS);
        JSONObject responseText = new JSONObject(response.getEntity().getText());
        assertEquals("Wrong Status code for first activity", Status.SUCCESS_CREATED.getCode(), ((JSONObject)responseText.get(SA1_GRID)).get("Status"));
        assertEquals("Wrong Status code for secaond activity", Status.SUCCESS_CREATED.getCode(), ((JSONObject)responseText.get(SA2_GRID)).get("Status"));

        assertStateContents(CANCELED, 2008, Calendar.MARCH, 2, "Just Canceled", sa1.getCurrentState());
        assertStateContents(SCHEDULED, 2008, Calendar.MARCH, 11, "Move by 1 day", sa2.getCurrentState());
    }

    public void testPostInvalidStateForOneActivity() throws Exception {
        expectGetActivities();
        activityState1 = createJSONFormatForRequest("canceledup", "2008-03-02", "Just Canceled");
        activityState2 = createJSONFormatForRequest("scheduled", "2008-03-11", "Move by 1 day");
        entity.put(SA1_GRID, activityState1);
        entity.put(SA2_GRID, activityState2);
        request.setEntity(new JsonRepresentation(entity));
        createResponseMessage(SA1_GRID,Status.CLIENT_ERROR_BAD_REQUEST.getCode());
        scheduledActivityDao.save(sa2);
        createResponseMessage(SA2_GRID,Status.SUCCESS_CREATED.getCode());
        response.setEntity(new JsonRepresentation(responseEntity));

        doPost();
        assertResponseStatus(Status.SUCCESS_MULTI_STATUS);
        JSONObject responseText = new JSONObject(response.getEntity().getText());
        assertEquals("Wrong status code for first activity", Status.CLIENT_ERROR_BAD_REQUEST.getCode(), ((JSONObject)responseText.get(SA1_GRID)).get("Status"));
        assertEquals("Wrong status code for second activity", Status.SUCCESS_CREATED.getCode(), ((JSONObject)responseText.get(SA2_GRID)).get("Status"));

        assertStateContents(SCHEDULED, 2008, Calendar.MARCH, 11, "Move by 1 day", sa2.getCurrentState());
    }

    public void testPostInvalidDateForOneActivity() throws Exception {
        expectGetActivities();
        activityState1 = createJSONFormatForRequest("canceled", "2008-03-02", "Just Canceled");
        activityState2 = createJSONFormatForRequest("scheduled", "2008", "Move by 1 day");
        entity.put(SA1_GRID, activityState1);
        entity.put(SA2_GRID, activityState2);
        request.setEntity(new JsonRepresentation(entity));
        createResponseMessage(SA1_GRID, Status.SUCCESS_CREATED.getCode());
        scheduledActivityDao.save(sa1);
        createResponseMessage(SA2_GRID, Status.CLIENT_ERROR_BAD_REQUEST.getCode());
        response.setEntity(new JsonRepresentation(responseEntity));

        doPost();
        assertResponseStatus(Status.SUCCESS_MULTI_STATUS);
        JSONObject responseText = new JSONObject(response.getEntity().getText());
        assertEquals("Wrong Status code for first activity", Status.SUCCESS_CREATED.getCode(), ((JSONObject)responseText.get(SA1_GRID)).get("Status"));
        assertEquals("Wrong Status code for secaond activity", Status.CLIENT_ERROR_BAD_REQUEST.getCode(), ((JSONObject)responseText.get(SA2_GRID)).get("Status"));

        assertStateContents(CANCELED, 2008, Calendar.MARCH, 2, "Just Canceled", sa1.getCurrentState());
    }

    private void assertStateContents(
        ScheduledActivityMode<?> mode, int year, int month, int day, String reason, ScheduledActivityState actual
    ) {
        assertEquals("Wrong mode", mode, actual.getMode());
        assertDayOfDate("Wrong date", year, month, day, actual.getDate());
        assertEquals("Wrong reason", reason, actual.getReason());
    }

    private void expectGetActivities() {
        expect(scheduledActivityDao.getByGridId(SA1_GRID)).andReturn(sa1);
        expect(scheduledActivityDao.getByGridId(SA2_GRID)).andReturn(sa2);
    }

    //Test Helper Methods

    private void createResponseMessage(String id, int statusCode) throws Exception{
        JSONObject object = new JSONObject();
        object.put("Status", statusCode);
        responseEntity.put(id, object);
    }

    private JSONObject createJSONFormatForRequest(String state, String date, String reason) throws Exception {
        JSONObject object = new JSONObject();
        object.put("reason", reason);
        object.put("state", state);
        object.put("date", date);
        return object;
    }
}
