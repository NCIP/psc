/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import org.easymock.EasyMock;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class AssignSubjectControllerTest extends ControllerTestCase {
    private AssignSubjectController controller;
    private AssignSubjectCommand commandForRefdata;

    private SubjectDao subjectDao;
    private StudyDao studyDao;
    private SiteDao siteDao;
    private StudySegmentDao studySegmentDao;
    private PopulationDao populationDao;

    private Study study;
    private List<Subject> subjects;
    private Site seattle, tacoma, olympia;
    private StudySite seattleSS, tacomaSS, olympiaSS;
    private Configuration configuration;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        subjectDao = registerDaoMockFor(SubjectDao.class);
        studyDao = registerDaoMockFor(StudyDao.class);
        studySegmentDao = registerDaoMockFor(StudySegmentDao.class);
        siteDao = registerDaoMockFor(SiteDao.class);
        populationDao = registerDaoMockFor(PopulationDao.class);
        configuration = registerMockFor(Configuration.class);

        controller = new AssignSubjectController();
        controller.setSubjectDao(subjectDao);
        controller.setStudyDao(studyDao);
        controller.setSiteDao(siteDao);
        controller.setStudySegmentDao(studySegmentDao);
        controller.setPopulationDao(populationDao);
        controller.setControllerTools(controllerTools);
        controller.setApplicationSecurityManager(applicationSecurityManager);
        controller.setConfiguration(configuration);

        // Stop controller from calling validation
        controller.setValidateOnBinding(false);

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

        subjects = new LinkedList<Subject>();

        SecurityContextHolderTestHelper.
            setUserAndReturnMembership("jimbo", PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).
            forAllStudies().forSites(seattle, tacoma, olympia);
    }

    public void testSubjectAssignedOnSubmit() throws Exception {
        AssignSubjectCommand mockCommand = registerMockFor(AssignSubjectCommand.class);
        AssignSubjectController mockableController = new MockableCommandController(mockCommand);
        mockableController.setControllerTools(controllerTools);
        mockableController.setStudyDao(studyDao);
        mockableController.setSiteDao(siteDao);
        mockableController.setSubjectDao(subjectDao);
        mockableController.setStudySegmentDao(studySegmentDao);
        mockableController.setPopulationDao(populationDao);
        mockableController.setApplicationSecurityManager(applicationSecurityManager);

        // TODO #1105
        mockCommand.setStudySubjectCalendarManager((PscUser) EasyMock.notNull());
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

    public void testBindStudySegment() throws Exception {
        request.setParameter("studySegment", "145");
        StudySegment expectedStudySegment = setId(145, createNamedInstance("B", StudySegment.class));
        expect(studySegmentDao.getById(145)).andReturn(expectedStudySegment);
        expect(configuration.get(Configuration.ENABLE_ASSIGNING_SUBJECT)).andReturn(Boolean.TRUE).anyTimes();

        AssignSubjectCommand command = getAndReturnCommand("studySegment");
        assertEquals(expectedStudySegment, command.getStudySegment());
    }

    public void testBindStudy() throws Exception {
        request.setParameter("study", "15");
        study = setId(15, createBasicTemplate());
        expect(studyDao.getById(15)).andReturn(study);
        expect(configuration.get(Configuration.ENABLE_ASSIGNING_SUBJECT)).andReturn(Boolean.TRUE).anyTimes();
        AssignSubjectCommand command = getAndReturnCommand("study");
        assertEquals(study, command.getStudy());
    }

    public void testBindSite() throws Exception {
        request.setParameter("site", "25");
        Site expectedSite = setId(25, createNamedInstance("Northwestern", Site.class));
        expect(siteDao.getById(25)).andReturn(expectedSite);
        expect(configuration.get(Configuration.ENABLE_ASSIGNING_SUBJECT)).andReturn(Boolean.TRUE).anyTimes();
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
        expect(configuration.get(Configuration.ENABLE_ASSIGNING_SUBJECT)).andReturn(Boolean.TRUE).anyTimes();
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
            setValidateOnBinding(false);
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
