package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.ArmDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.TestingTemplateService;
import static org.easymock.classextension.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Rhett Sutphin
 */
public class SelectArmControllerTest extends ControllerTestCase {
    private static final int ARM_ID = 90;

    private SelectArmController controller;
    private ArmDao armDao;
    private DeltaService deltaService;

    private Arm arm;
    private Study study;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        study = Fixtures.createBasicTemplate();
        Fixtures.assignIds(study);
        arm = study.getPlannedCalendar().getEpochs().get(1).getArms().get(1);
        arm.setId(ARM_ID);

        controller = new SelectArmController();
        armDao = registerDaoMockFor(ArmDao.class);
        deltaService = registerMockFor(DeltaService.class);

        controller.setArmDao(armDao);
        controller.setControllerTools(controllerTools);
        controller.setDeltaService(deltaService);
        controller.setTemplateService(new TestingTemplateService());

        expect(armDao.getById(ARM_ID)).andReturn(arm).anyTimes();
        request.setParameter("arm", Integer.toString(ARM_ID));
        request.setMethod("GET");
    }
    
    // TODO: test the inclusion of the plan tree hierarchy
    public void testRequest() throws Exception {
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("template/ajax/selectArm", mv.getViewName());

        Object actualArm = mv.getModel().get("arm");
        assertNotNull("arm missing", actualArm);
        assertTrue("arm is not wrapped", actualArm instanceof ArmTemplate);

        assertEquals("Wrong model: " + mv.getModel(), 4, mv.getModel().size());
    }
    
    public void testRequestWhenAmended() throws Exception {
        study.setDevelopmentAmendment(new Amendment("dev"));
        expect(deltaService.revise(arm)).andReturn((Arm) arm.transientClone());

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        Object actualArm = mv.getModel().get("arm");
        assertNotNull("arm missing", actualArm);
        assertTrue("arm is not wrapped", actualArm instanceof ArmTemplate);
        assertNotNull("dev revision missing", mv.getModel().get("developmentRevision"));

        assertEquals("Wrong model: " + mv.getModel(), 5, mv.getModel().size());
    }
}
