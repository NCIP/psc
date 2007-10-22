package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import static org.easymock.classextension.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Rhett Sutphin
 */
public class NewActivityControllerTest extends ControllerTestCase {
    private NewActivityController controller;
    private ActivityDao activityDao;

    protected void setUp() throws Exception {
        super.setUp();
        activityDao = registerMockFor(ActivityDao.class);

        controller = new NewActivityController();
        controller.setActivityDao(activityDao);
        controller.setValidateOnBinding(false);        
    }

    public void testFormView() throws Exception {
        request.setMethod("GET");
        assertEquals("editActivity", controller.handleRequest(request, response).getViewName());
    }

    public void testSuccessResponseBare() throws Exception {
        expectSuccessfulSubmit();
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        assertNotNull(mv.getModel().get("activity"));
        assertEquals(3, mv.getModel().size());
        assertEquals("viewActivity", mv.getViewName());
    }

    private void expectSuccessfulSubmit() {
        request.setMethod("POST");
        request.addParameter("activityType", "4");
        activityDao.save((Activity) notNull());
    }

    public void testSuccessResponseWithReturn() throws Exception {
        expectSuccessfulSubmit();
        replayMocks();
        request.addParameter("returnToPeriodId", "14");
        ModelAndView mv = controller.handleRequest(request, response);
        assertEquals("redirectToManagePeriod", mv.getViewName());
        assertEquals(2, mv.getModel().size());
        assertEquals(14, mv.getModel().get("id"));
        assertTrue(mv.getModel().containsKey("selectedActivity"));
    }

    public void testBindActivityType() throws Exception {
        ActivityType expected = ActivityType.LAB_TEST;
        request.addParameter("activityType", "" + expected.getId());

        ModelAndView mv = controller.handleRequest(request, response);
        NewActivityCommand command = (NewActivityCommand) mv.getModel().get("command");
        assertSame(expected, command.getActivityType());
    }
}
