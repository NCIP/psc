package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Rhett Sutphin
 */
public class StudyListControllerTest extends ControllerTestCase {
    private StudyListController controller;
    private StudyDao studyDao;

    protected void setUp() throws Exception {
        super.setUp();
        controller = new StudyListController();
        studyDao = registerMockFor(StudyDao.class);
        controller.setStudyDao(studyDao);
    }

    public void testModelAndView() throws Exception {
        List<Study> theList = new ArrayList<Study>();
        expect(studyDao.getAll()).andReturn(theList);
        replayMocks();

        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertSame("Studies list missing or wrong", theList, mv.getModel().get("studies"));
        assertEquals("studyList", mv.getViewName());
    }
}
