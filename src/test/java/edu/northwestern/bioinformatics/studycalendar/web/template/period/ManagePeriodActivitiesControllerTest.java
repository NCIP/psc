package edu.northwestern.bioinformatics.studycalendar.web.template.period;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import static org.easymock.classextension.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;

import static java.util.Arrays.asList;
import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 * @author Jaron Sampson
 */
public class ManagePeriodActivitiesControllerTest extends ControllerTestCase {
    private ManagePeriodActivitiesController controller = new ManagePeriodActivitiesController();
    private PeriodDao periodDao;
    private ActivityDao activityDao;
    private Period period, revisedPeriod;
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
        deltaService = registerMockFor(DeltaService.class);
        templateService = registerMockFor(TemplateService.class);

        controller.setPeriodDao(periodDao);
        controller.setSourceDao(sourceDao);
        controller.setActivityDao(activityDao);
        controller.setDeltaService(deltaService);
        controller.setControllerTools(controllerTools);
        controller.setTemplateService(templateService);

        request.addParameter("period", "15");

    }

    private ModelAndView doHandle() throws Exception {
        expect(periodDao.getById(15)).andReturn(period);
        expect(sourceDao.getAll()).andReturn(sources);
        expect(deltaService.revise(period)).andReturn(revisedPeriod);
        expect(templateService.findStudy(revisedPeriod)).andReturn(parent);
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
        return mv;
    }

    public void testView() throws Exception {
        // temporary name
        assertEquals("divsManagePeriod", doHandle().getViewName());
    }

    public void testModelIncludesPeriod() throws Exception {
        assertEquals(revisedPeriod, doHandle().getModel().get("period"));
    }

    public void testModelIncludesGrid() throws Exception {
        Map model = doHandle().getModel();
        assertNotNull("Missing grid", model.get("grid"));
        assertTrue(model.get("grid") instanceof PeriodActivitiesGrid);
        PeriodActivitiesGrid grid = (PeriodActivitiesGrid) model.get("grid");
        assertEquals(revisedPeriod, grid.getPeriod());
        assertEquals(8, grid.getColumnCount());
    }

    public void testModelIncludesStudy() throws Exception {
        assertEquals(period.getStudySegment().getEpoch().getPlannedCalendar().getStudy(),
            doHandle().getModel().get("study"));
    }

    public void testModelIncludesActivityTypes() throws Exception {
        assertEquals(ActivityType.values(), doHandle().getModel().get("activityTypes"));
    }

    public void testModelIncludesActivitySources() throws Exception {
        assertEquals(sources, doHandle().getModel().get("activitySources"));
    }

    public void testModelDataIncludesNewActivityIfParamPresent() throws Exception {
        request.addParameter("selectedActivity", "43");
        Activity expectedActivity = setId(43, createActivity("Answer + 1"));
        expect(activityDao.getById(43)).andReturn(expectedActivity);

        assertSame(expectedActivity, doHandle().getModel().get("selectedActivity"));
    }

    @SuppressWarnings({ "unchecked" })
    public void testReferenceDataEmptyIfNoNewActivity() throws Exception {
        request.removeParameter("selectedActivity");
        assertNotContains(doHandle().getModel().keySet(), "selectedActivity");
    }
}