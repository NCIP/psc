package edu.northwestern.bioinformatics.studycalendar.web;

import static java.util.Arrays.asList;

import edu.northwestern.bioinformatics.studycalendar.dao.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import static org.easymock.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.validation.Errors;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import gov.nih.nci.cabig.ctms.lang.DateTools;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;

/**
 * @author Rhett Sutphin
 */
public class AssignSubjectControllerTest extends ControllerTestCase {
    private AssignSubjectController controller;
    private AssignSubjectCommand commandForRefdata;

    private SubjectDao subjectDao;
    private SiteService siteService;
    private StudyDao studyDao;
    private SiteDao siteDao;
    private StudySegmentDao studySegmentDao;
    private UserDao userDao;
    private PopulationDao populationDao;

    private Study study;
    private List<Subject> subjects;
    private User user;
    private Site seattle, tacoma, olympia;
    private StudySite seattleSS, tacomaSS, olympiaSS;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        subjectDao = registerDaoMockFor(SubjectDao.class);
        studyDao = registerDaoMockFor(StudyDao.class);
        siteService = registerMockFor(SiteService.class);
        studySegmentDao = registerDaoMockFor(StudySegmentDao.class);
        userDao = registerDaoMockFor(UserDao.class);
        siteDao = registerDaoMockFor(SiteDao.class);
        populationDao = registerDaoMockFor(PopulationDao.class);

        controller = new AssignSubjectController();
        controller.setSubjectDao(subjectDao);
        controller.setSiteService(siteService);
        controller.setStudyDao(studyDao);
        controller.setSiteDao(siteDao);
        controller.setUserDao(userDao);
        controller.setStudySegmentDao(studySegmentDao);
        controller.setPopulationDao(populationDao);
        controller.setControllerTools(controllerTools);

        commandForRefdata = new AssignSubjectCommand();

        study = setId(40, createNamedInstance("Protocol 1138", Study.class));
        PlannedCalendar calendar = new PlannedCalendar();
        calendar.addEpoch(Epoch.create("Treatment", "A", "B", "C"));
        study.setPlannedCalendar(calendar);
        request.addParameter("study", study.getId().toString());
        expect(studyDao.getById(40)).andReturn(study).anyTimes();
        commandForRefdata.setStudy(study);

        seattle = createNamedInstance("Seattle", Site.class);
        tacoma = createNamedInstance("Tacoma", Site.class);
        olympia = createNamedInstance("Olympia", Site.class);
        seattleSS = createStudySite(study, seattle);
        tacomaSS = createStudySite(study, tacoma);
        olympiaSS = createStudySite(study, olympia);

        user = Fixtures.createUser("jimbo", Role.SUBJECT_COORDINATOR);
        user.getUserRole(Role.SUBJECT_COORDINATOR).setSites(new HashSet<Site>(asList(seattle, tacoma, olympia)));
        user.getUserRole(Role.SUBJECT_COORDINATOR).setStudySites(asList(seattleSS, tacomaSS, olympiaSS));

        subjects = new LinkedList<Subject>();

        SecurityContextHolderTestHelper.setSecurityContext(user.getName(), "pass");
    }

    public void testSubjectAssignedOnSubmit() throws Exception {
        AssignSubjectCommand mockCommand = registerMockFor(AssignSubjectCommand.class);
        AssignSubjectController mockableController = new MockableCommandController(mockCommand);
        mockableController.setUserDao(userDao);
        mockableController.setControllerTools(controllerTools);
        mockableController.setStudyDao(studyDao);
        mockableController.setSiteDao(siteDao);
        mockableController.setSubjectDao(subjectDao);
        mockableController.setStudySegmentDao(studySegmentDao);
        mockableController.setPopulationDao(populationDao);
        expect(userDao.getByName(user.getName())).andReturn(user).anyTimes();

        mockCommand.setSubjectCoordinator(user);
        mockCommand.setStudy(study);
        StudySubjectAssignment assignment = setId(14, new StudySubjectAssignment());
        expect(mockCommand.assignSubject()).andReturn(assignment);
        replayMocks();

        ModelAndView mv = mockableController.handleRequest(request, response);

        Errors errors = (Errors) mv.getModel().get("org.springframework.validation.BindingResult.command");
        if (errors != null) {
            assertFalse("Should be no errors: " + errors, errors.hasErrors());
        }
        assertEquals("Wrong view", "redirectToSchedule", mv.getViewName());
        assertEquals("Missing assignment ID", assignment.getId(), mv.getModel().get("assignment"));
        verifyMocks();
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
        study = setId(15, createBasicTemplate());
        expect(studyDao.getById(15)).andReturn(study);
        AssignSubjectCommand command = getAndReturnCommand("study");
        assertEquals(study, command.getStudy());
    }

    public void testBindSite() throws Exception {
        request.setParameter("site", "25");
        Site expectedSite = setId(25, createNamedInstance("Northwestern", Site.class));
        expect(siteDao.getById(25)).andReturn(expectedSite);
        AssignSubjectCommand command = getAndReturnCommand("site");
        assertEquals(expectedSite, command.getSite());
    }

    public void testBindPopulation() throws Exception {
        request.addParameter("populations", "25");
        request.addParameter("populations", "28");
        Population p25 = setId(25, new Population());
        Population p28 = setId(28, new Population());
        expect(populationDao.getById(25)).andReturn(p25);
        expect(populationDao.getById(28)).andReturn(p28);
        AssignSubjectCommand command = getAndReturnCommand("populations");
        Set<Population> actual = command.getPopulations();
        assertEquals("Wrong number of populations", 2, actual.size());
        assertContains("Missing expected pop", actual, p25);
        assertContains("Missing expected pop", actual, p28);
    }

    @SuppressWarnings({ "unchecked" })
    private AssignSubjectCommand getAndReturnCommand(String expectNoErrorsForField) throws Exception {
        request.setMethod("GET");
        expectRefDataCalls();
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
        expect(userDao.getByName(user.getName())).andReturn(user).atLeastOnce();
    }

    public void testRefdataIncludesStudy() throws Exception {
        assertSame(study, getRefdata().get("study"));
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

    @SuppressWarnings({ "unchecked" })
    public void testSitesMapInRefdata() throws Exception {
        Amendment current = Fixtures.createAmendments(
            DateTools.createDate(2001, Calendar.JANUARY, 1),
            DateTools.createDate(2004, Calendar.APRIL, 4),
            DateTools.createDate(2007, Calendar.JULY, 7)
        );
        Amendment prev = current.getPreviousAmendment();
        study.setAmendment(current);
        seattleSS.approveAmendment(prev, DateTools.createDate(2004, Calendar.MAY, 5));
        seattleSS.approveAmendment(current, DateTools.createDate(2007, Calendar.AUGUST, 5));
        tacomaSS.approveAmendment(prev, DateTools.createDate(2004, Calendar.JUNE, 1));
        olympiaSS.getAmendmentApprovals().clear();

        Map<String, Object> refdata = getRefdata();
        Map<Site, String> actualSites = (Map<Site, String>) refdata.get("sites");
        assertNotNull("Missing sites refdata", actualSites);
        assertEquals("Wrong number of assignable sites", 2, actualSites.size());
        assertContainsKey("Seattle not in sites map", actualSites, seattle);
        assertEquals("Wrong label for seattle", "Seattle - amendment 07/07/2007 (current)",
            actualSites.get(seattle));
        assertContainsKey("Tacoma not in sites map", actualSites, tacoma);
        assertEquals("Wrong label for tacoma", "Tacoma - amendment 04/04/2004",
            actualSites.get(tacoma));

        assertContainsKey(refdata, "unapprovedSites");
        Collection<Site> unapproved = (Collection<Site>) refdata.get("unapprovedSites");
        assertEquals("Wrong number of unapproved sites", 1, unapproved.size());
        assertSame(olympia, unapproved.iterator().next());
    }

    @SuppressWarnings({ "unchecked" })
    public void testRefdataIncludesNoStudySegmentsWhenFirstEpochHasNoStudySegments() throws Exception {
        study.getPlannedCalendar().setEpochs(new LinkedList<Epoch>());
        study.getPlannedCalendar().addEpoch(Epoch.create("Screening"));
        List<StudySegment> actualStudySegments = (List<StudySegment>) getRefdata().get("studySegments");
        assertEquals(0, actualStudySegments.size());
    }

    private Map<String, Object> getRefdata() throws Exception {
        expectRefDataCalls();
        replayMocks();
        Map<String, Object> actualRefdata = controller.referenceData(request, commandForRefdata, null);
        verifyMocks();
        return actualRefdata;
    }

    private class MockableCommandController extends AssignSubjectController {
        private AssignSubjectCommand command;

        public MockableCommandController(AssignSubjectCommand command) {
            this.command = command;
            setStudySegmentDao(studySegmentDao);
        }

        @Override
        protected Object formBackingObject(HttpServletRequest request) throws Exception {
            return command;
        }

        @Override
        protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest, Object oCommand, Errors errors) throws Exception {
            return null;
        }
    }
}
