package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import static edu.northwestern.bioinformatics.studycalendar.test.Fixtures.setId;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import static org.easymock.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;

public class SubjectOffStudyControllerTest extends ControllerTestCase{

    private SubjectService subjectService;
    private StudySubjectAssignmentDao assignmentDao;
    private SubjectOffStudyController controller;
    private StudySubjectAssignment assignment;
    private SubjectOffStudyCommand command;

    protected void setUp() throws Exception {
        super.setUp();
        command = registerMockFor(SubjectOffStudyCommand.class, SubjectOffStudyCommand.class.getMethod("takeSubjectOffStudy"));
        subjectService = registerMockFor(SubjectService.class);
        assignmentDao = registerDaoMockFor(StudySubjectAssignmentDao.class);

        controller = new SubjectOffStudyController(){
           @Override protected Object formBackingObject(HttpServletRequest request) throws Exception {
                return command;
            }
        };
        controller.setSubjectService(subjectService);
        controller.setStudySubjectAssignmentDao (assignmentDao);
        controller.setControllerTools(controllerTools);

        assignment = setId(10, new StudySubjectAssignment());
        assignment.setScheduledCalendar(setId(20, new ScheduledCalendar()));
    }

    public void testSubjectAssignedOnSubmit() throws Exception {
        expect(command.takeSubjectOffStudy()).andReturn(assignment);
        replayMocks();

        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("Wrong view", "redirectToSchedule", mv.getViewName());
        assertEquals("Calendar Parameter wrong", "20", mv.getModelMap().get("calendar"));
    }

   public void testBindDate() throws Exception {
        request.addParameter("expectedEndDate", "08/05/2003");
        expect(command.takeSubjectOffStudy()).andReturn(assignment);
        replayMocks();

        controller.handleRequest(request, response);
        verifyMocks();

        assertDayOfDate(2003, Calendar.AUGUST, 5, command.getExpectedEndDate());
    }


    public void testBindStudySubjectAssignment() throws Exception {
        request.setParameter("assignment", "10");
        expect(assignmentDao.getById(10)).andReturn(assignment);
        expect(command.takeSubjectOffStudy()).andReturn(assignment);
        replayMocks();

        controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("Subject assignments are different", assignment.getId(), command.getAssignment().getId());

    }
}
