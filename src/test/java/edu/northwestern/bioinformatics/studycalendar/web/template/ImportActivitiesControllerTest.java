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
    }

    public void testSubmit() throws Exception {
        request.setMethod("POST");

        command.apply();
        replayMocks();

        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertNotNull("View is null", mv.getViewName());
    }

    private ImportActivitiesCommand getAndReturnCommand(String expectNoErrorsForField) throws Exception {
        request.setMethod("GET");
        replayMocks();
        Map<String, Object> model = controller.handleRequest(request, response).getModel();
        ControllerTestCase.assertNoBindingErrorsFor(expectNoErrorsForField, model);
        ImportActivitiesCommand command = (ImportActivitiesCommand) model.get("command");
        verifyMocks();
        resetMocks();
        return command;
    }
}
