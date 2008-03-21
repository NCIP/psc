package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.nwu.bioinformatics.commons.DateUtils;
import static org.easymock.EasyMock.expect;
import org.restlet.data.Status;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Saurabh Agrawal
 */
public class ScheduledActivitiesResourceTest extends ResourceTestCase<ScheduledActivitiesResource> {

    private ScheduledActivityDao scheduledActivityDao;
    private StudySubjectAssignmentDao studySubjectAssignmentDao;
    StudySubjectAssignment studySubjectAssignment;
    private String DAY = "2";
    private String YEAR = "2008";
    private String MONTH = "5";


    @Override
    public void setUp() throws Exception {
        super.setUp();
        scheduledActivityDao = registerDaoMockFor(ScheduledActivityDao.class);

        studySubjectAssignmentDao = registerDaoMockFor(StudySubjectAssignmentDao.class);
        studySubjectAssignment = new StudySubjectAssignment();
        studySubjectAssignment.setGridId("grid_id");
        studySubjectAssignment.setStudyId("study_id");
        final ScheduledCalendar calendar = new ScheduledCalendar();
        studySubjectAssignment.setScheduledCalendar(calendar);

        request.getAttributes().put(UriTemplateParameters.ASSIGNMENT_IDENTIFIER.attributeName(), studySubjectAssignment.getGridId());

        request.getAttributes().put(UriTemplateParameters.YEAR.attributeName(), YEAR);

        request.getAttributes().put(UriTemplateParameters.MONTH.attributeName(), MONTH);

        request.getAttributes().put(UriTemplateParameters.DAY.attributeName(), DAY);

        request.getAttributes().put(UriTemplateParameters.STUDY_IDENTIFIER.attributeName(), "study_id");

    }

    @Override
    protected ScheduledActivitiesResource createResource() {
        ScheduledActivitiesResource resource = new ScheduledActivitiesResource();
        resource.setScheduledActivityDao(scheduledActivityDao);
        resource.setScheduledActivityXmlSerializer(xmlSerializer);
        resource.setStudySubjectAssignmentDao(studySubjectAssignmentDao);
        return resource;
    }

    public void testGetAllowed() throws Exception {
        assertAllowedMethods("GET");
    }


    public void testGetXmlForAllScheduledActivitiesForASelectedDate() throws Exception {

        List<ScheduledActivity> scheduledActivityList = new ArrayList<ScheduledActivity>();
        Date date = DateUtils.createDate(Integer.parseInt(YEAR), Integer.parseInt(MONTH), Integer.parseInt(DAY));


        expect(studySubjectAssignmentDao.getByGridId(studySubjectAssignment.getGridId())).andReturn(studySubjectAssignment);
        expect(scheduledActivityDao.getEventsByDate(studySubjectAssignment.getScheduledCalendar(), date, date)).andReturn(scheduledActivityList);

        expect(xmlSerializer.createDocumentString(scheduledActivityList)).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

    public void testGetXmlForAllScheduledActivitiesWhenYearIsNull() throws Exception {
        request.getAttributes().remove(UriTemplateParameters.YEAR.attributeName());


        expect(studySubjectAssignmentDao.getByGridId(studySubjectAssignment.getGridId())).andReturn(studySubjectAssignment);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    public void testGetXmlForAllScheduledActivitiesWhenStudyDoesNotMatch() throws Exception {
        request.getAttributes().remove(UriTemplateParameters.STUDY_IDENTIFIER.attributeName());


        expect(studySubjectAssignmentDao.getByGridId(studySubjectAssignment.getGridId())).andReturn(studySubjectAssignment);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

}