package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.delta.RevisionChanges;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
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
    private DisplayTemplateController controller;

    private StudyDao studyDao;
    private DeltaService deltaService;
    private Study study;
    private Arm e1, e2a, e2b;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studyDao = registerDaoMockFor(StudyDao.class);
        deltaService = registerMockFor(DeltaService.class);

        study = setId(100, Fixtures.createBasicTemplate());
        e1 =  study.getPlannedCalendar().getEpochs().get(0).getArms().get(0);
        e2a = study.getPlannedCalendar().getEpochs().get(1).getArms().get(0);
        e2b = study.getPlannedCalendar().getEpochs().get(1).getArms().get(1);
        int id = 50;
        for (Epoch epoch : study.getPlannedCalendar().getEpochs()) {
            epoch.setId(id++);
            for (Arm arm : epoch.getArms()) { arm.setId(id++); }
        }

        controller = new DisplayTemplateController();
        controller.setStudyDao(studyDao);
        controller.setDeltaService(deltaService);
        controller.setControllerTools(controllerTools);

        request.setMethod("GET");
        request.addParameter("study", study.getId().toString());

        expect(studyDao.getById(study.getId())).andReturn(study);
    }

    public void testView() throws Exception {
        study.setAmendment(null);
        replayMocks();
        assertEquals("template/display", controller.handleRequest(request, response).getViewName());
        verifyMocks();
    }
    
    public void testNonArmModel() throws Exception {
        study.setAmendment(null);
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(study, actualModel.get("study"));
        assertSame(study.getPlannedCalendar(), actualModel.get("plannedCalendar"));
        assertSame(e1.getEpoch(), actualModel.get("epoch"));
        assertFalse(actualModel.containsKey("calendar"));
    }

    public void testArmIsFirstArmByDefault() throws Exception {
        study.setAmendment(null);
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(e1, ((ArmTemplate) actualModel.get("arm")).getBase());
    }

    public void testArmRespectsSelectedArmParameter() throws Exception {
        study.setAmendment(null);
        request.addParameter("arm", e2b.getId().toString());
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(e2b, ((ArmTemplate) actualModel.get("arm")).getBase());
    }

    public void testArmIgnoresSelectedArmParameterIfNotInStudy() throws Exception {
        study.setAmendment(null);
        request.addParameter("arm", "234");
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(e1, ((ArmTemplate) actualModel.get("arm")).getBase());
    }

    public void testAssignmentsIncludedWhenComplete() throws Exception {
        List<StudyParticipantAssignment> expectedAssignments = Arrays.asList(new StudyParticipantAssignment(), new StudyParticipantAssignment(), new StudyParticipantAssignment());
        expect(studyDao.getAssignmentsForStudy(study.getId())).andReturn(expectedAssignments);

        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(expectedAssignments, actualModel.get("assignments"));
    }

    public void testUseDevelopmentAmendmentIfPresent() throws Exception {
        study.setAmendment(null);
        Amendment dev = new Amendment("New amendment");
        study.setDevelopmentAmendment(dev);
        Study amended = study.transientClone();
        amended.setName("Changed");

        expect(deltaService.revise(study, dev)).andReturn(amended);
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(amended, actualModel.get("study"));
        assertSame(dev, actualModel.get("developmentRevision"));
        assertNull("Changes should not be included for initial dev", actualModel.get("revisionChanges"));
    }

    public void testChangesIncludedIfInAmendmentDevelopmentPresent() throws Exception {
        Amendment dev = new Amendment("New amendment");
        study.setDevelopmentAmendment(dev);
        Study amended = study.transientClone();
        amended.setName("Changed");

        expect(studyDao.getAssignmentsForStudy(study.getId())).andReturn(Collections.<StudyParticipantAssignment>emptyList());
        expect(deltaService.revise(study, dev)).andReturn(amended);
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(amended, actualModel.get("study"));
        assertSame(dev, actualModel.get("developmentRevision"));
        assertNotNull(actualModel.get("revisionChanges"));
        assertTrue(actualModel.get("revisionChanges") instanceof RevisionChanges);
    }

    private Map<String, Object> getAndReturnModel() throws Exception {
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
        Map<String, Object> model = mv.getModel();
        return model;
    }
}
