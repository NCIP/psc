package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.nwu.bioinformatics.commons.DateUtils;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledStudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import static org.easymock.classextension.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.Calendar;
import java.util.Date;
import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings("unchecked")
public class DisplayScheduleControllerTest extends ControllerTestCase {
    private static final int STUDY_ID = 32;

    private DisplayScheduleController controller;
    private StudySubjectAssignmentDao studySubjectAssignmentDao;
    private ScheduledCalendarDao scheduledCalendarDao;
    private ScheduledStudySegmentDao scheduledStudySegmentDao;
    private StudyDao studyDao;
    private StudySubjectAssignment assignment;
    private static final int ASSIGNMENT_ID = 17;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studySubjectAssignmentDao = registerDaoMockFor(StudySubjectAssignmentDao.class);
        scheduledCalendarDao = registerDaoMockFor(ScheduledCalendarDao.class);
        scheduledStudySegmentDao = registerDaoMockFor(ScheduledStudySegmentDao.class);
        studyDao = registerDaoMockFor(StudyDao.class);

        controller = new DisplayScheduleController();
        controller.setStudySubjectAssignmentDao (studySubjectAssignmentDao);
        controller.setScheduledCalendarDao(scheduledCalendarDao);
        controller.setScheduledStudySegmentDao(scheduledStudySegmentDao);
        controller.setStudyDao(studyDao);
        controller.setControllerTools(controllerTools);
          
        assignment = setId(ASSIGNMENT_ID, new StudySubjectAssignment());
        ScheduledCalendar expectedCalendar = new ScheduledCalendar();
        PlannedCalendar expectedPlannedCalendar = new PlannedCalendar();
        {
            Study study = setId(STUDY_ID, createNamedInstance("A", Study.class));
            StudySite studySite = createStudySite(study, createNamedInstance("NU", Site.class));
            expectedPlannedCalendar.setStudy(studySite.getStudy());
            expectedCalendar.addStudySegment(new ScheduledStudySegment());
            expectedCalendar.addStudySegment(new ScheduledStudySegment());

            assignment.setSubject(createSubject("Preston", "Sturges"));
            assignment.setScheduledCalendar(expectedCalendar);
            assignment.setStudySite(studySite);
            assignment.setCurrentAmendment(new Amendment());
        }
    }

    public void testBaseResponse() throws Exception {
        expect(studySubjectAssignmentDao.getById(ASSIGNMENT_ID)).andReturn(assignment);
        request.setParameter("assignment", assignment.getId().toString());

        ModelAndView mv = doHandle();

        assertEquals("schedule/display", mv.getViewName());

        Map<String, Object> actualModel = mv.getModel();

        assertSame(assignment.getStudySite().getStudy().getPlannedCalendar(), actualModel.get("plannedCalendar"));
        assertSame(assignment.getScheduledCalendar(), actualModel.get("calendar"));
        assertSame(assignment.getSubject(), actualModel.get("subject"));
        assertSame(assignment.getScheduledCalendar().getCurrentStudySegment(), actualModel.get("studySegment"));
        assertSame(assignment, actualModel.get("assignment"));
    }

    private void expectRefData() {
        expect(studyDao.getAssignmentsForStudy(STUDY_ID)).andReturn(Arrays.asList(assignment));
    }

    public void testAssignmentMayBeGridId() throws Exception {
        String gridId = "LE-BIG-ID";
        expect(studySubjectAssignmentDao.getByGridId(gridId)).andReturn(assignment);
        request.setParameter("assignment", gridId);

        ModelAndView mv = doHandle();

        assertEquals("schedule/display", mv.getViewName());

        Map<String, Object> actualModel = mv.getModel();

        assertSame(assignment.getStudySite().getStudy().getPlannedCalendar(), actualModel.get("plannedCalendar"));
        assertSame(assignment.getScheduledCalendar(), actualModel.get("calendar"));
        assertSame(assignment.getSubject(), actualModel.get("subject"));
        assertSame(assignment.getScheduledCalendar().getCurrentStudySegment(), actualModel.get("studySegment"));
        assertSame(assignment, actualModel.get("assignment"));
    }

    private ModelAndView doHandle() throws Exception {
        expectRefData();

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        return mv;
    }

    public void testCalendarParameterUsedWhenNoAssignment() throws Exception {
        assignment.getScheduledCalendar().setId(54);
        request.addParameter("calendar", "54");
        expect(scheduledCalendarDao.getById(54)).andReturn(assignment.getScheduledCalendar());

        ModelAndView mv = doHandle();

        assertEquals("schedule/display", mv.getViewName());

        Map<String, Object> actualModel = mv.getModel();

        assertSame(assignment.getStudySite().getStudy().getPlannedCalendar(), actualModel.get("plannedCalendar"));
        assertSame(assignment.getScheduledCalendar(), actualModel.get("calendar"));
        assertSame(assignment.getSubject(), actualModel.get("subject"));
        assertSame(assignment.getScheduledCalendar().getCurrentStudySegment(), actualModel.get("studySegment"));
        assertSame(assignment, actualModel.get("assignment"));
    }
    
    public void testDefaultDates() throws Exception {
        expect(studySubjectAssignmentDao.getById(ASSIGNMENT_ID)).andReturn(assignment);
        request.setParameter("assignment", assignment.getId().toString());
        expectRefData();

        ScheduledStudySegment scheduledStudySegment = registerMockFor(ScheduledStudySegment.class);
        expect(scheduledStudySegment.getNextStudySegmentPerProtocolStartDate())
            .andReturn(DateUtils.createDate(2005, Calendar.MARCH, 11));
        scheduledStudySegment.setScheduledCalendar(assignment.getScheduledCalendar());
        expect(scheduledStudySegment.isComplete()).andReturn(false);

        replayMocks();
        assignment.getScheduledCalendar().addStudySegment(scheduledStudySegment);
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        Map<String, Object> actualModel = mv.getModel();
        Map<String, Date> actualDates = (Map<String, Date>) actualModel.get("dates");
        assertNotNull("Dates missing", actualDates);

        assertDayOfDate(2005, Calendar.MARCH, 11, actualDates.get("PER_PROTOCOL"));
        assertDatesClose(new Date(), actualDates.get("IMMEDIATE"), 1000);
    }

    public void testStudySegmentWhenStudySegmentIdSpecified() throws Exception {
        expect(studySubjectAssignmentDao.getById(ASSIGNMENT_ID)).andReturn(assignment);
        request.setParameter("assignment", assignment.getId().toString());

        ScheduledStudySegment studySegment = new ScheduledStudySegment();
        studySegment.setId(14);
        request.addParameter("studySegment", "14");
        expect(scheduledStudySegmentDao.getById(14)).andReturn(studySegment);

        ModelAndView mv = doHandle();

        assertSame(studySegment, mv.getModel().get("studySegment"));
    }
}
