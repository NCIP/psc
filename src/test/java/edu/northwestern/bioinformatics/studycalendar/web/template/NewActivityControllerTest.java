package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import static org.easymock.classextension.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Rhett Sutphin
 */
public class NewActivityControllerTest extends ControllerTestCase {
    private NewActivityController controller;
    private ActivityDao activityDao;
    private SourceDao sourceDao;
    private Source source;

    protected void setUp() throws Exception {
        super.setUp();
        activityDao = registerMockFor(ActivityDao.class);
        sourceDao   = registerDaoMockFor(SourceDao.class);

        controller = new NewActivityController();
        controller.setActivityDao(activityDao);
        controller.setSourceDao(sourceDao);
        controller.setValidateOnBinding(false);

        source = createNamedInstance(NewActivityController.PSC_CREATE_NEW_ACTIVITY_SOURCE_NAME, Source.class);
    }

    public void testFormView() throws Exception {
        request.setMethod("GET");
        replayMocks();

        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
                
        assertEquals("editActivity", mv.getViewName());
        assertEquals(NewActivityController.PSC_CREATE_NEW_ACTIVITY_SOURCE_NAME, ((String)mv.getModel().get("sourceName")));
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
        expect(sourceDao.getByName(NewActivityController.PSC_CREATE_NEW_ACTIVITY_SOURCE_NAME)).andReturn(source);
        activityDao.save((Activity) notNull());
    }

    public void testSuccessResponseWithReturn() throws Exception {
        expectSuccessfulSubmit();
        replayMocks();
        request.addParameter("returnToPeriod", "14");
        ModelAndView mv = controller.handleRequest(request, response);
        assertEquals("redirectToManagePeriod", mv.getViewName());
        assertEquals(2, mv.getModel().size());
        assertEquals(14, mv.getModel().get("period"));
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
