package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.NotificationDao;
import edu.northwestern.bioinformatics.studycalendar.domain.AdverseEvent;
import static edu.northwestern.bioinformatics.studycalendar.test.ServicedFixtures.setId;
import edu.northwestern.bioinformatics.studycalendar.domain.Notification;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import static org.easymock.classextension.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class DismissAeControllerTest extends ControllerTestCase {
    private DismissAeController controller;

    private NotificationDao notificationDao;

    protected void setUp() throws Exception {
        super.setUp();
        notificationDao = registerDaoMockFor(NotificationDao.class);
        controller = new DismissAeController();
        controller.setNotificationDao(notificationDao);
        controller.setControllerTools(controllerTools);
    }

    public void testBindNotification() throws Exception {
        AdverseEvent ae = new AdverseEvent();
        ae.setDescription("Grade 4 adverse event");
        ae.setDetectionDate(new Date());


        Notification expectedAEN = setId(17, new Notification(ae));
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
