package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.AdverseEventNotificationDao;
import edu.northwestern.bioinformatics.studycalendar.domain.AdverseEventNotification;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Rhett Sutphin
 */
public class DismissAeControllerTest extends ControllerTestCase {
    private DismissAeController controller;

    private AdverseEventNotificationDao notificationDao;

    protected void setUp() throws Exception {
        super.setUp();
        notificationDao = registerDaoMockFor(AdverseEventNotificationDao.class);
        controller = new DismissAeController();
        controller.setNotificationDao(notificationDao);
        controller.setControllerTools(controllerTools);
    }

    public void testBindNotification() throws Exception {
        AdverseEventNotification expectedAEN = setId(17, new AdverseEventNotification());
        expect(notificationDao.getById(17)).andReturn(expectedAEN);
        notificationDao.save(expectedAEN);
        request.addParameter("notification", "17");

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        DismissAeCommand command = (DismissAeCommand) mv.getModel().get("command");
        assertNotNull("Command missing", command);
        assertSame("Wrong notification bound", expectedAEN, command.getNotification());
    }
}
