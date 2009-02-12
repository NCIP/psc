package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import static edu.northwestern.bioinformatics.studycalendar.test.ServicedFixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.test.ServicedFixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.activity.AdvancedEditActivityCommand;
import static org.easymock.classextension.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Rhett Sutphin
 */
public class NewActivityControllerTest extends ControllerTestCase {
    private NewActivityController controller;
    private ActivityDao activityDao;
    private ActivityTypeDao activityTypeDao;
    private SourceDao sourceDao;
    private Source source;
    private List<ActivityType> activityTypes = new ArrayList<ActivityType>();

    protected void setUp() throws Exception {
        super.setUp();
        activityDao = registerMockFor(ActivityDao.class);
        sourceDao   = registerDaoMockFor(SourceDao.class);
        activityTypeDao = registerDaoMockFor(ActivityTypeDao.class);

        controller = new NewActivityController();
        controller.setActivityDao(activityDao);
        controller.setSourceDao(sourceDao);
        controller.setActivityTypeDao(activityTypeDao);
        controller.setValidateOnBinding(false);
        controller.setControllerTools(controllerTools);

        source = createNamedInstance(NewActivityController.PSC_CREATE_NEW_ACTIVITY_SOURCE_NAME, Source.class);
    }

    public void testFormView() throws Exception {
        expect(activityTypeDao.getAll()).andReturn(activityTypes).anyTimes();
        request.setMethod("GET");

        replayMocks();

        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
                
        assertEquals("advancedEditActivity", mv.getViewName());
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
        ActivityType expected = ServicedFixtures.createActivityType("LAB_TEST");
        expect(activityTypeDao.getById(4)).andReturn(expected).anyTimes();
        request.setMethod("POST");
        request.addParameter("activity.type", "4");
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
        assertEquals(14,mv.getModel().get("period"));
        assertTrue(mv.getModel().containsKey("selectedActivity"));
    }

    public void testBindActivityType() throws Exception {
        ActivityType expected = ServicedFixtures.createActivityType("LAB_TEST");
        expect(activityTypeDao.getById(1)).andReturn(expected).anyTimes();
        expect(sourceDao.getByName(NewActivityController.PSC_CREATE_NEW_ACTIVITY_SOURCE_NAME)).andReturn(source);
        activityDao.save((Activity) notNull());

        replayMocks();
        request.addParameter("activity.type", "1");
        ModelAndView mv = controller.handleRequest(request, response);
        AdvancedEditActivityCommand command = (AdvancedEditActivityCommand) mv.getModel().get("command");

        verifyMocks();
        assertSame(expected, command.getActivity().getType());
    }
}
