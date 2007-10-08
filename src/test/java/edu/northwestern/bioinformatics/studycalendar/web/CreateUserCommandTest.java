package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.EasyMock.expect;

public class CreateUserCommandTest extends StudyCalendarTestCase {
    UserService service;
    CreateUserCommand command;

    protected void setUp() throws Exception {
        super.setUp();

        command = new CreateUserCommand();
        service = registerMockFor(UserService.class);
        command.setUserService(service);
    }

    public void testApplyForNewUser() throws Exception {
        User expectedUser = Fixtures.createUser(null, "Joe", null, new Role[]{Role.STUDY_COORDINATOR}, true, "pass");

        command.setName(expectedUser.getName());
        command.setUserRoles(expectedUser.getUserRoles());
        command.setActiveFlag(expectedUser.getActiveFlag());
        command.setPassword(expectedUser.getPlainTextPassword());

        expect(service.saveUser(expectedUser)).andReturn(expectedUser);
        replayMocks();

        User actualUser = command.apply();
        verifyMocks();

        assertEquals("Different user names", expectedUser.getName(), actualUser.getName());
    }
}
