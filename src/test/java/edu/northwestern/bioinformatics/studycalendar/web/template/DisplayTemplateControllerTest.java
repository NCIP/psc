package edu.northwestern.bioinformatics.studycalendar.web.template;

import static java.util.Collections.singletonList;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Role.SUBJECT_COORDINATOR;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.delta.RevisionChanges;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.SecurityContextHolderTestHelper;
import static org.easymock.classextension.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

/**
 * @author Rhett Sutphin
 */
public class DisplayTemplateControllerTest extends ControllerTestCase {
    private static final String STUDY_NAME = "NU-1066";

    private DisplayTemplateController controller;

    private StudyDao studyDao;
    private DeltaService deltaService;
    private TemplateService templateService;
    private AmendmentService amendmentService;

    private Study study;
    private StudySegment e1, e2a, e2b;
    private Amendment a0, a1, a2;
    private UserDao userDao;
    private User subjectCoord;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        userDao = registerDaoMockFor(UserDao.class);
        studyDao = registerDaoMockFor(StudyDao.class);
        deltaService = registerMockFor(DeltaService.class);
        templateService = registerMockFor(TemplateService.class);
        amendmentService = registerMockFor(AmendmentService.class);

        study = setId(100, Fixtures.createBasicTemplate());
        study.setName(STUDY_NAME);
        e1 =  study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);
        e2a = study.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0);
        e2b = study.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(1);
        int id = 50;
        for (Epoch epoch : study.getPlannedCalendar().getEpochs()) {
            epoch.setId(id++);
            for (StudySegment studySegment : epoch.getStudySegments()) { studySegment.setId(id++); }
        }

        a2 = setId(2, createAmendments("A0", "A1", "A2"));
        a1 = setId(1, a2.getPreviousAmendment());
        a0 = setId(0, a1.getPreviousAmendment());
        study.setAmendment(a2);

        controller = new DisplayTemplateController();
        controller.setUserDao(userDao);
        controller.setStudyDao(studyDao);
        controller.setDeltaService(deltaService);
        controller.setTemplateService(templateService);
        controller.setAmendmentService(amendmentService);
        controller.setControllerTools(controllerTools);

        request.setMethod("GET");
        request.addParameter("study", study.getId().toString());

        subjectCoord = Fixtures.createUser("john", Role.SUBJECT_COORDINATOR);
        SecurityContextHolderTestHelper.setSecurityContext(subjectCoord.getName(), "asdf");

         expect(userDao.getByName(subjectCoord.getName())).andReturn(subjectCoord).anyTimes();
         expect(templateService.filterForVisibility(singletonList(study), subjectCoord.getUserRole(Role.SUBJECT_COORDINATOR)))
                 .andReturn(singletonList(study)).anyTimes();

    }


    public void testGetStudyByAssignedIdentifier() throws Exception {
        request.addParameter("study", study.getName());
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(study, actualModel.get("study"));
    }

    public void testGetStudyByStudyId() throws Exception {
        request.addParameter("study", study.getId().toString());
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(study, actualModel.get("study"));
    }


    public void testView() throws Exception {
        expect(studyDao.getByAssignedIdentifier(study.getId().toString())).andReturn(null);
        expect(studyDao.getById(study.getId())).andReturn(study);
        List<StudySubjectAssignment> expectedAssignments = Arrays.asList(new StudySubjectAssignment(), new StudySubjectAssignment(), new StudySubjectAssignment());
        expect(studyDao.getAssignmentsForStudy(study.getId())).andReturn(expectedAssignments);        
        replayMocks();
        assertEquals("template/display", controller.handleRequest(request, response).getViewName());
        verifyMocks();
    }
    
    public void testNonStudySegmentModel() throws Exception {
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(study, actualModel.get("study"));
        assertSame(study.getPlannedCalendar(), actualModel.get("plannedCalendar"));
        assertSame(e1.getEpoch(), actualModel.get("epoch"));
        assertFalse(actualModel.containsKey("calendar"));
    }

    public void testStudySegmentIsFirstStudySegmentByDefault() throws Exception {
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(e1, ((StudySegmentTemplate) actualModel.get("studySegment")).getBase());
    }

    public void testStudySegmentRespectsSelectedStudySegmentParameter() throws Exception {
        request.addParameter("studySegment", e2b.getId().toString());
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(e2b, ((StudySegmentTemplate) actualModel.get("studySegment")).getBase());
    }

    public void testStudySegmentIgnoresSelectedStudySegmentParameterIfNotInStudy() throws Exception {
        request.addParameter("studySegment", "234");
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(e1, ((StudySegmentTemplate) actualModel.get("studySegment")).getBase());
    }

    public void testAssignmentsIncludedWhenComplete() throws Exception {
        reset(studyDao);
        expect(studyDao.getByAssignedIdentifier(study.getId().toString())).andReturn(null);
        expect(studyDao.getById(study.getId())).andReturn(study);

        List<StudySubjectAssignment> expectedAssignments = Arrays.asList(new StudySubjectAssignment(), new StudySubjectAssignment(), new StudySubjectAssignment());
        expect(studyDao.getAssignmentsForStudy(study.getId())).andReturn(expectedAssignments);
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
        Map<String, Object> actualModel = (Map<String, Object>) mv.getModel();
        assertSame(expectedAssignments, actualModel.get("assignments"));
    }

    public void testDefaultToPublishedAmendment() throws Exception {
        study.setDevelopmentAmendment(new Amendment("dev"));

        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(study, actualModel.get("study"));
        assertSame(study.getAmendment(), actualModel.get("amendment"));
        assertNull("Development revision not selected, so should not be present",
            actualModel.get("developmentRevision"));
    }

    public void testExceptionIfNoPublishedAmendmentAndNoneSelected() throws Exception {
        study.setDevelopmentAmendment(null);
        study.setAmendment(null);

        try {
            getAndReturnModel();
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException e) {
            assertEquals("No default amendment for " + STUDY_NAME, e.getMessage());
        }
    }

    public void testSelectingPreviouslyPublishedAmendment() throws Exception {
        study.setDevelopmentAmendment(new Amendment("dev"));
        request.setParameter("amendment", "1");
        Study amended = study.transientClone();
        expect(templateService.filterForVisibility(singletonList(amended), subjectCoord.getUserRole(Role.SUBJECT_COORDINATOR)))
                 .andReturn(singletonList(study)).anyTimes();
        expect(amendmentService.getAmendedStudy(study, a1)).andReturn(amended);

        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(amended, actualModel.get("study"));
        assertSame(a1, actualModel.get("amendment"));
        assertNull("Development revision not selected, so should not be present",
            actualModel.get("developmentRevision"));
    }

    public void testSelectingDevelopmentAmendmentIfPresent() throws Exception {
        Amendment dev = setId(4, new Amendment("dev"));
        study.setDevelopmentAmendment(dev);
        request.setParameter("amendment", "4");
//        Study amended = study.transientClone();
        Study amended = study;

        expect(templateService.filterForVisibility(singletonList(amended), subjectCoord.getUserRole(Role.SUBJECT_COORDINATOR)))
                .andReturn(singletonList(study)).anyTimes();
        expect(deltaService.revise(study, dev)).andReturn(amended);
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(amended, actualModel.get("study"));
        assertSame(dev, actualModel.get("amendment"));
        assertSame(dev, actualModel.get("developmentRevision"));
        assertNotNull(actualModel.get("revisionChanges"));
        assertTrue(actualModel.get("revisionChanges") instanceof RevisionChanges);
    }
    
    public void testSelectDevelopmentAmendmentForInitialCreation() throws Exception {
        Amendment dev = setId(4, new Amendment("dev"));
        study.setDevelopmentAmendment(dev);
        study.setAmendment(null);
        request.setParameter("amendment", "4");
//        Study amended = study.transientClone();
        Study amended = study;

        expect(deltaService.revise(study, dev)).andReturn(amended);
        expect(studyDao.getByAssignedIdentifier(study.getId().toString())).andReturn(null);
        expect(studyDao.getById(study.getId())).andReturn(study);

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
        Map<String, Object> actualModel = (Map<String, Object>) mv.getModel();

        assertSame(amended, actualModel.get("study"));
        assertSame(dev, actualModel.get("amendment"));
        assertSame(dev, actualModel.get("developmentRevision"));
        assertNull("Changes should not be included for initial dev", actualModel.get("revisionChanges"));
    }

    public void testChangesIncludedIfInAmendmentDevelopment() throws Exception {
        Amendment dev = setId(4, new Amendment("dev"));
        request.addParameter("amendment", "4");
        study.setDevelopmentAmendment(dev);
//        Study amended = study.transientClone();
        Study amended = study;

        expect(templateService.filterForVisibility(singletonList(amended), subjectCoord.getUserRole(Role.SUBJECT_COORDINATOR)))
                .andReturn(singletonList(study)).anyTimes();
        expect(deltaService.revise(study, dev)).andReturn(amended);
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(amended, actualModel.get("study"));
        assertSame(dev, actualModel.get("developmentRevision"));
    }

    public void testStudyAssignableIfSubjectCoordAssigned() throws Exception {

        Map<String, Object> actualModel = getAndReturnModel();

        assertSame("Study should be assignable", Boolean.TRUE, actualModel.get("canAssignSubjects"));
    }

    @SuppressWarnings({ "unchecked" })
    private Map<String, Object> getAndReturnModel() throws Exception {
        expect(studyDao.getByAssignedIdentifier(study.getId().toString())).andReturn(null);
        expect(studyDao.getById(study.getId())).andReturn(study);
        List<StudySubjectAssignment> expectedAssignments = Arrays.asList(new StudySubjectAssignment(), new StudySubjectAssignment(), new StudySubjectAssignment());
        expect(studyDao.getAssignmentsForStudy(study.getId())).andReturn(expectedAssignments);
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
        return (Map<String, Object>) mv.getModel();
    }
}
