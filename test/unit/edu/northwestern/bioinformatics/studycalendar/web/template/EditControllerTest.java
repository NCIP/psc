package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.ArmDao;
import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import static org.easymock.classextension.EasyMock.expect;
import org.springframework.context.ApplicationContext;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Rhett Sutphin
 */
public class EditControllerTest extends ControllerTestCase {
    private EditController controller;

    private StudyDao studyDao;
    private EpochDao epochDao;
    private ArmDao armDao;

    private EditCommand command;
    private ApplicationContext applicationContext;

    protected void setUp() throws Exception {
        super.setUp();
        studyDao = registerDaoMockFor(StudyDao.class);
        epochDao = registerDaoMockFor(EpochDao.class);
        armDao = registerDaoMockFor(ArmDao.class);
        applicationContext = registerMockFor(ApplicationContext.class);
        command = registerMockFor(EditCommand.class);

        controller = new EditController();
        controller.setArmDao(armDao);
        controller.setEpochDao(epochDao);
        controller.setStudyDao(studyDao);
        controller.setApplicationContext(applicationContext);
        controller.setCommandBeanName("mockCommandBean");
        controller.setControllerTools(controllerTools);
        expect(applicationContext.getBean("mockCommandBean")).andReturn(command).anyTimes();
    }
    
    public void testHandle() throws Exception {
        command.apply();
        expect(command.getModel()).andReturn(new ModelMap("foo", 95));
        expect(command.getRelativeViewName()).andReturn("pony");

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("template/ajax/pony", mv.getViewName());
        assertContainsPair(mv.getModel(), "foo", 95);
    }
    
    public void testHandleGetIsError() throws Exception {
        request.setMethod("GET");
        replayMocks();
        assertNull(controller.handleRequest(request, response));
        verifyMocks();

        assertEquals("Wrong HTTP status code", HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        assertEquals("Wrong error message", "POST is the only valid method for this URL", response.getErrorMessage());
    }
}
