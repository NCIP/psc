package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static org.easymock.EasyMock.expect;
import org.restlet.data.Status;

/**
 * @author Jalpa Patel
 */
public class SchedulePreviewResourceTest extends AuthorizedResourceTestCase<SchedulePreviewResource>{
    private AmendedTemplateHelper helper;
    private SubjectService subjectService;
    private ScheduledCalendar scheduledCalendar;
    private Study study;
    private StudySegment studySegment;

    public void setUp() throws Exception {
        super.setUp();
        super.setUp();

        helper = registerMockFor(AmendedTemplateHelper.class);
        helper.setRequest(request);
        subjectService =  registerMockFor(SubjectService.class);
        scheduledCalendar = new ScheduledCalendar();
        study = createInDevelopmentTemplate("p1");
        PlannedCalendar plannedCalendar = new PlannedCalendar();
        Epoch epoch = new Epoch();
        studySegment = new StudySegment();
        setGridId("segment_grid_id0",studySegment);
        epoch.addStudySegment(studySegment);
        plannedCalendar.addEpoch(epoch);
        study.setPlannedCalendar(plannedCalendar);
      }

    @Override
    protected SchedulePreviewResource createAuthorizedResource() {
      SchedulePreviewResource resource = new SchedulePreviewResource();
        resource.setAmendedTemplateHelper(helper);
        resource.setSubjectService(subjectService);
        resource.setXmlSerializer(xmlSerializer);
        return resource;
    }

    public void testGet404WhenHelperNotfoundException() throws Exception {
        expect(helper.getAmendedTemplate()).andThrow(new AmendedTemplateHelper.NotFound("Not Found"));
        doGet();

        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void testGet400WhenNoPairForSegmentAndDate() throws Exception {
        request.getResourceRef().addQueryParameter("segment[1]","segment_grid_id1");
        expect(helper.getAmendedTemplate()).andReturn(null);
        doGet();

        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    public void testGet400WhenUnparsableDate() throws Exception {
        request.getResourceRef().addQueryParameter("segment[0]","segment_grid_id0");
        request.getResourceRef().addQueryParameter("start_date[0]","2008");
        expect(helper.getAmendedTemplate()).andReturn(study);
        doGet();

        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }
}
