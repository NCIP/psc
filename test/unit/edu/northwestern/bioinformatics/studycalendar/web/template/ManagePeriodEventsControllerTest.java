package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedEventDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import static org.easymock.classextension.EasyMock.expect;
import org.easymock.classextension.EasyMock;
import org.easymock.IArgumentMatcher;
import org.springframework.web.servlet.ModelAndView;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 * @author Jaron Sampson
 */
public class ManagePeriodEventsControllerTest extends ControllerTestCase {
    private ManagePeriodEventsController controller = new ManagePeriodEventsController();
    private PeriodDao periodDao;
    private PlannedEventDao plannedEventDao;
    private ActivityDao activityDao;
    private Period period, revisedPeriod;
    private List<Activity> activities = new LinkedList<Activity>();
    private ManagePeriodEventsCommand command;
    private AmendmentService amendmentService;
    private StudyService studyService;
    private DeltaService deltaService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        period = createPeriod("7th", 10, 8, 4);
        Study parent = createNamedInstance("Root", Study.class);
        parent.setPlannedCalendar(new PlannedCalendar());
        parent.getPlannedCalendar().addEpoch(Epoch.create("Holocene", "Middle"));
        parent.getPlannedCalendar().getEpochs().get(0).getArms().get(0).addPeriod(period);
        parent.setDevelopmentAmendment(new Amendment("dev"));
        Fixtures.assignIds(parent);

        revisedPeriod = (Period) period.transientClone();

        periodDao = registerDaoMockFor(PeriodDao.class);
        activityDao = registerDaoMockFor(ActivityDao.class);
        plannedEventDao = registerMockFor(PlannedEventDao.class);
        amendmentService = registerMockFor(AmendmentService.class);
        deltaService = registerMockFor(DeltaService.class);
        studyService = registerMockFor(StudyService.class);

        controller.setPeriodDao(periodDao);
        controller.setActivityDao(activityDao);
        controller.setPlannedEventDao(plannedEventDao);
        controller.setControllerTools(controllerTools);
        controller.setDeltaService(deltaService);
        controller.setAmendmentService(amendmentService);
        controller.setStudyService(studyService);
            
        request.setMethod("GET"); // To simplify the binding tests
        request.addParameter("id", "15");

        expect(periodDao.getById(15)).andReturn(period).anyTimes();
        expect(activityDao.getAll()).andReturn(activities).anyTimes();

        command = new ManagePeriodEventsCommand(period, plannedEventDao, amendmentService);
    }

    private ManagePeriodEventsCommand handleAndReturnBoundCommand() throws Exception {
        return (ManagePeriodEventsCommand) doHandle().getModel().get("command");
    }

    private ModelAndView doHandle() throws Exception {
        expect(deltaService.revise(period)).andReturn(revisedPeriod);
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
        return mv;
    }

    public void testFormBackingObjectForGET() throws Exception {
        request.setMethod("GET");
        Period expectedPeriod = new Period();
        expect(deltaService.revise(period)).andReturn(expectedPeriod);

        replayMocks();
        Object command = controller.formBackingObject(request);
        verifyMocks();

        assertTrue(command instanceof ManagePeriodEventsCommand);
        assertSame(expectedPeriod, ((ManagePeriodEventsCommand) command).getPeriod());
    }

    public void testFormBackingObjectForPOST() throws Exception {
        request.setMethod("POST");

        replayMocks();
        Object command = controller.formBackingObject(request);
        verifyMocks();

        assertTrue(command instanceof ManagePeriodEventsCommand);
        assertSame(period, ((ManagePeriodEventsCommand) command).getPeriod());
    }

    public void testBindingGridDetails() throws Exception {
        request.addParameter("grid[7].details", "Anything");
        ManagePeriodEventsCommand command = handleAndReturnBoundCommand();
        assertEquals("Value not bound", "Anything", command.getGrid().get(7).getDetails());
    }

    public void testBindingGridDetailsBlankIsNull() throws Exception {
        request.addParameter("grid[7].details", "  ");

        ManagePeriodEventsCommand command = handleAndReturnBoundCommand();
        assertEquals("Value not bound", null, command.getGrid().get(7).getDetails());
    }

    public void testBindingGridCount() throws Exception {
        request.addParameter("grid[7].eventIds[4]", "11");

        ManagePeriodEventsCommand command = handleAndReturnBoundCommand();
        Integer actual = command.getGrid().get(7).getEventIds().get(4);
        assertEquals("Value not bound", 11, (int) actual);
    }

    public void testBindingGridCountBlankIsZero() throws Exception {
        request.addParameter("grid[7].eventIds[4]", "22");

        ManagePeriodEventsCommand command = handleAndReturnBoundCommand();
        Integer actual = command.getGrid().get(7).getEventIds().get(4);
        assertEquals("Value not bound", 22, (int) actual);
    }

    public void testBindingGridActivity() throws Exception {
        request.addParameter("grid[3].activity", "9");
        Activity expectedActivity = setId(9, new Activity());
        expect(activityDao.getById(9)).andReturn(expectedActivity);

        ManagePeriodEventsCommand command = handleAndReturnBoundCommand();
        assertSame("Value not bound", expectedActivity, command.getGrid().get(3).getActivity());
    }

    public void testFormView() throws Exception {
        assertEquals("managePeriod", doHandle().getViewName());
    }

    @SuppressWarnings({ "unchecked" })
    public void testFormModel() throws Exception {
        Map<String, Object> model = doHandle().getModel();

        assertEquals(revisedPeriod, model.get("period"));
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
