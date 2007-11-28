package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public class ImportActivitiesControllerTest extends ControllerTestCase {
    private ImportActivitiesController controller;
    private ImportActivitiesCommand command;
    List<Activity> activities;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        command = registerMockFor(ImportActivitiesCommand.class, ImportActivitiesCommand.class.getMethod("apply"));
        controller = new ImportActivitiesController() {

            protected ImportActivitiesCommand formBackingObject(HttpServletRequest request) throws Exception {
                return command;
            }
        };

        // Stop controller from calling validation
        controller.setValidators(null);

    }

    public void testSubmit() throws Exception {
        request.setMethod("POST");

        command.apply();
        replayMocks();

        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertNotNull("View is null", mv.getViewName());
        assertEquals("Wrong view", "redirectToStudyList", mv.getViewName());
    }

    public void testSubmitWithReturnToPeriodId() throws Exception {
        request.setMethod("POST");
        request.setParameter("returnToPeriodId", "1");

        command.apply();
        replayMocks();

        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertNotNull("View is null", mv.getViewName());
        assertEquals("Wrong view", "redirectToManagePeriod", mv.getViewName());
        assertNotNull("Period Id is null", mv.getModel().get("id"));
    }

    public void testGet() throws Exception {
        request.setMethod("GET");

        ModelAndView mv = controller.handleRequest(request, response);

        assertNotNull("View is null", mv.getViewName());
    }
}
