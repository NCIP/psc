package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class NewStudyControllerTest extends ControllerTestCase {
    private NewStudyController controller = new NewStudyController();

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
}
