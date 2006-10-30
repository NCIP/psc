package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.Arrays;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class DisplayTemplateControllerTest extends ControllerTestCase {
    private DisplayTemplateController controller;

    private StudyDao studyDao;
    private Study study;
    private Arm e1, e2a, e2b;

    protected void setUp() throws Exception {
        super.setUp();
        studyDao = registerDaoMockFor(StudyDao.class);

        study = setId(100, createNamedInstance("Proto", Study.class));
        study.setPlannedCalendar(new PlannedCalendar());
        Epoch epoch1 = setId(1, Epoch.create("E1"));
        e1 = setId(1, epoch1.getArms().get(0));
        Epoch epoch2 = setId(1, Epoch.create("E2", "A", "B"));
        e2a = setId(10, epoch2.getArms().get(0));
        e2b = setId(11, epoch2.getArms().get(1));
        study.getPlannedCalendar().addEpoch(epoch1);
        study.getPlannedCalendar().addEpoch(epoch2);

        controller = new DisplayTemplateController();
        controller.setStudyDao(studyDao);

        request.setMethod("GET");
        request.addParameter("study", study.getId().toString());
        expect(studyDao.getById(study.getId())).andReturn(study);
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

    public void testAssignmentsIncludedIfComplete() throws Exception {
        List<StudyParticipantAssignment> expectedAssignments = Arrays.asList(new StudyParticipantAssignment(), new StudyParticipantAssignment(), new StudyParticipantAssignment());
        study.getPlannedCalendar().setComplete(true);
        expect(studyDao.getAssignmentsForStudy(study.getId())).andReturn(expectedAssignments);

        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(expectedAssignments, actualModel.get("assignments"));
    }

    private Map<String, Object> getAndReturnModel() throws Exception {
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
        Map<String, Object> model = mv.getModel();
        assertSame("study not in model", study, model.get("study"));
        return model;
    }
}
