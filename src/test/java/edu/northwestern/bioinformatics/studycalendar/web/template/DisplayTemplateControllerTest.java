package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.delta.RevisionChanges;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
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
    private AmendmentService amendmentService;

    private Study study;
    private Arm e1, e2a, e2b;
    private Amendment a0, a1, a2;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studyDao = registerDaoMockFor(StudyDao.class);
        deltaService = registerMockFor(DeltaService.class);
        amendmentService = registerMockFor(AmendmentService.class);

        study = setId(100, Fixtures.createBasicTemplate());
        study.setName(STUDY_NAME);
        e1 =  study.getPlannedCalendar().getEpochs().get(0).getArms().get(0);
        e2a = study.getPlannedCalendar().getEpochs().get(1).getArms().get(0);
        e2b = study.getPlannedCalendar().getEpochs().get(1).getArms().get(1);
        int id = 50;
        for (Epoch epoch : study.getPlannedCalendar().getEpochs()) {
            epoch.setId(id++);
            for (Arm arm : epoch.getArms()) { arm.setId(id++); }
        }

        a2 = setId(2, createAmendments("A0", "A1", "A2"));
        a1 = setId(1, a2.getPreviousAmendment());
        a0 = setId(0, a1.getPreviousAmendment());
        study.setAmendment(a2);

        controller = new DisplayTemplateController();
        controller.setStudyDao(studyDao);
        controller.setDeltaService(deltaService);
        controller.setAmendmentService(amendmentService);
        controller.setControllerTools(controllerTools);

        request.setMethod("GET");
        request.addParameter("study", study.getId().toString());

        expect(studyDao.getById(study.getId())).andReturn(study);
        expect(studyDao.getAssignmentsForStudy(study.getId())).andReturn(Collections.<StudySubjectAssignment>emptyList()).anyTimes();
    }

    public void testView() throws Exception {
        replayMocks();
        assertEquals("template/display", controller.handleRequest(request, response).getViewName());
        verifyMocks();
    }
    
    public void testNonArmModel() throws Exception {
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(study, actualModel.get("study"));
        assertSame(study.getPlannedCalendar(), actualModel.get("plannedCalendar"));
        assertSame(e1.getEpoch(), actualModel.get("epoch"));
        assertFalse(actualModel.containsKey("calendar"));
    }

    public void testArmIsFirstArmByDefault() throws Exception {
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(e1, ((ArmTemplate) actualModel.get("arm")).getBase());
    }

    public void testArmRespectsSelectedArmParameter() throws Exception {
        request.addParameter("arm", e2b.getId().toString());
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(e2b, ((ArmTemplate) actualModel.get("arm")).getBase());
    }

    public void testArmIgnoresSelectedArmParameterIfNotInStudy() throws Exception {
        request.addParameter("arm", "234");
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(e1, ((ArmTemplate) actualModel.get("arm")).getBase());
    }

    public void testAssignmentsIncludedWhenComplete() throws Exception {
        reset(studyDao);
        expect(studyDao.getById(study.getId())).andReturn(study);
        
        List<StudySubjectAssignment> expectedAssignments = Arrays.asList(new StudySubjectAssignment(), new StudySubjectAssignment(), new StudySubjectAssignment());
        expect(studyDao.getAssignmentsForStudy(study.getId())).andReturn(expectedAssignments);

        Map<String, Object> actualModel = getAndReturnModel();
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
        study.setDevelopmentAmendment(new Amendment("dev"));
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
        Study amended = study.transientClone();

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
        Study amended = study.transientClone();

        expect(deltaService.revise(study, dev)).andReturn(amended);
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(amended, actualModel.get("study"));
        assertSame(dev, actualModel.get("amendment"));
        assertSame(dev, actualModel.get("developmentRevision"));
        assertNull("Changes should not be included for initial dev", actualModel.get("revisionChanges"));
    }

    public void testChangesIncludedIfInAmendmentDevelopment() throws Exception {
        Amendment dev = setId(4, new Amendment("dev"));
        request.addParameter("amendment", "4");
        study.setDevelopmentAmendment(dev);
        Study amended = study.transientClone();

        expect(deltaService.revise(study, dev)).andReturn(amended);
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(amended, actualModel.get("study"));
        assertSame(dev, actualModel.get("developmentRevision"));
    }

    @SuppressWarnings({ "unchecked" })
    private Map<String, Object> getAndReturnModel() throws Exception {
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
        return (Map<String, Object>) mv.getModel();
    }
}
