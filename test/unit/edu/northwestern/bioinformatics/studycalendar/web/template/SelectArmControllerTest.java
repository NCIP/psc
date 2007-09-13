package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.ArmDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import static org.easymock.classextension.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Rhett Sutphin
 */
public class SelectArmControllerTest extends ControllerTestCase {
    private SelectArmController controller;
    private ArmDao armDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        controller = new SelectArmController();
        armDao = registerDaoMockFor(ArmDao.class);

        controller.setDao(armDao);
        controller.setControllerTools(controllerTools);
    }
    
    // TODO: test the inclusion of the plan tree hierarchy
    public void testRequest() throws Exception {
        request.setMethod("GET");
        request.setParameter("arm", "90");

        expect(armDao.getById(90)).andReturn(new Arm());

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("template/ajax/selectArm", mv.getViewName());

        assertEquals("Wrong model: " + mv.getModel(), 2, mv.getModel().size());
        Object actualArm = mv.getModel().get("arm");
        assertNotNull("arm missing", actualArm);
        assertTrue("arm is not wrapped", actualArm instanceof ArmTemplate);
    }
}
