package edu.northwestern.bioinformatics.studycalendar.web;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Rhett Sutphin
 */
public class AssignParticipantControllerTest extends ControllerTestCase {

    public void testParticipantAssignedOnSubmit() throws Exception {
        AssignParticipantCommand mockCommand = registerMockFor(AssignParticipantCommand.class);
        AssignParticipantController controller = new MockableCommandController(mockCommand);

        mockCommand.assignParticipant();
        replayMocks();

        controller.handleRequest(request, response);
        verifyMocks();
    }

    private class MockableCommandController extends AssignParticipantController {
        private AssignParticipantCommand command;

        public MockableCommandController(AssignParticipantCommand command) {
            this.command = command;
        }

        protected Object formBackingObject(HttpServletRequest request) throws Exception {
            return command;
        }
    }
}
