package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import static org.easymock.classextension.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Rhett Sutphin
 */
public class MarkCompleteControllerTest extends ControllerTestCase {
    private MarkCompleteController mockCommandController;
    private MarkCompleteController controller;
    private MarkCompleteCommand mockCommand;
    private MarkCompleteCommand command;
    private StudyDao studyDao;

    protected void setUp() throws Exception {
        super.setUp();

        studyDao = registerDaoMockFor(StudyDao.class);
        mockCommand = registerMockFor(MarkCompleteCommand.class);

        mockCommandController = new MarkCompleteController() {
            @Override
            protected Object formBackingObject(HttpServletRequest request) throws Exception {
                return mockCommand;
            }
        };
        mockCommandController.setStudyDao(studyDao);
        command = new MarkCompleteCommand(studyDao);
        controller = new MarkCompleteController() {
            @Override
            protected Object formBackingObject(HttpServletRequest request) throws Exception {
                return command;
            }
        };
        controller.setStudyDao(studyDao);
    }

    public void testBindStudy() throws Exception {
        request.setMethod("GET");

        int id = 4;
        request.addParameter("study", Integer.toString(id));
        Study study = setId(id, new Study());
        expect(studyDao.getById(id)).andReturn(study);

        replayMocks();
        controller.handleRequest(request, response);
        verifyMocks();

        assertSame(study, command.getStudy());
    }

    public void testBindCompleted() throws Exception {
        request.setMethod("GET");
        request.addParameter("completed", "true");

        replayMocks();
        controller.handleRequest(request, response);
        verifyMocks();

        assertTrue(command.getCompleted());
    }

    public void testApplyOnPost() throws Exception {
        mockCommand.apply();

        replayMocks();
        mockCommandController.handleRequest(request, response);
        verifyMocks();
    }

    public void testModelAndViewOnPost() throws Exception {
        mockCommand.apply();

        replayMocks();
        ModelAndView mv = mockCommandController.handleRequest(request, response);
        verifyMocks();

        assertEquals("redirectToStudyList", mv.getViewName());
        assertEquals(0, mv.getModel().size());
    }
}
