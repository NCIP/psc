package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreator;
import static org.easymock.classextension.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Rhett Sutphin
 */
public class NewStudyControllerTest extends ControllerTestCase {
    private static final int ID = 81;

    private NewStudyController controller;
    private StudyDao studyDao;
    private NewStudyCommand command;

    protected void setUp() throws Exception {
        super.setUp();
        request.setMethod("GET");
        studyDao = registerMockFor(StudyDao.class);
        command = registerMockFor(NewStudyCommand.class);

        controller = new NewStudyController() {
            @Override
            protected Object getCommand(HttpServletRequest request) throws Exception {
                return command;
            }
        };
        controller.setStudyDao(studyDao);
    }

    public void testHandle() throws Exception {
        Study study = setId(ID, new Study());
        expect(command.create()).andReturn(study);

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("redirectToCalendarTemplate", mv.getViewName());
        assertEquals(1, mv.getModel().size());
        assertContainsPair(mv.getModel(), "study", ID);
    }
    
    public void testBindMode() throws Exception {
        request.setParameter("base", "BLANK");
        command.setBase(TemplateSkeletonCreator.BLANK);
        expect(command.create()).andReturn(setId(ID, new Study()));

        replayMocks();
        controller.handleRequest(request, response);
        verifyMocks();
    }

}
