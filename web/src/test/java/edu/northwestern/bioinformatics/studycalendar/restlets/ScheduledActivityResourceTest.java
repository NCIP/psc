package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Saurabh Agrawal
 * @author Rhett Sutphin
 */
public class ScheduledActivityResourceTest extends ResourceTestCase<ScheduledActivityResource> {
    private ScheduledActivityDao scheduledActivityDao;
    private StudySubjectAssignmentDao studySubjectAssignmentDao;

    private StudySubjectAssignment assignment;
    private ScheduledActivity scheduledActivity;
    private Study study;

    private CurrentScheduledActivityStateXmlSerializer currentScheduledActivityStateXmlSerializer;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        scheduledActivityDao = registerDaoMockFor(ScheduledActivityDao.class);
        scheduledActivity = Fixtures.setId(12, Fixtures.createScheduledActivity("A", 2007, Calendar.MARCH, 4));
        scheduledActivity.setGridId("SA-GRID");

        studySubjectAssignmentDao = registerDaoMockFor(StudySubjectAssignmentDao.class);
        study = Fixtures.createSingleEpochStudy("AG 0701", "QoL");
        assignment = Fixtures.createAssignment(
            study,
            Fixtures.createNamedInstance("AG", Site.class),
            Fixtures.createSubject("Jo", "Jo")
        );
        assignment.setGridId("SSA-GRID");
        assignment.getScheduledCalendar().addStudySegment(
            Fixtures.createScheduledStudySegment(study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0)));
        assignment.getScheduledCalendar().getScheduledStudySegments().get(0)
            .addEvent(scheduledActivity);

        UriTemplateParameters.ASSIGNMENT_IDENTIFIER.putIn(request, assignment.getGridId());
        UriTemplateParameters.SCHEDULED_ACTIVITY_IDENTIFIER.putIn(request, scheduledActivity.getGridId());
        UriTemplateParameters.STUDY_IDENTIFIER.putIn(request, study.getAssignedIdentifier());

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

    ////// GET

    public void testGetXmlForNonExistingScheduledActivity() throws Exception {
        UriTemplateParameters.SCHEDULED_ACTIVITY_IDENTIFIER.putIn(request, "Unknown-One");

        expect(scheduledActivityDao.getByGridId("Unknown-One")).andReturn(null);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void testGetXmlForUnmatchedStudy() throws Exception {
        expectGetScheduledActivity();
        study.setAssignedIdentifier("AG 1701");

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void testGetForUnmatchedScheduledActivity() throws Exception {
        assignment.setGridId("A-different-one");
        expectGetScheduledActivity();

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void testGetXmlForExistingScheduledActivity() throws Exception {
        expectGetScheduledActivity();
        expectObjectXmlized(scheduledActivity);

        doGet();

        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

    ////// POST

    public void testPostValidXml() throws Exception {
        StringBuilder xml = new StringBuilder();

        ScheduledActivityState scheduledActivityState = new Canceled("cancel", new Date());

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append(MessageFormat.format("<scheduled-activity-state  state=\"{0}\" date=\"2008-01-15\" reason=\"{1}\" >",
                AbstractScheduledActivityStateXmlSerializer.CANCELED, scheduledActivityState.getReason()));
        xml.append("</scheduled-activity-state>");

        final InputStream in = new ByteArrayInputStream(xml.toString().getBytes());
        List<ScheduledActivity> scheduledActivityList = new ArrayList<ScheduledActivity>();
        scheduledActivityList.add(scheduledActivity);

        request.setEntity(new InputRepresentation(in, MediaType.TEXT_XML));
        expectGetScheduledActivity();
        scheduledActivityDao.save(scheduledActivity);
        doPost();

        assertResponseStatus(Status.SUCCESS_CREATED);
    }

    public void testPostInvalidXml() throws Exception {
        StringBuffer expected = new StringBuffer();

        ScheduledActivityState scheduledActivityState = new Canceled("cancel", new Date());

        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        expected.append(MessageFormat.format("<scheduled-activity-state  state=\"invalud-state\" date=\"2008-01-15\" reason=\"{1}\" >",
                scheduledActivityState.getReason()));
        expected.append("</scheduled-activity-state>");

        final InputStream in = new ByteArrayInputStream(expected.toString().getBytes());

        request.setEntity(new InputRepresentation(in, MediaType.TEXT_XML));
        expectGetScheduledActivity();

        doPost();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    private void expectGetScheduledActivity() {
        expect(scheduledActivityDao.getByGridId(scheduledActivity.getGridId())).andReturn(scheduledActivity);
    }
}