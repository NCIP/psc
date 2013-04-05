/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template.period;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRoleUse;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static java.util.Arrays.*;
import static org.easymock.classextension.EasyMock.*;

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
    private ActivityTypeDao activityTypeDao;
    private List<Source> sources;
    private StudySegment studySegment;
    private List<ActivityType> activityTypes = new ArrayList<ActivityType>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        period = createPeriod("7th", 10, 8, 4);
        parent = createBasicTemplate();
        parent.setDevelopmentAmendment(new Amendment("dev"));
        studySegment = parent.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);
        studySegment.addPeriod(period);
        Fixtures.assignIds(parent);

        sources = asList(createNamedInstance("Source A", Source.class));

        revisedPeriod = (Period) period.transientClone();

        periodDao = registerDaoMockFor(PeriodDao.class);
        sourceDao = registerDaoMockFor(SourceDao.class);
        activityDao = registerDaoMockFor(ActivityDao.class);
        deltaService = registerMockFor(DeltaService.class);
        templateService = registerMockFor(TemplateService.class);
        activityTypeDao = registerMockFor(ActivityTypeDao.class);

        controller.setPeriodDao(periodDao);
        controller.setSourceDao(sourceDao);
        controller.setActivityDao(activityDao);
        controller.setDeltaService(deltaService);
        controller.setControllerTools(controllerTools);
        controller.setTemplateService(templateService);
        controller.setActivityTypeDao(activityTypeDao);
        controller.setApplicationSecurityManager(applicationSecurityManager);

        ActivityType a1 = Fixtures.createActivityType("LAB_TEST");
        activityTypes.add(a1);

        request.addParameter("period", "15");
        SecurityContextHolderTestHelper.
            setUserAndReturnMembership("jimbo", PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER).forAllSites().forAllStudies();
    }

    public void testAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations, PscRoleUse.TEMPLATE_MANAGEMENT.roles());
    }    

    private ModelAndView doHandle() throws Exception {
        expect(periodDao.getById(15)).andReturn(period);
        expect(sourceDao.getAll()).andReturn(sources);
        expect(deltaService.revise(period)).andReturn(revisedPeriod);
        expect(templateService.findStudy(revisedPeriod)).andReturn(parent);
        expect(templateService.findParent(revisedPeriod)).andReturn(studySegment);
        expect(activityTypeDao.getAll()).andReturn(activityTypes).anyTimes();
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

    public void testGridIncludesAllActivities() throws Exception {
        Activity a0 = createActivity("A0");
        Activity a1 = createActivity("A1");
        Activity a2 = createActivity("A2");
        Activity a3 = createActivity("A3");
        revisedPeriod.addPlannedActivity(createPlannedActivity(a1, 3));
        Period p1 = createPeriod(1, 7, 1); p1.addPlannedActivity(createPlannedActivity(a1, 2));
        Period p2 = createPeriod(1, 7, 1); p2.addPlannedActivity(createPlannedActivity(a2, 1));
        Period p3 = createPeriod(1, 7, 1); p3.addPlannedActivity(createPlannedActivity(a3, 5));
        p2.addPlannedActivity(createPlannedActivity(a0, 4));
        parent.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0).addPeriod(p1);
        parent.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(1).addPeriod(p2);
        parent.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0).addPeriod(p3);

        PeriodActivitiesGrid grid = (PeriodActivitiesGrid) doHandle().getModel().get("grid");
        Collection<PeriodActivitiesGridRow> rows = grid.getRowGroups().get(Fixtures.DEFAULT_ACTIVITY_TYPE);
        assertEquals("Should be one row for each activity", 4, rows.size());
        Iterator<PeriodActivitiesGridRow> it = rows.iterator();
        assertGridRow(a0, false, it.next());
        assertGridRow(a1, true, it.next());
        assertGridRow(a2, false, it.next());
        assertGridRow(a3, false, it.next());
    }
    
    public void testGridUsesCycleLengthIfAvailable() throws Exception {
        studySegment.setCycleLength(18);

        PeriodActivitiesGrid grid = (PeriodActivitiesGrid) doHandle().getModel().get("grid");
        assertEquals((Integer) 18, grid.getCycleLength());
    }

    private void assertGridRow(Activity expectedActivity, boolean expectUsed, PeriodActivitiesGridRow actual) {
        assertEquals("Wrong activity", expectedActivity, actual.getActivity());
        assertEquals("Wrong value for isUsed", expectUsed, actual.isUsed());
    }

    public void testModelIncludesStudy() throws Exception {
        assertEquals(period.getStudySegment().getEpoch().getPlannedCalendar().getStudy(),
            doHandle().getModel().get("study"));
    }

    public void testModelIncludesActivityTypes() throws Exception {
        assertEquals(activityTypes, doHandle().getModel().get("activityTypes"));
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

    public void testCanEditIfTemplateBuilder() throws Exception {
        assertTrue((Boolean) doHandle().getModel().get("canEdit"));
    }

    public void testCannotEditIfNotTemplateBuilder() throws Exception {
        SecurityContextHolderTestHelper.
            setUserAndReturnMembership("carla", PscRole.STUDY_QA_MANAGER).forAllSites();
        assertFalse((Boolean) doHandle().getModel().get("canEdit"));
    }
}