package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import gov.nih.nci.security.AuthenticationManager;
import gov.nih.nci.security.exceptions.CSException;
import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class LoginCommandTest extends StudyCalendarTestCase {
    private static final String USERNAME = "alice";
    private static final String PASSWORD = "wonderland";

    private LoginCommand command;

    private AuthenticationManager authenticationManager;

    protected void setUp() throws Exception {
        super.setUp();
        authenticationManager = registerMockFor(AuthenticationManager.class);
        command = new LoginCommand(authenticationManager);
        command.setUsername(USERNAME);
        command.setPassword(PASSWORD);
    }

    public void testLoginSuccessfulWhenSuccessful() throws Exception {
        expect(authenticationManager.login(USERNAME, PASSWORD)).andReturn(true);
        replayMocks();
        assertTrue(command.login());
        verifyMocks();
    }

    public void testLoginFailsWhenFails() throws Exception {
        expect(authenticationManager.login(USERNAME, PASSWORD)).andReturn(false);
        replayMocks();
        assertFalse(command.login());
        verifyMocks();
    }
    
    public void testLoginFailsWhenException() throws Exception {
        expect(authenticationManager.login(USERNAME, PASSWORD)).andThrow(new CSException());
        replayMocks();
        assertFalse(command.login());
        verifyMocks();
    }
}
