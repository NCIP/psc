package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import static org.easymock.classextension.EasyMock.expect;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Rhett Sutphin
 */
public class AddToControllerTest extends ControllerTestCase {
    private AddToController controller;

    private StudyDao studyDao;
    private EpochDao epochDao;

    protected void setUp() throws Exception {
        super.setUp();
        studyDao = registerDaoMockFor(StudyDao.class);
        epochDao = registerDaoMockFor(EpochDao.class);

        controller = new AddToController();
        controller.setEpochDao(epochDao);
        controller.setStudyDao(studyDao);
    }
    
    public void testCreateCommandEpoch() throws Exception {
        request.addParameter("study", "18");
        Object command = controller.getCommand(request);
        assertTrue("Wrong command type created", command instanceof AddEpochCommand);
    }
    
    public void testCreateCommandArm() throws Exception {
        request.addParameter("epoch", "77");
        Object command = controller.getCommand(request);
        assertTrue("Wrong command type created", command instanceof AddArmCommand);
    }
    
    public void testCreateCommandInvalidParams() throws Exception {
        try {
            controller.getCommand(request);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("No command matches the given parameters", iae.getMessage());
        }
    }

    public void testHandle() throws Exception {
        final AddToCommand command = registerMockFor(AddToCommand.class);
        controller = new AddToController() {
            protected Object getCommand(HttpServletRequest request) throws Exception {
                return command;
            }
        };
        controller.setEpochDao(epochDao);
        controller.setStudyDao(studyDao);
        command.apply();
        expect(command.getModel()).andReturn(new ModelMap("foo", 95));
        expect(command.whatAdded()).andReturn("pony");

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("template/ajax/addPony", mv.getViewName());
        assertContainsPair(mv.getModel(), "foo", 95);
    }
}
