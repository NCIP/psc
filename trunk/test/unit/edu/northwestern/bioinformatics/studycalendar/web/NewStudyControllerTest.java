package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.*;

import java.util.Map;
import java.util.List;
import java.util.Arrays;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;

/**
 * @author Rhett Sutphin
 */
public class NewStudyControllerTest extends ControllerTestCase {
    private NewStudyController controller = new NewStudyController();
    private StudyDao studyDao;

    protected void setUp() throws Exception {
        super.setUp();
        studyDao = registerMockFor(StudyDao.class);
        controller.setStudyDao(studyDao);
    }

    public void testReferenceData() throws Exception {
        Map<String, Object> refdata = controller.referenceData(request);
        assertEquals("Wrong action name", "New", refdata.get("action"));
    }

    public void testViewOnGet() throws Exception {
        request.setMethod("GET");
        ModelAndView mv = controller.handleRequest(request, response);
        assertEquals("editStudy", mv.getViewName());
    }

    public void testViewOnGoodSubmit() throws Exception {
        request.addParameter("studyName", "Study of things and stuff");
        request.addParameter("arms", "no");
        ModelAndView mv = controller.handleRequest(request, response);
        assertEquals("viewStudy", mv.getViewName());
    }

    public void testBindHasArms() throws Exception {
        request.addParameter("arms", "yes");
        NewStudyCommand command = postAndReturnCommand();
        assertTrue(command.getArms());
    }

    public void testBindStudyName() throws Exception {
        String studyName = "This study right here";
        request.addParameter("studyName", studyName);
        NewStudyCommand command = postAndReturnCommand();
        assertEquals(studyName, command.getStudyName());
    }

    public void testBindArmNames() throws Exception {
        List<String> names = Arrays.asList("The arm", "An arm");
        request.addParameter("armNames[0]", names.get(0));
        request.addParameter("armNames[1]", names.get(1));

        NewStudyCommand command = postAndReturnCommand();
        assertEquals(names.get(0), command.getArmNames().get(0));
        assertEquals(names.get(1), command.getArmNames().get(1));
    }

    private NewStudyCommand postAndReturnCommand() throws Exception {
        request.setMethod("POST");
        studyDao.save((Study) notNull());  // TODO: once there is validation, this won't happen
        expectLastCall().atLeastOnce().asStub();

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        Object command = mv.getModel().get("command");
        assertNotNull("Command not present in model: " + mv.getModel(), command);
        return (NewStudyCommand) command;
    }
}
