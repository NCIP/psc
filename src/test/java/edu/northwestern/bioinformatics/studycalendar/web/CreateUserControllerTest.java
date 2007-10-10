package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import static org.easymock.EasyMock.expect;
import org.springframework.validation.Errors;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Collections;

/**
 * @author John Dzak
 */
public class CreateUserControllerTest extends ControllerTestCase {
    UserService userService;
    CreateUserController controller;
    private SiteDao siteDao;
    private UserDao userDao;


    @Override
    protected void setUp() throws Exception {
        super.setUp();

        userService = registerMockFor(UserService.class);
        siteDao     = registerDaoMockFor(SiteDao.class);
        userDao     = registerDaoMockFor(UserDao.class);

        controller = new CreateUserController();
        controller.setUserService(userService);
        controller.setSiteDao(siteDao);
        controller.setUserDao(userDao);
        controller.setControllerTools(controllerTools);
    }

    public void testParticipantAssignedOnSubmit() throws Exception {
        MockableCommand mockCommand = registerMockFor(MockableCommand.class, MockableCommand.class.getMethod("apply"));
        CreateUserController mockableController = new MockableCommandController(mockCommand, userService);

        expect(mockCommand.apply()).andReturn(createNamedInstance("Joe", User.class));
        replayMocks();

        ModelAndView mv = mockableController.handleRequest(request, response);
        verifyMocks();

        assertEquals("Wrong view", "listUsers", ((RedirectView)mv.getView()).getUrl());
    }

    public void testBindRolesGrid() throws Exception {
        Site site = setId(0, createNamedInstance("Mayo Clinic", Site.class));

        request.addParameter("rolesGrid[0]['PARTICIPANT_COORDINATOR'].selected", "true");
        request.setMethod("POST");

        expect(siteDao.getAll()).andReturn(Collections.singletonList(site));
        expect(siteDao.getById(0)).andReturn(site);
        
        replayMocks();

        Map<String, Object> model = controller.handleRequest(request, response).getModel();

        assertNoBindingErrorsFor("rolesGrid", model);
        CreateUserCommand command = (CreateUserCommand) model.get("command");
        verifyMocks();

        assertNotNull("Command Object null", command);

        assertNotNull("User site/roles map null", command.getRolesGrid());
        assertTrue("Does not contain site key", command.getRolesGrid().containsKey(site));

        assertNotNull("Roles map null", command.getRolesGrid().get(site));
        assertTrue("Does not contain role", command.getRolesGrid().get(site).containsKey(Role.PARTICIPANT_COORDINATOR));

        assertNotNull("Role Cell null", command.getRolesGrid().get(site).get(Role.PARTICIPANT_COORDINATOR));
        assertEquals("Selected should be true", true, command.getRolesGrid().get(site).get(Role.PARTICIPANT_COORDINATOR).isSelected());
    }




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
