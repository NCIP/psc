package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Canceled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.AbstractScheduledActivityStateXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.CurrentScheduledActivityStateXmlSerializer;
import static org.easymock.EasyMock.expect;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.InputRepresentation;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import static java.text.MessageFormat.format;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Saurabh Agrawal
 */
public class ScheduledActivityResourceTest extends ResourceTestCase<ScheduledActivityResource> {

    private ScheduledActivityDao scheduledActivityDao;
    private StudySubjectAssignmentDao studySubjectAssignmentDao;
    StudySubjectAssignment studySubjectAssignment;
    private ScheduledActivity scheduledActivity;

    private CurrentScheduledActivityStateXmlSerializer currentScheduledActivityStateXmlSerializer;


    @Override
    public void setUp() throws Exception {
        super.setUp();
        scheduledActivityDao = registerDaoMockFor(ScheduledActivityDao.class);
        scheduledActivity = new ScheduledActivity();
        scheduledActivity.setId(1);
        scheduledActivity.setGridId("grid_id");

        studySubjectAssignmentDao = registerDaoMockFor(StudySubjectAssignmentDao.class);
        studySubjectAssignment = new StudySubjectAssignment();
        studySubjectAssignment.setGridId("grid_id");
        studySubjectAssignment.setStudyId("study_id");
        studySubjectAssignment.setScheduledCalendar(new ScheduledCalendar());

        request.getAttributes().put(UriTemplateParameters.ASSIGNMENT_IDENTIFIER.attributeName(), studySubjectAssignment.getGridId());

        request.getAttributes().put(UriTemplateParameters.SCHEDULED_ACTIVITY_IDENTIFIER.attributeName(), scheduledActivity.getId().intValue() + "");
        request.getAttributes().put(UriTemplateParameters.STUDY_IDENTIFIER.attributeName(), "study_id");

        currentScheduledActivityStateXmlSerializer = new CurrentScheduledActivityStateXmlSerializer();

    }

    @Override
    protected ScheduledActivityResource createResource() {
        ScheduledActivityResource resource = new ScheduledActivityResource();
        resource.setScheduledActivityDao(scheduledActivityDao);
        resource.setXmlSerializer(xmlSerializer);
        resource.setStudySubjectAssignmentDao(studySubjectAssignmentDao);
        resource.setCurrentScheduledActivityStateXmlSerializer(currentScheduledActivityStateXmlSerializer);
        return resource;
    }

    public void testGetAndPostAllowed() throws Exception {
        assertAllowedMethods("GET", "POST");
    }

    public void testGetXmlForNonExistingScheduledActivity() throws Exception {
        List<ScheduledActivity> scheduledActivityList = new ArrayList<ScheduledActivity>();


        expect(studySubjectAssignmentDao.getByGridId(studySubjectAssignment.getGridId())).andReturn(studySubjectAssignment);
        expect(scheduledActivityDao.getEventsByDate(studySubjectAssignment.getScheduledCalendar(), null, null)).andReturn(scheduledActivityList);

        doGet();
        assertFalse("no scheduled activity exists for given scheduled calendar", getResource().isAvailable());
        assertEquals("Result  success", 404, response.getStatus().getCode());
    }

    public void testGetXmlForInValidStudy() throws Exception {
        studySubjectAssignment.setStudyId("new_study_id");

        expect(studySubjectAssignmentDao.getByGridId(studySubjectAssignment.getGridId())).andReturn(studySubjectAssignment);

        doGet();
        assertFalse("no scheduled activity exists for given scheduled calendar", getResource().isAvailable());
        assertEquals("Result  success", 404, response.getStatus().getCode());
    }

    public void testGetXmlForIfUrlHasInCorrectScheduledActivityParamter() throws Exception {
        request.getAttributes().put(UriTemplateParameters.SCHEDULED_ACTIVITY_IDENTIFIER.attributeName(), "2");

        List<ScheduledActivity> scheduledActivityList = new ArrayList<ScheduledActivity>();


        expect(studySubjectAssignmentDao.getByGridId(studySubjectAssignment.getGridId())).andReturn(studySubjectAssignment);
        expect(scheduledActivityDao.getEventsByDate(studySubjectAssignment.getScheduledCalendar(), null, null)).andReturn(scheduledActivityList);

        doGet();
        assertFalse("no scheduled activity exists for given scheduled calendar", getResource().isAvailable());
        assertEquals("Result  success", 404, response.getStatus().getCode());
    }

    public void testGetXmlForIfUrlHasNoScheduledActivityParamtere() throws Exception {
        request.getAttributes().remove(UriTemplateParameters.SCHEDULED_ACTIVITY_IDENTIFIER.attributeName());

        expect(studySubjectAssignmentDao.getByGridId(studySubjectAssignment.getGridId())).andReturn(studySubjectAssignment);

        doGet();
        assertFalse("no scheduled activity exists for given scheduled calendar", getResource().isAvailable());
        assertEquals("Result  success", 404, response.getStatus().getCode());
    }

    public void testGetXmlForExistingScheduledActivity() throws Exception {
        List<ScheduledActivity> scheduledActivityList = new ArrayList<ScheduledActivity>();
        scheduledActivityList.add(scheduledActivity);


        expect(studySubjectAssignmentDao.getByGridId(studySubjectAssignment.getGridId())).andReturn(studySubjectAssignment);
        expect(scheduledActivityDao.getEventsByDate(studySubjectAssignment.getScheduledCalendar(), null, null)).andReturn(scheduledActivityList);
        expectObjectXmlized(scheduledActivity);

        doGet();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertResponseIsCreatedXml();
    }



    public void testPostValidXml() throws Exception {

        StringBuffer expected = new StringBuffer();

        ScheduledActivityState scheduledActivityState = new Canceled("cancel");

        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        PlannedActivity plannedActivity = scheduledActivity.getPlannedActivity();
        String plannedActivityGridId = plannedActivity != null ? plannedActivity.getGridId() : null;
        expected.append(format("<scheduled-activity-state  state=\"{0}\" date=\"2008-01-15\" reason=\"{1}\" >",
                AbstractScheduledActivityStateXmlSerializer.CANCELED, scheduledActivityState.getReason()));

        expected.append("</scheduled-activity-state>");

        final InputStream in = new ByteArrayInputStream(expected.toString().getBytes());
        List<ScheduledActivity> scheduledActivityList = new ArrayList<ScheduledActivity>();
        scheduledActivityList.add(scheduledActivity);

        request.setEntity(new InputRepresentation(in, MediaType.TEXT_XML));
        expect(studySubjectAssignmentDao.getByGridId(studySubjectAssignment.getGridId())).andReturn(studySubjectAssignment);
        expect(scheduledActivityDao.getEventsByDate(studySubjectAssignment.getScheduledCalendar(), null, null)).andReturn(scheduledActivityList);
        scheduledActivityDao.save(scheduledActivity);
        doPost();

        assertResponseStatus(Status.REDIRECTION_SEE_OTHER);
        assertEquals(ROOT_URI + "/studies/study_id/schedules/grid_id/activities/null",
                response.getLocationRef().getTargetRef().toString());
    }

    public void testPostInvalidXml() throws Exception {
        StringBuffer expected = new StringBuffer();

        ScheduledActivityState scheduledActivityState = new Canceled("cancel");

        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
//        expected.append("<scheduled-activities");
//        expected.append(format("       {0}=\"{1}\"", SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS));
//        expected.append(format("       {0}:{1}=\"{2} {3}\"", SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_LOCATION_ATTRIBUTE, PSC_NS, AbstractStudyCalendarXmlSerializer.SCHEMA_LOCATION));
//        expected.append(format("       {0}:{1}=\"{2}\">", SCHEMA_NAMESPACE_ATTRIBUTE, XML_SCHEMA_ATTRIBUTE, XSI_NS));

        PlannedActivity plannedActivity = scheduledActivity.getPlannedActivity();
        String plannedActivityGridId = plannedActivity != null ? plannedActivity.getGridId() : null;
        expected.append(format("<scheduled-activity-state  state=\"invalud-state\" date=\"2008-01-15\" reason=\"{1}\" >",
                scheduledActivityState.getReason()));

        expected.append("</scheduled-activity-state>");

        final InputStream in = new ByteArrayInputStream(expected.toString().getBytes());
        List<ScheduledActivity> scheduledActivityList = new ArrayList<ScheduledActivity>();
        scheduledActivityList.add(scheduledActivity);

        request.setEntity(new InputRepresentation(in, MediaType.TEXT_XML));
        expect(studySubjectAssignmentDao.getByGridId(studySubjectAssignment.getGridId())).andReturn(studySubjectAssignment);
        expect(scheduledActivityDao.getEventsByDate(studySubjectAssignment.getScheduledCalendar(), null, null)).andReturn(scheduledActivityList);


        try {
            doPost();

        } catch (StudyCalendarValidationException e) {

            fail("No Holday existis with id:" + 4 + " at the site:");
            assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);

        }

    }

//
//        public void testGetXmlForNonExistentActivityIs404() throws Exception {
//            expectFoundActivity(null);
//
//            doGet();
//
//            assertEquals("Result not 'not found'", 404, response.getStatus().getCode());
//        }

//
//        public void testPutExistingActivity() throws Exception {
//            Activity newActivity = new Activity();
//            expectFoundActivity(activity);
//            expectReadXmlFromRequestAs(newActivity);
//            expectObjectXmlized(newActivity);
//
//            activityDao.save(activity);
//            doPut();
//
//            assertEquals("Result not success", 200, response.getStatus().getCode());
//            assertResponseIsCreatedXml();
//        }
////
//        public void testDeleteExistingActivityWhichIsNotusedAnyWhere() throws Exception {
//            expectFoundActivity(activity);
//            expectActivityUsedByPlannedCalendar(activity, false);
//            activityDao.delete(activity);
//            doDelete();
//
//            assertEquals("Result not success", 200, response.getStatus().getCode());
////        assertResponseIsCreatedXml();
//        }

//        public void testDeleteExistingActivityWhichIsused() throws Exception {
//            expectFoundActivity(activity);
//            expectActivityUsedByPlannedCalendar(activity, true);
//            doDelete();
//
//            assertEquals("Result is success", 400, response.getStatus().getCode());
////        assertResponseIsCreatedXml();
//        }
//
//        public void testPutNewXml() throws Exception {
//            expectFoundActivity(null);
//            expectObjectXmlized(activity);
//            expectReadXmlFromRequestAs(activity);
//
//            activityDao.save(activity);
//            doPut();
//
//            assertResponseStatus(Status.SUCCESS_CREATED);
//            assertResponseIsCreatedXml();
//        }

//        private void expectFoundActivity(Activity expectedActivity) {
//            expect(activityDao.getByCodeAndSourceName(ACTIVITY_NAME, SOURCE_NAME)).andReturn(expectedActivity);
//        }
//
//        private void expectActivityUsedByPlannedCalendar(Activity expectedActivity, boolean isExcepted) {
//            if (isExcepted) {
//                List<PlannedActivity> plannedActivities = new ArrayList<PlannedActivity>();
//                plannedActivities.add(new PlannedActivity());
//                expect(plannedActivityDao.getPlannedActivitiesForAcivity(expectedActivity.getId())).andReturn(plannedActivities);
//            } else {
//                expect(plannedActivityDao.getPlannedActivitiesForAcivity(expectedActivity.getId())).andReturn(null);
//
//            }
//        }


}