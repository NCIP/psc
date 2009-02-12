package edu.northwestern.bioinformatics.studycalendar.web;

import static edu.northwestern.bioinformatics.studycalendar.test.ServicedFixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import static org.easymock.EasyMock.expect;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;

/**
 * @author John Dzak
 */
public class CreateUserControllerTest extends ControllerTestCase {
    UserService userService;
    CreateUserController controller;
    private CreateUserCommand command;


    @Override
    protected void setUp() throws Exception {
        super.setUp();

        command     = registerMockFor(CreateUserCommand.class);
        userService = registerMockFor(UserService.class);

        controller = new CreateUserController();
        controller.setUserService(userService);
        controller.setControllerTools(controllerTools);
        controller.setValidateOnBinding(false);
    }

    public void testSubjectAssignedOnSubmit() throws Exception {
        CreateUserController mockableController = new MockableCommandController();
        expect(command.apply()).andReturn(createNamedInstance("Joe", User.class));
        replayMocks();

        ModelAndView mv = mockableController.handleRequest(request, response);
        verifyMocks();

        assertEquals("Wrong view", "listUsers", ((RedirectView)mv.getView()).getUrl());
    }

    ///////////    Mockable Controller
    private class MockableCommandController extends CreateUserController {

        public MockableCommandController() {
            setValidateOnBinding(false);
        }

        protected Object formBackingObject(HttpServletRequest request) throws Exception {
            return command;
        }

        protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception { }
    }

}
