package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ArmDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import static org.easymock.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;

/**
 * @author Rhett Sutphin
 */
public class AssignParticipantControllerTest extends ControllerTestCase {
    private ParticipantDao participantDao;
    private StudySiteDao studySiteDao;
    private StudyDao studyDao;
    private ArmDao armDao;

    private AssignParticipantController controller;
    private Study study;
    private List<Participant> participants;

    protected void setUp() throws Exception {
        super.setUp();
        participantDao = registerDaoMockFor(ParticipantDao.class);
        studyDao = registerDaoMockFor(StudyDao.class);
        studySiteDao = registerDaoMockFor(StudySiteDao.class);
        armDao = registerDaoMockFor(ArmDao.class);

        controller = new AssignParticipantController();
        controller.setParticipantDao(participantDao);
        controller.setStudyDao(studyDao);
        controller.setStudySiteDao(studySiteDao);
        controller.setArmDao(armDao);

        study = setId(40, createNamedInstance("Protocol 1138", Study.class));
        createStudySite(study, createNamedInstance("Seattle", Site.class));
        PlannedCalendar calendar = new PlannedCalendar();
        calendar.addEpoch(Epoch.create("Treatment", "A", "B", "C"));
        study.setPlannedCalendar(calendar);
        request.addParameter("id", study.getId().toString());

        participants = new LinkedList<Participant>();
    }

    public void testParticipantAssignedOnSubmit() throws Exception {
        AssignParticipantCommand mockCommand = registerMockFor(AssignParticipantCommand.class);
        AssignParticipantController mockableController = new MockableCommandController(mockCommand);
        StudyParticipantAssignment assignment = setId(14, new StudyParticipantAssignment());

        expect(mockCommand.assignParticipant()).andReturn(assignment);
        replayMocks();

        ModelAndView mv = mockableController.handleRequest(request, response);
        verifyMocks();

        assertEquals("Wrong view", "redirectToSchedule", mv.getViewName());
        assertEquals("Missing assignment ID", assignment.getId(), mv.getModel().get("assignment"));
    }

    public void testBindStartDate() throws Exception {
        request.setParameter("startDate", "09/20/1996");
        AssignParticipantCommand command = getAndReturnCommand("startDate");
        assertDayOfDate(1996, Calendar.SEPTEMBER, 20, command.getStartDate());
    }

    public void testBindArm() throws Exception {
        request.setParameter("arm", "145");
        Arm expectedArm = setId(145, createNamedInstance("B", Arm.class));
        expect(armDao.getById(145)).andReturn(expectedArm);
        AssignParticipantCommand command = getAndReturnCommand("arm");
        assertEquals(expectedArm, command.getArm());
    }

    private AssignParticipantCommand getAndReturnCommand(String expectNoErrorsForField) throws Exception {
        request.setMethod("GET");
        expectRefDataCalls();
        replayMocks();
        Map<String, Object> model = controller.handleRequest(request, response).getModel();
        assertNoBindingErrorsFor(expectNoErrorsForField, model);
        AssignParticipantCommand command = (AssignParticipantCommand) model.get("command");
        verifyMocks();
        resetMocks();
        return command;
    }

    private void expectRefDataCalls() {
        expect(participantDao.getAll()).andReturn(participants);
        expect(studyDao.getById(study.getId())).andReturn(study);
    }

    public void testRefdataIncludesStudy() throws Exception {
        assertSame(study, getRefdata().get("study"));
    }

    public void testRefdataIncludesStudySite() throws Exception {
        assertSame(study.getStudySites().get(0), getRefdata().get("studySite"));
    }

    public void testRefdataIncludesParticipants() throws Exception {
        assertSame(participants, getRefdata().get("participants"));
    }

    public void testRefdataIncludesEpoch() throws Exception {
        assertSame(study.getPlannedCalendar().getEpochs().get(0), getRefdata().get("epoch"));
    }

    public void testRefdataIncludesArms() throws Exception {
        assertEquals(study.getPlannedCalendar().getEpochs().get(0).getArms(), getRefdata().get("arms"));
    }

    public void testRefdataIncludesNoArmsWhenFirstEpochHasNoArms() throws Exception {
        study.getPlannedCalendar().setEpochs(new LinkedList<Epoch>());
        study.getPlannedCalendar().addEpoch(Epoch.create("Screening"));
        List<Arm> actualArms = (List<Arm>) getRefdata().get("arms");
        assertEquals(0, actualArms.size());
    }

    private Map<String, Object> getRefdata() throws Exception {
        expectRefDataCalls();
        replayMocks();
        Map<String, Object> actualRefdata = controller.referenceData(request);
        verifyMocks();
        return actualRefdata;
    }

    private class MockableCommandController extends AssignParticipantController {
        private AssignParticipantCommand command;

        public MockableCommandController(AssignParticipantCommand command) {
            this.command = command;
            setArmDao(armDao);
        }

        protected Object formBackingObject(HttpServletRequest request) throws Exception {
            return command;
        }

        protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        }
    }
}
