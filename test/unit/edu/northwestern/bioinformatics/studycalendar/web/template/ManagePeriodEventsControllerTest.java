package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedEventDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;
import static org.easymock.classextension.EasyMock.expect;
import org.easymock.classextension.EasyMock;
import org.easymock.IArgumentMatcher;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingErrorProcessor;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Rhett Sutphin
 * @author Jaron Sampson
 */
public class ManagePeriodEventsControllerTest extends ControllerTestCase {
    private ManagePeriodEventsController controller = new ManagePeriodEventsController();
    private PeriodDao periodDao;
    private PlannedEventDao plannedEventDao;
    private ActivityDao activityDao;
    private Period period = createPeriod("7th", 10, 8, 4);
    private List<Activity> activities = new LinkedList<Activity>();
    private ManagePeriodEventsCommand command;

    protected void setUp() throws Exception {
        super.setUp();
        Study parent = createNamedInstance("Root", Study.class);
        parent.setPlannedCalendar(new PlannedCalendar());
        parent.getPlannedCalendar().addEpoch(Epoch.create("Holocene", "Middle"));
        parent.getPlannedCalendar().getEpochs().get(0).getArms().get(0).addPeriod(period);

        periodDao = registerDaoMockFor(PeriodDao.class);
        activityDao = registerDaoMockFor(ActivityDao.class);
        plannedEventDao = registerMockFor(PlannedEventDao.class);

        controller.setPeriodDao(periodDao);
        controller.setActivityDao(activityDao);
        controller.setPlannedEventDao(plannedEventDao);
        controller.setControllerTools(controllerTools);
            
        request.setMethod("GET");
        request.addParameter("id", "15");

        expect(periodDao.getById(15)).andReturn(period).anyTimes();
        expect(activityDao.getAll()).andReturn(activities).anyTimes();

        command = new ManagePeriodEventsCommand(period, plannedEventDao);
    }

    public void testFormBackingObject() throws Exception {
        periodDao.evict(period);
        replayMocks();

        Object command = controller.formBackingObject(request);
        verifyMocks();
        assertTrue(command instanceof ManagePeriodEventsCommand);
        assertSame(period, ((ManagePeriodEventsCommand) command).getPeriod());
    }

    public void testFormProcessing() throws Exception {


        request.addParameter("grid[0].addition","true");
        request.addParameter("grid[0].columnNumber","3");
        request.addParameter("grid[0].conditionalCheckbox",	"false");
        request.addParameter("grid[0].conditionalDetails","");
        request.addParameter("grid[0].conditionalUpdated",	"false" );
        request.addParameter("grid[0].details",	"abc");
        request.addParameter("grid[0].eventIds[0]",	"646" );
        request.addParameter("grid[0].eventIds[1]",	"-1" );
        request.addParameter("grid[0].eventIds[2]",	"645" );
        request.addParameter("grid[0].eventIds[3]",	"-1" );
        request.addParameter("grid[0].rowNumber",	"2" );
        request.addParameter("grid[0].updated",	"true" );
        plannedEventDao.save(eqPlannedEvent(new PlannedEvent()));

        periodDao.evict(period);

        replayMocks();

        ManagePeriodEventsCommand command
            = (ManagePeriodEventsCommand) controller.handleRequest(request, response).getModel().get("command");
        ModelAndView model = controller.processFormSubmission(request, response, command, null);
        verifyMocks();
        assertEquals("Periods are not the same" , period, model.getModel().get("period"));
        assertEquals("Parameter rowNumber is not the same", 2, model.getModel().get("rowNumber"));
        assertEquals("Parameter columnNumber is not the same", 3, model.getModel().get("columnNumber"));
        assertEquals("Study is not the same", period.getArm().getEpoch().getPlannedCalendar().getStudy(), model.getModel().get("study"));
    }

    public void testBindingGridDetails() throws Exception {
        request.addParameter("grid[7].details", "Anything");
        periodDao.evict(period);
        replayMocks();

        ManagePeriodEventsCommand command
            = (ManagePeriodEventsCommand) controller.handleRequest(request, response).getModel().get("command");
        assertEquals("Value not bound", "Anything", command.getGrid().get(7).getDetails());
    }

    public void testBindingGridDetailsBlankIsNull() throws Exception {
        request.addParameter("grid[7].details", "  ");
        periodDao.evict(period);
        replayMocks();

        ManagePeriodEventsCommand command
            = (ManagePeriodEventsCommand) controller.handleRequest(request, response).getModel().get("command");
        assertEquals("Value not bound", null, command.getGrid().get(7).getDetails());
    }

    public void testBindingGridCount() throws Exception {
        request.addParameter("grid[7].eventIds[4]", "11");
        periodDao.evict(period);
        replayMocks();

        ManagePeriodEventsCommand command
            = (ManagePeriodEventsCommand) controller.handleRequest(request, response).getModel().get("command");
        Integer actual = command.getGrid().get(7).getEventIds().get(4);
        assertEquals("Value not bound", 11, (int) actual);
    }

    public void testBindingGridCountBlankIsZero() throws Exception {
        request.addParameter("grid[7].eventIds[4]", "22");
        periodDao.evict(period);        
        replayMocks();

        ManagePeriodEventsCommand command
            = (ManagePeriodEventsCommand) controller.handleRequest(request, response).getModel().get("command");
        Integer actual = command.getGrid().get(7).getEventIds().get(4);
        assertEquals("Value not bound", 22, (int) actual);
    }

    public void testBindingGridActivity() throws Exception {
        request.addParameter("grid[3].activity", "9");
        Activity expectedActivity = setId(9, new Activity());
        expect(activityDao.getById(9)).andReturn(expectedActivity);
        periodDao.evict(period);
        replayMocks();

        ManagePeriodEventsCommand command
            = (ManagePeriodEventsCommand) controller.handleRequest(request, response).getModel().get("command");
        verifyMocks();
        assertSame("Value not bound", expectedActivity, command.getGrid().get(3).getActivity());
    }

    public void testFormView() throws Exception {
        periodDao.evict(period);
        replayMocks();
        assertEquals("managePeriod", controller.handleRequest(request, response).getViewName());
        verifyMocks();
    }

    public void testFormModel() throws Exception {
        periodDao.evict(period);
        replayMocks();
        Map<String, Object> model = controller.handleRequest(request, response).getModel();
        verifyMocks();

        assertEquals(period, model.get("period"));
        assertEquals(activities, model.get("activities"));
        assertNotNull(model.get("activitiesById"));
        assertEquals(period.getArm().getEpoch().getPlannedCalendar().getStudy(), model.get("study"));
    }

    public void testReferenceDataIncludesNewActivityIfParamPresent() throws Exception {
        request.addParameter("selectedActivity", "43");
        Activity expectedActivity = setId(43, new Activity());
        expect(activityDao.getById(43)).andReturn(expectedActivity);
        replayMocks();

        Map<String, Object> refdata = controller.referenceData(request, command, null);
        assertSame(expectedActivity, refdata.get("selectedActivity"));
        verifyMocks();
    }


    public void testReferenceDataEmptyIfNoNewActivity() throws Exception {
        request.removeParameter("selectedActivity");
        replayMocks();

        assertNotContains(controller.referenceData(request, command, null).keySet(), "selectedActivity");
        verifyMocks();
    }

    public static PlannedEvent eqPlannedEvent(PlannedEvent event) {
        EasyMock.reportMatcher(new PlannedEventMatcher(event));
        return null;
    }

    private static class PlannedEventMatcher implements IArgumentMatcher {
        private PlannedEvent expectedPlannedEvent;

        public PlannedEventMatcher(PlannedEvent expectedPlannedEvent) {
            this.expectedPlannedEvent = expectedPlannedEvent;
        }

        public boolean matches(Object object) {
            if(!(object instanceof PlannedEvent)) {
                return false;
            }
            //we don't care if the events are not the same for this test
            return true;
        }

        public void appendTo(StringBuffer sb) {
            sb.append("PlannedEvent activity=").append(expectedPlannedEvent.getActivity());
        }
    }
}
