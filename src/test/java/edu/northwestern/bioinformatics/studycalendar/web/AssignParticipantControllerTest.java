package edu.northwestern.bioinformatics.studycalendar.web;

import static java.util.Arrays.asList;

import edu.northwestern.bioinformatics.studycalendar.dao.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.StudySiteService;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import static org.easymock.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.validation.Errors;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author Rhett Sutphin
 */
public class AssignParticipantControllerTest extends ControllerTestCase {
    private ParticipantDao participantDao;
    private SiteService siteService;
    private StudyDao studyDao;
    private SiteDao siteDao;
    private ArmDao armDao;

    private UserDao userDao;
    private AssignParticipantController controller;
    private Study study;
    private List<Participant> participants;
    private User user;
    private StudySite studySite;
    private Site site;
    private List<Site> sites;

    protected void setUp() throws Exception {
        super.setUp();
        participantDao = registerDaoMockFor(ParticipantDao.class);
        studyDao = registerDaoMockFor(StudyDao.class);
        siteService = registerMockFor(SiteService.class);
        armDao = registerDaoMockFor(ArmDao.class);
        userDao = registerDaoMockFor(UserDao.class);
        siteDao = registerDaoMockFor(SiteDao.class);

        controller = new AssignParticipantController();
        controller.setParticipantDao(participantDao);
        controller.setSiteService(siteService);
        controller.setStudyDao(studyDao);
        controller.setSiteDao(siteDao);
        controller.setUserDao(userDao);
        controller.setArmDao(armDao);
        controller.setControllerTools(controllerTools);


        study = setId(40, createNamedInstance("Protocol 1138", Study.class));
        site  = createNamedInstance("Seattle", Site.class);
        studySite = createStudySite(study, site);
        PlannedCalendar calendar = new PlannedCalendar();
        calendar.addEpoch(Epoch.create("Treatment", "A", "B", "C"));
        study.setPlannedCalendar(calendar);
        request.addParameter("id", study.getId().toString());

        user = new User();
        user.setName("user");
        Set<UserRole> userRoles = new HashSet<UserRole>();
        UserRole userRole = new UserRole();
        userRole.setRole(Role.PARTICIPANT_COORDINATOR);
        userRoles.add(userRole);
        user.setUserRoles(userRoles);

        sites = asList(site);
        participants = new LinkedList<Participant>();

        SecurityContextHolderTestHelper.setSecurityContext(user.getName(), "pass");
    }


    protected void tearDown() throws Exception {
        super.tearDown();
        ApplicationSecurityManager.removeUserSession();
    }

    public void testParticipantAssignedOnSubmit() throws Exception {
        AssignParticipantCommand mockCommand = registerMockFor(AssignParticipantCommand.class);
        AssignParticipantController mockableController = new MockableCommandController(mockCommand);
        mockableController.setUserDao(userDao);
        mockCommand.setParticipantCoordinator(user);
        expect(userDao.getByName(user.getName())).andReturn(user).anyTimes();
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

    public void testBindStudy() throws Exception {
        request.setParameter("study", "15");
        Study expectedStudy = setId(15, createNamedInstance("Study B", Study.class));
        expect(studyDao.getById(15)).andReturn(expectedStudy);
        AssignParticipantCommand command = getAndReturnCommand("study");
        assertEquals(expectedStudy, command.getStudy());
    }

    public void testBindSite() throws Exception {
        request.setParameter("site", "25");
        Site expectedSite = setId(25, createNamedInstance("Northwestern", Site.class));
        expect(siteDao.getById(25)).andReturn(expectedSite);
        AssignParticipantCommand command = getAndReturnCommand("site");
        assertEquals(expectedSite, command.getSite());
    }

    private AssignParticipantCommand getAndReturnCommand(String expectNoErrorsForField) throws Exception {
        request.setMethod("GET");
        expectRefDataCalls();
        expectFormBackingDataCalls();
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
        expect(siteService.getSitesForParticipantCoordinator(user.getName(), study)).andReturn(sites);
    }

    private void expectFormBackingDataCalls() {
        expect(studyDao.getById(study.getId())).andReturn(study);
        expect(siteService.getSitesForParticipantCoordinator(user.getName(), study)).andReturn(sites);
    }

    public void testRefdataIncludesStudy() throws Exception {
        assertSame(study, getRefdata().get("study"));
    }

    public void testRefdataIncludesStudySite() throws Exception {
        List actualSites = ((ArrayList)getRefdata().get("sites"));
        assertEquals(sites.size(), actualSites.size());
        assertEquals(sites.get(0), actualSites.get(0));
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
