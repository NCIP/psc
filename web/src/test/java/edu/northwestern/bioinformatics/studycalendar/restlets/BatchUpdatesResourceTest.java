package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createBasicTemplate;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Canceled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import org.restlet.data.Status;
import org.restlet.data.MediaType;
import org.restlet.ext.json.JsonRepresentation;
import static org.easymock.EasyMock.expect;
import org.json.JSONObject;

import java.util.Calendar;
import java.text.SimpleDateFormat;

import gov.nih.nci.cabig.ctms.lang.DateTools;

/**
 * @author Jalpa Patel
 */
public class BatchUpdatesResourceTest extends ResourceTestCase<BatchUpdatesResource>  {
    private ScheduledActivityDao scheduledActivityDao;
    private ScheduleService scheduleService;
    private ScheduledActivity scheduledActivity1, scheduledActivity2;
    private JSONObject entity = new JSONObject();
    private JSONObject responseEntity = new JSONObject();
    private JSONObject activityState1,activityState2;
    private static final String GridId1 = "1111";
    private static final String GridId2 = "2222";
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    public void setUp() throws Exception {
        super.setUp();
        scheduledActivityDao = registerDaoMockFor(ScheduledActivityDao.class);
        scheduleService = registerMockFor(ScheduleService.class);
        scheduledActivity1 = Fixtures.setId(12, Fixtures.createScheduledActivity("A", 2008, Calendar.MARCH, 4));
        scheduledActivity1.setGridId(GridId1);
        scheduledActivity2 = Fixtures.setId(13, Fixtures.createScheduledActivity("B", 2008, Calendar.MARCH, 10));
        scheduledActivity2.setGridId(GridId2);

        Subject subject = createSubject("Perry", "Duglas");
        Study study = createBasicTemplate("Study");
        Site site = createSite("NU");
        StudySubjectAssignment studySubjectAssignment = createAssignment(study,site,subject);
        studySubjectAssignment.setGridId("ssa1111");
        ScheduledStudySegment scheduledStudySegment =  createScheduledStudySegment(DateTools.createDate(2008, Calendar.MARCH, 1), 20);
        studySubjectAssignment.getScheduledCalendar().addStudySegment(
            scheduledStudySegment);
        scheduledActivity1.setScheduledStudySegment(scheduledStudySegment);
        scheduledActivity2.setScheduledStudySegment(scheduledStudySegment);

        activityState1 = createJSONFormatForRequest("canceled", "2008-03-02", "Just Canceled");
        activityState2 = createJSONFormatForRequest("scheduled", "2008-03-11", "Move by 1 day");
        entity.put(GridId1, activityState1);
        entity.put(GridId2, activityState2);
        request.setEntity(new JsonRepresentation(entity));
    }

    @Override
    protected BatchUpdatesResource createResource() {
        BatchUpdatesResource resource = new BatchUpdatesResource();
        resource.setScheduledActivityDao(scheduledActivityDao);
        resource.setScheduleService(scheduleService);
        return resource;
    }

    public void testAllowedMethods() throws Exception {
        assertAllowedMethods("POST", "GET");
    }

    public void testPostWithInvalidActivityId() throws Exception {
        expect(scheduledActivityDao.getByGridId(GridId1)).andReturn(null);
        expect(scheduledActivityDao.getByGridId(GridId2)).andReturn(scheduledActivity2);
        createResponseMessage(GridId1,Status.CLIENT_ERROR_NOT_FOUND.getCode());
        expectUpdateScheduledActivityState(activityState2, new Scheduled());
        scheduledActivityDao.save(scheduledActivity2);
        createResponseMessage(GridId2,Status.SUCCESS_CREATED.getCode());
        response.setEntity(new JsonRepresentation(responseEntity));
        doPost();
        assertResponseStatus(Status.SUCCESS_MULTI_STATUS);
        JSONObject responseText = new JSONObject(response.getEntity().getText());
        assertEquals("Wrong Status code for activity with Invalid gridid",
                Status.CLIENT_ERROR_NOT_FOUND.getCode(), ((JSONObject)responseText.get(GridId1)).get("Status"));
        assertEquals("Wrong Status code for secaond activity",
                Status.SUCCESS_CREATED.getCode(), ((JSONObject)responseText.get(GridId2)).get("Status"));
    }

    public void test400ForUnsupportedEntityContentType() throws Exception {
        request.setEntity("id = 1111", MediaType.TEXT_PLAIN);
        doPost();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    public void testPostValidJSON() throws Exception {
        expectGetActivities();
        expectUpdateScheduledActivityState(activityState1, new Canceled());
        scheduledActivityDao.save(scheduledActivity1);
        createResponseMessage(GridId1,Status.SUCCESS_CREATED.getCode());
        expectUpdateScheduledActivityState(activityState2, new Scheduled());
        scheduledActivityDao.save(scheduledActivity2);
        createResponseMessage(GridId2,Status.SUCCESS_CREATED.getCode());
        response.setEntity(new JsonRepresentation(responseEntity));
        doPost();

        assertResponseStatus(Status.SUCCESS_MULTI_STATUS);
        JSONObject responseText = new JSONObject(response.getEntity().getText());
        assertEquals("Wrong Status code for first activity", Status.SUCCESS_CREATED.getCode(), ((JSONObject)responseText.get(GridId1)).get("Status"));
        assertEquals("Wrong Status code for secaond activity", Status.SUCCESS_CREATED.getCode(), ((JSONObject)responseText.get(GridId2)).get("Status"));
    }

    public void testPostInvalidStateForOneActivity() throws Exception {
        expectGetActivities();
        activityState1 = createJSONFormatForRequest("canceledup", "2008-03-02", "Just Canceled");
        activityState2 = createJSONFormatForRequest("scheduled", "2008-03-11", "Move by 1 day");
        entity.put(GridId1, activityState1);
        entity.put(GridId2, activityState2);
        request.setEntity(new JsonRepresentation(entity));
        expectUpdateScheduledActivityState(activityState1, null);
        createResponseMessage(GridId1,Status.CLIENT_ERROR_BAD_REQUEST.getCode());
        expectUpdateScheduledActivityState(activityState2, new Scheduled());
        scheduledActivityDao.save(scheduledActivity2);
        createResponseMessage(GridId2,Status.SUCCESS_CREATED.getCode());
        response.setEntity(new JsonRepresentation(responseEntity));

        doPost();
        assertResponseStatus(Status.SUCCESS_MULTI_STATUS);
        JSONObject responseText = new JSONObject(response.getEntity().getText());
        assertEquals("Wrong Status code for first activity", Status.CLIENT_ERROR_BAD_REQUEST.getCode(), ((JSONObject)responseText.get(GridId1)).get("Status"));
        assertEquals("Wrong Status code for secaond activity", Status.SUCCESS_CREATED.getCode(), ((JSONObject)responseText.get(GridId2)).get("Status"));
    }

    public void testPostInvalidDateForOneActivity() throws Exception {
        expectGetActivities();
        activityState1 = createJSONFormatForRequest("canceled", "2008-03-02", "Just Canceled");
        activityState2 = createJSONFormatForRequest("scheduled", "2008", "Move by 1 day");
        entity.put(GridId1, activityState1);
        entity.put(GridId2, activityState2);
        request.setEntity(new JsonRepresentation(entity));
        expectUpdateScheduledActivityState(activityState1, new Canceled());
        createResponseMessage(GridId1,Status.SUCCESS_CREATED.getCode());
        scheduledActivityDao.save(scheduledActivity1);
        createResponseMessage(GridId2,Status.CLIENT_ERROR_BAD_REQUEST.getCode());
        response.setEntity(new JsonRepresentation(responseEntity));

        doPost();
        assertResponseStatus(Status.SUCCESS_MULTI_STATUS);
        JSONObject responseText = new JSONObject(response.getEntity().getText());
        assertEquals("Wrong Status code for first activity", Status.SUCCESS_CREATED.getCode(), ((JSONObject)responseText.get(GridId1)).get("Status"));
        assertEquals("Wrong Status code for secaond activity", Status.CLIENT_ERROR_BAD_REQUEST.getCode(), ((JSONObject)responseText.get(GridId2)).get("Status"));

    }


    private void expectUpdateScheduledActivityState(JSONObject object, ScheduledActivityState state) throws Exception {
        expect(scheduleService.createScheduledActivityState(object.get("state").toString(), formatter.parse(object.get("date").toString()) ,object.get("reason").toString()))
                .andReturn(state);
    }

    private void expectGetActivities() {
        expect(scheduledActivityDao.getByGridId(GridId1)).andReturn(scheduledActivity1);
        expect(scheduledActivityDao.getByGridId(GridId2)).andReturn(scheduledActivity2);
    }

    //Test Helper Methods

    private void createResponseMessage(String Id, int statusCode) throws Exception{
        JSONObject object =  new JSONObject();
        object.put("Status",statusCode);
        responseEntity.put(Id,object);
    }

    private JSONObject createJSONFormatForRequest(String state, String date, String reason) throws Exception {
        JSONObject object = new JSONObject();
        object.put("reason", reason);
        object.put("state", state);
        object.put("date", date);
        return object;
    }
}
