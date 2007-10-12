package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import static org.easymock.EasyMock.expect;
import org.easymock.classextension.EasyMock;
import org.easymock.internal.matchers.ArrayEquals;
import org.easymock.IArgumentMatcher;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author John Dzak
 */
public class CreateUserControllerTest extends ControllerTestCase {
    UserService userService;
    CreateUserController controller;
    private SiteDao siteDao;
    private UserDao userDao;
    private User user;
    private Site site;
    private List sites;


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
        controller.setValidateOnBinding(false);

        site = setId(0, createNamedInstance("Mayo Clinic", Site.class));
        sites = Collections.singletonList(site);

        user = createUser(-1, "John", -1L, true, "pass");
        user.setUserRoles(Collections.singleton(createUserRole(Role.PARTICIPANT_COORDINATOR, site)));
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
        User expectedUser = createUser(user.getId(), user.getName(), user.getCsmUserId(), user.getActiveFlag(), user.getPlainTextPassword());
        expectedUser.setUserRoles(Collections.singleton(createUserRole(Role.SITE_COORDINATOR, site)));


        request.addParameter("_rolesGrid[0]['PARTICIPANT_COORDINATOR'].selected", "1");
        request.addParameter("rolesGrid[0]['SITE_COORDINATOR'].selected", "true");
        setParametersForPost();
        request.setMethod("POST");

        expect(siteDao.getAll()).andReturn(Arrays.asList(site));
        expect(siteDao.getById(0)).andReturn(site).anyTimes();
        expect(userService.saveUser(userDetailsEq(expectedUser))).andReturn(expectedUser);

        replayMocks();

        ModelAndView mv = controller.handleRequest(request, response);

        verifyMocks();

        assertEquals("Wrong view", "listUsers", ((RedirectView)mv.getView()).getUrl());
    }

    public void testFormBackingObjectWithOutId() throws Exception {
        User expectedUser = new User();

        request.setMethod("GET");

        expect(siteDao.getAll()).andReturn(new ArrayList());

        replayMocks();

        Map<String, Object> model = controller.handleRequest(request, response).getModel();
        CreateUserCommand command = (CreateUserCommand) model.get("command");
        verifyMocks();

        assertEquals("Wrong User", expectedUser.getName(), command.getUser().getName());
    }

    public void testFormBackingObjectWithId() throws Exception {
        request.setParameter("id", "-1");
        request.setMethod("GET");

        expect(userService.getUserById(-1)).andReturn(user);
        expect(siteDao.getAll()).andReturn(sites);

        replayMocks();

        Map<String, Object> model = controller.handleRequest(request, response).getModel();
        CreateUserCommand command = (CreateUserCommand) model.get("command");
        assertNoBindingErrorsFor("user", model);
        assertNoBindingErrorsFor("userRoles", model);
        verifyMocks();

        assertNotNull("Command Object null", command);

        assertEquals("Wrong User", user.getName(), command.getUser().getName());
        assertEquals("Re enter password is empty", user.getPlainTextPassword(), command.getRePassword());

        assertNotNull("User site/roles map null", command.getRolesGrid());
        assertTrue("Does not contain site key", command.getRolesGrid().containsKey(site));

        assertNotNull("Roles map null", command.getRolesGrid().get(site));
        assertTrue("Does not contain role", command.getRolesGrid().get(site).containsKey(Role.PARTICIPANT_COORDINATOR));

        assertNotNull("Role Cell null", command.getRolesGrid().get(site).get(Role.PARTICIPANT_COORDINATOR));
        assertEquals("Selected should be true", true, command.getRolesGrid().get(site).get(Role.PARTICIPANT_COORDINATOR).isSelected());
    }

    protected void setParametersForPost() throws Exception {
        request.setParameter("user.id", user.getId().toString());
        request.setParameter("user.csmUserId", user.getCsmUserId().toString());
        request.setParameter("user.activeFlag", user.getActiveFlag().toString());
        request.setParameter("user.name", user.getName());
        request.setParameter("user.plainTextPassword", user.getPlainTextPassword());
        request.setParameter("rePassword", user.getPlainTextPassword());
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

    public static User userDetailsEq(User user) {
        EasyMock.reportMatcher(new UserDetailsEquals(user));
        return null;
    }

    public static class UserDetailsEquals implements IArgumentMatcher {
        private User expected;

        public UserDetailsEquals(User expected) {
            this.expected = expected;
        }

        public boolean matches(Object o) {
            if (!(o instanceof User)) {
                return false;
            }
            User actual = ((User) o);

            if (expected.getName() != null ? !expected.getName().equals(actual.getName()) : actual.getName() != null) return false;
            if (expected.getCsmUserId() != null ?
                    !expected.getCsmUserId().equals(actual.getCsmUserId()) : actual.getCsmUserId() != null) return false;
            if (expected.getActiveFlag() != null ? !expected.getActiveFlag().equals(actual.getActiveFlag()) : actual.getActiveFlag() != null) return false;
            try {
                if (expected.getPassword() != null ? !expected.getPassword().equals(actual.getPassword()) : actual.getPassword() != null) return false;
            } catch (Exception e) {e.printStackTrace();}
            if (expected.getUserRoles() != null ?expected.getUserRoles().size() != actual.getUserRoles().size(): actual.getUserRoles() != null) return false;
            for(UserRole userRole : actual.getUserRoles()) {
                if (expected.getUserRoles().contains(userRole) == false) return false;
            }
            return true;
        }

        public void appendTo(StringBuffer buffer) {
            buffer.append("userDetailsEq(");
            buffer.append(expected.getClass().getName());
            buffer.append(" with name \"");
            buffer.append(expected.getName());
            buffer.append("\"");
        }
    }

}
