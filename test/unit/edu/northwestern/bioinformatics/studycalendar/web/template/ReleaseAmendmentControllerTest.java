package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import static org.easymock.classextension.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Rhett Sutphin
 */
public class ReleaseAmendmentControllerTest extends ControllerTestCase {
    private ReleaseAmendmentController mockCommandController;
    private ReleaseAmendmentController controller;
    private ReleaseAmendmentCommand mockCommand;
    private ReleaseAmendmentCommand command;
    private StudyDao studyDao;
    private AmendmentService amendmentService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        studyDao = registerDaoMockFor(StudyDao.class);
        amendmentService = registerMockFor(AmendmentService.class);
        mockCommand = registerMockFor(ReleaseAmendmentCommand.class);

        mockCommandController = new ReleaseAmendmentController() {
            @Override
            protected Object formBackingObject(HttpServletRequest request) throws Exception {
                return mockCommand;
            }
        };
        mockCommandController.setStudyDao(studyDao);
        command = new ReleaseAmendmentCommand(amendmentService);
        controller = new ReleaseAmendmentController() {
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
