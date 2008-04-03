package edu.northwestern.bioinformatics.studycalendar.web.reporting;

import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author John Dzak
 */
public class ScheduledActivitiesReportControllerTest extends ControllerTestCase {
    private ScheduledActivitiesReportController controller;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        controller = new ScheduledActivitiesReportController();
    }

    public void testCreateModel() {
        assertNotNull("Model should contain modes", controller.createModel().get("modes"));
    }

    public void testHandle() throws Exception {
        ModelAndView mv = controller.handleRequest(request, response);
        assertEquals("Wrong view", "reporting/scheduledActivitiesReport", mv.getViewName());
    }
}
