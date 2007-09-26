package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyParticipantAssignmentDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.service.ParticipantService;
import static org.easymock.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;

public class ParticipantOffStudyControllerTest extends ControllerTestCase{

    private ParticipantService participantService;
    private StudyParticipantAssignmentDao assignmentDao;
    private ParticipantOffStudyController controller;
    private StudyParticipantAssignment assignment;
    private ParticipantOffStudyCommand command;

    protected void setUp() throws Exception {
        super.setUp();
        command = registerMockFor(ParticipantOffStudyCommand.class, ParticipantOffStudyCommand.class.getMethod("takeParticipantOffStudy"));
        participantService = registerMockFor(ParticipantService.class);
        assignmentDao = registerDaoMockFor(StudyParticipantAssignmentDao.class);

        controller = new ParticipantOffStudyController(){
           @Override protected Object formBackingObject(HttpServletRequest request) throws Exception {
                return command;
            }
        };
        controller.setParticipantService(participantService);
        controller.setStudyParticipantAssignmentDao(assignmentDao);
        controller.setControllerTools(controllerTools);

        assignment = setId(10, new StudyParticipantAssignment());
        assignment.setScheduledCalendar(setId(20, new ScheduledCalendar()));
    }

    public void testParticipantAssignedOnSubmit() throws Exception {
        expect(command.takeParticipantOffStudy()).andReturn(assignment);
        replayMocks();

        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("Wrong view", "redirectToSchedule", mv.getViewName());
        assertEquals("Calendar Parameter wrong", "20", mv.getModelMap().get("calendar"));
    }

   public void testBindDate() throws Exception {
        request.addParameter("expectedEndDate", "08/05/2003");
        expect(command.takeParticipantOffStudy()).andReturn(assignment);
        replayMocks();

        controller.handleRequest(request, response);
        verifyMocks();

        assertDayOfDate(2003, Calendar.AUGUST, 5, command.getExpectedEndDate());
    }


    public void testBindStudyParticipantAssignment() throws Exception {
        request.setParameter("assignment", "10");
        expect(assignmentDao.getById(10)).andReturn(assignment);
        expect(command.takeParticipantOffStudy()).andReturn(assignment);
        replayMocks();

        controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("Participant assignments are different", assignment.getId(), command.getAssignment().getId());

    }
}
