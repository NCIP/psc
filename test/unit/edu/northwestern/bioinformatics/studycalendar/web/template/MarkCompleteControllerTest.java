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
    private MarkCompleteController controller;
    private MarkCompleteCommand command;
    private StudyDao studyDao;

    protected void setUp() throws Exception {
        super.setUp();

        studyDao = registerDaoMockFor(StudyDao.class);
        command = registerMockFor(MarkCompleteCommand.class);

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
        command.setStudy(study);

        replayMocks();
        controller.handleRequest(request, response);
        verifyMocks();
    }

    public void testBindCompleted() throws Exception {
        request.setMethod("GET");
        request.addParameter("completed", "true");
        command.setCompleted(true);

        replayMocks();
        controller.handleRequest(request, response);
        verifyMocks();
    }

    public void testApplyOnPost() throws Exception {
        command.apply();

        replayMocks();
        controller.handleRequest(request, response);
        verifyMocks();
    }

    public void testModelAndViewOnPost() throws Exception {
        command.apply();

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("redirectToStudyList", mv.getViewName());
        assertEquals(0, mv.getModel().size());
    }
}
