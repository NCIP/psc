package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.restlet.data.MediaType;
import org.restlet.data.Status;

import java.util.Calendar;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static org.easymock.EasyMock.*;

/**
 * @author Jalpa Patel
 */
public class SchedulePreviewResourceTest extends AuthorizedResourceTestCase<SchedulePreviewResource>{
    private AmendedTemplateHelper helper;
    private SubjectService subjectService;
    private Study study;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        helper = registerMockFor(AmendedTemplateHelper.class);
        helper.setRequest(request);
        expect(helper.getReadAuthorizations()).
            andStubReturn(ResourceAuthorization.createCollection(PscRole.values()));
        subjectService =  registerMockFor(SubjectService.class);
        study = createSingleEpochStudy("DC", "Treatment", "A", "B");
        assignIds(study.getPlannedCalendar().getEpochs().get(0), 0);
        // A's grid ID is now GRID-50 and B's is GRID-100
      }

    @Override
    @SuppressWarnings({"unchecked"})
    protected SchedulePreviewResource createAuthorizedResource() {
      SchedulePreviewResource resource = new SchedulePreviewResource();
        resource.setAmendedTemplateHelper(helper);
        resource.setSubjectService(subjectService);
        resource.setXmlSerializer(xmlSerializer);
        return resource;
    }

    public void testPreviewIsBuiltFromGoodSegmentDatePair() throws Exception {
        request.getResourceRef().addQueryParameter("segment[0]", "GRID-50");
        request.getResourceRef().addQueryParameter("start_date[0]", "2009-03-04");
        requestJson();

        expect(helper.getAmendedTemplate()).andReturn(study);
        subjectService.scheduleStudySegmentPreview(
            (ScheduledCalendar) notNull(), eq(findNodeByName(study, StudySegment.class, "A")),
            sameDay(2009, Calendar.MARCH, 4));

        doGet();

        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testPreviewIsBuiltFromRandomlyIndexedSegmentDatePair() throws Exception {
        request.getResourceRef().addQueryParameter("segment[tortoise]", "GRID-100");
        request.getResourceRef().addQueryParameter("start_date[tortoise]", "2009-03-21");
        requestJson();

        expect(helper.getAmendedTemplate()).andReturn(study);
        subjectService.scheduleStudySegmentPreview(
            (ScheduledCalendar) notNull(), eq(findNodeByName(study, StudySegment.class, "B")),
            sameDay(2009, Calendar.MARCH, 21));

        doGet();

        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testPreviewMayContainMultipleSegments() throws Exception {
        request.getResourceRef().addQueryParameter("segment[Alpha]", "GRID-100");
        request.getResourceRef().addQueryParameter("segment[Echo]", "GRID-50");
        request.getResourceRef().addQueryParameter("start_date[Alpha]", "2009-03-21");
        request.getResourceRef().addQueryParameter("start_date[Echo]", "2009-04-13");
        requestJson();

        expect(helper.getAmendedTemplate()).andReturn(study);
        subjectService.scheduleStudySegmentPreview(
            (ScheduledCalendar) notNull(), eq(findNodeByName(study, StudySegment.class, "B")),
            sameDay(2009, Calendar.MARCH, 21));
        subjectService.scheduleStudySegmentPreview(
            (ScheduledCalendar) notNull(), eq(findNodeByName(study, StudySegment.class, "A")),
            sameDay(2009, Calendar.APRIL, 13));

        doGet();

        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testGet404WhenHelperNotfoundException() throws Exception {
        expect(helper.getAmendedTemplate()).andThrow(new AmendedTemplateHelper.NotFound("Not Found"));
        doGet();

        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void testGet400WhenNoDateForSegment() throws Exception {
        request.getResourceRef().addQueryParameter("segment[1]", "GRID-50");
        expect(helper.getAmendedTemplate()).andReturn(study);
        doGet();

        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST,
            "The following segment(s) do not have matching start_date(s): [1]");
    }

    public void testGet400WhenNoSegmentForDate() throws Exception {
        request.getResourceRef().addQueryParameter("start_date[1]", "2007-06-13");
        expect(helper.getAmendedTemplate()).andReturn(study);
        doGet();

        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST,
            "The following start_date(s) do not have matching segment(s): [1]");
    }

    public void testGet400WhenInvalidSegmentRequested() throws Exception {
        request.getResourceRef().addQueryParameter("segment[1]", "GRID-non");
        request.getResourceRef().addQueryParameter("start_date[1]", "2009-07-11");
        expect(helper.getAmendedTemplate()).andReturn(study);
        doGet();

        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST,
            "No study segment with identifier GRID-non in the study");
    }

    public void testGet400ForMismatchedSegmentAndDate() throws Exception {
        request.getResourceRef().addQueryParameter("segment[moth]", "GRID-50");
        request.getResourceRef().addQueryParameter("start_date[hummingbird]", "2009-07-11");
        expect(helper.getAmendedTemplate()).andReturn(study);
        doGet();

        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST,
            "The following start_date(s) do not have matching segment(s): [hummingbird]");
    }

    public void testGet400WhenUnparsableDate() throws Exception {
        request.getResourceRef().addQueryParameter("segment[0]", "GRID-50");
        request.getResourceRef().addQueryParameter("start_date[0]", "2008");
        expect(helper.getAmendedTemplate()).andReturn(study);
        doGet();

        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST,
            "Invalid date: start_date[0]=2008.  The date must be formatted as yyyy-mm-dd.");
    }
    
    public void test400ForMissingParams() throws Exception {
        expect(helper.getAmendedTemplate()).andReturn(study);
        doGet();

        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST,
            "At least one segment/start_date pair is required");
    }

    private void requestJson() {
        setAcceptedMediaTypes(MediaType.APPLICATION_JSON);
    }
}
