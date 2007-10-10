package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import static org.easymock.EasyMock.expect;
import org.springframework.validation.Errors;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author John Dzak
 */
public class CreateUserControllerTest extends ControllerTestCase {
    UserService userService;
    CreateUserController controller;


    @Override
    protected void setUp() throws Exception {
        super.setUp();

        userService = registerMockFor(UserService.class);

        controller = new CreateUserController();
        controller.setUserService(userService);
    }

    public void testParticipantAssignedOnSubmit() throws Exception {
        MockableCommand mockCommand = registerMockFor(MockableCommand.class, MockableCommand.class.getMethod("apply"));
        CreateUserController mockableController = new MockableCommandController(mockCommand, userService);

        expect(mockCommand.apply()).andReturn(Fixtures.createNamedInstance("Joe", User.class));
        replayMocks();

        ModelAndView mv = mockableController.handleRequest(request, response);
        verifyMocks();

        assertEquals("Wrong view", "listUsers", ((RedirectView)mv.getView()).getUrl());
    }

  /*  public void testBindArm() throws Exception {
        request.addParameter("temp[0][0]", "145");
        request.addParameter("temp[0][1]", "123");

        request.setMethod("POST");

        replayMocks();
        Map<String, Object> model = controller.handleRequest(request, response).getModel();

        assertNoBindingErrorsFor("temp", model);
        CreateUserCommand command = (CreateUserCommand) model.get("command");
        verifyMocks();

        assertNotNull("Command Object null", command);
        assertEquals("Wrong size of temp", 2, command.getTemp().size());
        assertEquals("Wrong size of temp", "145", command.getTemp().get(0).get(0));
        assertEquals("Wrong size of temp", "123", command.getTemp().get(0).get(1));
    }*/




    ///////////    Mockable Controller and Command
    private class MockableCommandController extends CreateUserController {
        private CreateUserCommand command;
        private UserService service;

        public MockableCommandController(CreateUserCommand command, UserService service) {
            this.command = command;
            this.service = userService;
        }

        protected Object formBackingObject(HttpServletRequest request) throws Exception {
            command.setUserService(service);
            return command;
        }

        protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception { }
    }

    private class MockableCommand extends CreateUserCommand {

        public MockableCommand(User user, SiteDao siteDao) {
            super(user, siteDao);
        }

        public void validate(Errors errors) { }
    }
}
