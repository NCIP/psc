package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;

import static java.util.Arrays.asList;
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
    private PlannedActivityDao plannedActivityDao;
    private ActivityDao activityDao;
    private Period period, revisedPeriod;
    private List<Activity> activities = new LinkedList<Activity>();
    private ManagePeriodEventsCommand command;
    private DeltaService deltaService;
    private TemplateService templateService;
    private Study parent;
    private SourceDao sourceDao;
    private List<Source> sources;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        period = createPeriod("7th", 10, 8, 4);
        parent = createNamedInstance("Root", Study.class);
        parent.setPlannedCalendar(new PlannedCalendar());
        parent.getPlannedCalendar().addEpoch(Epoch.create("Holocene", "Middle"));
        parent.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0).addPeriod(period);
        parent.setDevelopmentAmendment(new Amendment("dev"));
        Fixtures.assignIds(parent);

        sources = asList(createNamedInstance("Source A", Source.class));

        revisedPeriod = (Period) period.transientClone();

        periodDao = registerDaoMockFor(PeriodDao.class);
        sourceDao = registerDaoMockFor(SourceDao.class);
        activityDao = registerDaoMockFor(ActivityDao.class);
        plannedActivityDao = registerDaoMockFor(PlannedActivityDao.class);
        deltaService = registerMockFor(DeltaService.class);
        templateService = registerMockFor(TemplateService.class);

        controller.setPeriodDao(periodDao);
        controller.setSourceDao(sourceDao);
        controller.setActivityDao(activityDao);
        controller.setPlannedActivityDao(plannedActivityDao);
        controller.setDeltaService(deltaService);
        controller.setControllerTools(controllerTools);
        controller.setTemplateService(templateService);
            
        request.setMethod("GET"); // To simplify the binding tests
        request.addParameter("id", "15");

        expect(periodDao.getById(15)).andReturn(period).anyTimes();
        expect(activityDao.getAll()).andReturn(activities).anyTimes();
        command = new ManagePeriodEventsCommand(period);
    }

    private ManagePeriodEventsCommand handleAndReturnBoundCommand() throws Exception {
        return (ManagePeriodEventsCommand) doHandle().getModel().get("command");
    }

    private ModelAndView doHandle() throws Exception {
        expect(sourceDao.getAll()).andReturn(sources);
        expect(deltaService.revise(period)).andReturn(revisedPeriod);
        expect(templateService.findStudy(revisedPeriod)).andReturn(parent);
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
        Object command = controller.getCommand(request);
        verifyMocks();

        assertTrue(command instanceof ManagePeriodEventsCommand);
        assertSame(expectedPeriod, ((ManagePeriodEventsCommand) command).getPeriod());
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
        assertEquals(period.getStudySegment().getEpoch().getPlannedCalendar().getStudy(), model.get("study"));
    }

    public void testReferenceDataIncludesNewActivityIfParamPresent() throws Exception {
        request.addParameter("selectedActivity", "43");
        Activity expectedActivity = setId(43, new Activity());
        expect(activityDao.getById(43)).andReturn(expectedActivity);
        expect(templateService.findStudy(period)).andReturn(parent);
        expect(sourceDao.getAll()).andReturn(sources);
        replayMocks();

        Map<String, Object> refdata = controller.referenceData(request, command, null);
        assertSame(expectedActivity, refdata.get("selectedActivity"));
        verifyMocks();
    }


    public void testReferenceDataEmptyIfNoNewActivity() throws Exception {
        expect(templateService.findStudy(period)).andReturn(parent);
        expect(sourceDao.getAll()).andReturn(sources);        
        request.removeParameter("selectedActivity");
        replayMocks();

        assertNotContains(controller.referenceData(request, command, null).keySet(), "selectedActivity");
        verifyMocks();
    }

    public static PlannedActivity eqPlannedActivity(PlannedActivity event) {
        EasyMock.reportMatcher(new PlannedActivityMatcher(event));
        return null;
    }

    private static class PlannedActivityMatcher implements IArgumentMatcher {
        private PlannedActivity expectedPlannedActivity;

        public PlannedActivityMatcher(PlannedActivity expectedPlannedActivity) {
            this.expectedPlannedActivity = expectedPlannedActivity;
        }

        public boolean matches(Object object) {
            if(!(object instanceof PlannedActivity)) {
                return false;
            }
            //we don't care if the events are not the same for this test
            return true;
        }

        public void appendTo(StringBuffer sb) {
            sb.append("PlannedActivity activity=").append(expectedPlannedActivity.getActivity());
        }
    }
}
