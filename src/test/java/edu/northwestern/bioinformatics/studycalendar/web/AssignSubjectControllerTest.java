package edu.northwestern.bioinformatics.studycalendar.web;

import static java.util.Arrays.asList;

import edu.northwestern.bioinformatics.studycalendar.dao.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import static org.easymock.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author Rhett Sutphin
 */
public class AssignSubjectControllerTest extends ControllerTestCase {
    private SubjectDao subjectDao;
    private SiteService siteService;
    private StudyDao studyDao;
    private SiteDao siteDao;
    private StudySegmentDao studySegmentDao;

    private UserDao userDao;
    private AssignSubjectController controller;
    private Study study;
    private List<Subject> subjects;
    private User user;
    private StudySite studySite;
    private Site site;
    private List<Site> sites;

    protected void setUp() throws Exception {
        super.setUp();
        subjectDao = registerDaoMockFor(SubjectDao.class);
        studyDao = registerDaoMockFor(StudyDao.class);
        siteService = registerMockFor(SiteService.class);
        studySegmentDao = registerDaoMockFor(StudySegmentDao.class);
        userDao = registerDaoMockFor(UserDao.class);
        siteDao = registerDaoMockFor(SiteDao.class);

        controller = new AssignSubjectController();
        controller.setSubjectDao(subjectDao);
        controller.setSiteService(siteService);
        controller.setStudyDao(studyDao);
        controller.setSiteDao(siteDao);
        controller.setUserDao(userDao);
        controller.setStudySegmentDao(studySegmentDao);
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
        userRole.setRole(Role.SUBJECT_COORDINATOR);
        userRoles.add(userRole);
        user.setUserRoles(userRoles);

        sites = asList(site);
        subjects = new LinkedList<Subject>();

        SecurityContextHolderTestHelper.setSecurityContext(user.getName(), "pass");
    }


    protected void tearDown() throws Exception {
        super.tearDown();
        ApplicationSecurityManager.removeUserSession();
    }

    public void testSubjectAssignedOnSubmit() throws Exception {
        AssignSubjectCommand mockCommand = registerMockFor(AssignSubjectCommand.class);
        AssignSubjectController mockableController = new MockableCommandController(mockCommand);
        mockableController.setUserDao(userDao);
        mockCommand.setSubjectCoordinator(user);
        expect(userDao.getByName(user.getName())).andReturn(user).anyTimes();
        StudySubjectAssignment assignment = setId(14, new StudySubjectAssignment());

        expect(mockCommand.assignSubject()).andReturn(assignment);
        replayMocks();

        ModelAndView mv = mockableController.handleRequest(request, response);
        verifyMocks();

        assertEquals("Wrong view", "redirectToSchedule", mv.getViewName());
        assertEquals("Missing assignment ID", assignment.getId(), mv.getModel().get("assignment"));
    }

    public void testBindStartDate() throws Exception {
        request.setParameter("startDate", "09/20/1996");
        AssignSubjectCommand command = getAndReturnCommand("startDate");
        assertDayOfDate(1996, Calendar.SEPTEMBER, 20, command.getStartDate());
    }

    public void testBindStudySegment() throws Exception {
        request.setParameter("studySegment", "145");
        StudySegment expectedStudySegment = setId(145, createNamedInstance("B", StudySegment.class));
        expect(studySegmentDao.getById(145)).andReturn(expectedStudySegment);
        AssignSubjectCommand command = getAndReturnCommand("studySegment");
        assertEquals(expectedStudySegment, command.getStudySegment());
    }

    public void testBindStudy() throws Exception {
        request.setParameter("study", "15");
        Study expectedStudy = setId(15, createNamedInstance("Study B", Study.class));
        expect(studyDao.getById(15)).andReturn(expectedStudy);
        AssignSubjectCommand command = getAndReturnCommand("study");
        assertEquals(expectedStudy, command.getStudy());
    }

    public void testBindSite() throws Exception {
        request.setParameter("site", "25");
        Site expectedSite = setId(25, createNamedInstance("Northwestern", Site.class));
        expect(siteDao.getById(25)).andReturn(expectedSite);
        AssignSubjectCommand command = getAndReturnCommand("site");
        assertEquals(expectedSite, command.getSite());
    }

    private AssignSubjectCommand getAndReturnCommand(String expectNoErrorsForField) throws Exception {
        request.setMethod("GET");
        expectRefDataCalls();
        expectFormBackingDataCalls();
        replayMocks();
        Map<String, Object> model = controller.handleRequest(request, response).getModel();
        assertNoBindingErrorsFor(expectNoErrorsForField, model);
        AssignSubjectCommand command = (AssignSubjectCommand) model.get("command");
        verifyMocks();
        resetMocks();
        return command;
    }

    private void expectRefDataCalls() {
        expect(subjectDao.getAll()).andReturn(subjects);
        expect(studyDao.getById(study.getId())).andReturn(study);
        expect(siteService.getSitesForSubjectCoordinator(user.getName(), study)).andReturn(sites);
    }

    private void expectFormBackingDataCalls() {
        expect(studyDao.getById(study.getId())).andReturn(study);
        expect(siteService.getSitesForSubjectCoordinator(user.getName(), study)).andReturn(sites);
    }

    public void testRefdataIncludesStudy() throws Exception {
        assertSame(study, getRefdata().get("study"));
    }

    public void testRefdataIncludesStudySite() throws Exception {
        List actualSites = ((ArrayList)getRefdata().get("sites"));
        assertEquals(sites.size(), actualSites.size());
        assertEquals(sites.get(0), actualSites.get(0));
    }

    public void testRefdataIncludesSubjects() throws Exception {
        assertSame(subjects, getRefdata().get("subjects"));
    }

    public void testRefdataIncludesEpoch() throws Exception {
        assertSame(study.getPlannedCalendar().getEpochs().get(0), getRefdata().get("epoch"));
    }

    public void testRefdataIncludesStudySegments() throws Exception {
        assertEquals(study.getPlannedCalendar().getEpochs().get(0).getStudySegments(), getRefdata().get("studySegments"));
    }

    public void testRefdataIncludesNoStudySegmentsWhenFirstEpochHasNoStudySegments() throws Exception {
        study.getPlannedCalendar().setEpochs(new LinkedList<Epoch>());
        study.getPlannedCalendar().addEpoch(Epoch.create("Screening"));
        List<StudySegment> actualStudySegments = (List<StudySegment>) getRefdata().get("studySegments");
        assertEquals(0, actualStudySegments.size());
    }

    private Map<String, Object> getRefdata() throws Exception {
        expectRefDataCalls();
        replayMocks();
        Map<String, Object> actualRefdata = controller.referenceData(request);
        verifyMocks();
        return actualRefdata;
    }

    private class MockableCommandController extends AssignSubjectController {
        private AssignSubjectCommand command;

        public MockableCommandController(AssignSubjectCommand command) {
            this.command = command;
            setStudySegmentDao(studySegmentDao);
        }

        protected Object formBackingObject(HttpServletRequest request) throws Exception {
            return command;
        }

        protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        }
    }
}
