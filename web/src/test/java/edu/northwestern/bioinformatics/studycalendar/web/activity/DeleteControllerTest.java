package edu.northwestern.bioinformatics.studycalendar.web.activity;

import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.service.ActivityService;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;

import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.BUSINESS_ADMINISTRATOR;
import static org.easymock.EasyMock.expect;

public class DeleteControllerTest extends ControllerTestCase {

    private ActivityDao activityDao;
    private PlannedActivityDao plannedActivityDao;
    private ActivityTypeDao activityTypeDao;
    private ActivityService activityService;
    private DeleteController controller;

    private Activity a0, a1, a2;
    private Source source;
    List<ActivityType> activityTypes = new ArrayList<ActivityType>();


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        controller = new DeleteController();
        activityDao = registerDaoMockFor(ActivityDao.class);
        plannedActivityDao = registerDaoMockFor(PlannedActivityDao.class);
        activityTypeDao = registerDaoMockFor(ActivityTypeDao.class);
        activityService = registerMockFor(ActivityService.class);

        controller.setActivityDao(activityDao);
        controller.setPlannedActivityDao(plannedActivityDao);
        controller.setActivityService(activityService);
        controller.setActivityTypeDao(activityTypeDao);


        source = setId(11, createNamedInstance("Test Source", Source.class));

        a0 = Fixtures.createActivity("Activity 0", "code0", source, edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createActivityType("INTERVENTION"));
        a0.setId(10);

        a1 = Fixtures.createActivity("Activity 1", "code1", source, Fixtures.createActivityType("LAB_TEST"));
        a1.setId(20);

        a2 = Fixtures.createActivity("Activity 2", "code2", source, Fixtures.createActivityType("DISEASE_MEASURE"));
        a2.setId(30);

        PlannedActivity pa = Fixtures.createPlannedActivity(a2.getName(), 2);
    }

    public void testAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations, BUSINESS_ADMINISTRATOR);
    }

    @SuppressWarnings({ "unchecked" })
    public void testModelWithNoErorMessage() throws Exception {
        request.setParameter("activityId", "10");
        Map<String, Object> actualModel;
        expect(activityDao.getById(a0.getId())).andReturn(a0).anyTimes();
        expect(activityService.deleteActivity(a0)).andReturn(true).anyTimes();
        expect(activityTypeDao.getAll()).andReturn(activityTypes).anyTimes();
        List<Activity> activityList = new ArrayList<Activity>();
        activityList.add(a1);
        expect(activityDao.getBySourceId(source.getId())).andReturn(activityList).anyTimes();
        List<PlannedActivity> plannedActivityList = new ArrayList<PlannedActivity>();
        expect(plannedActivityDao.getPlannedActivitiesForActivity(a1.getId())).andReturn(plannedActivityList).anyTimes();

        replayMocks();
        actualModel = controller.handleRequestInternal(request, response).getModel();
        verifyMocks();

        assertTrue("Missing model object", actualModel.containsKey("enableDeletes"));
        assertTrue("Missing model object", actualModel.containsKey("activitiesPerSource"));
        assertTrue("Missing model object", actualModel.containsKey("activityTypes"));

    }


    @SuppressWarnings({ "unchecked" })
    public void testModelWithErorMessage() throws Exception {
        request.setParameter("activityId", "30");
        Map<String, Object> actualModel;
        expect(activityDao.getById(a2.getId())).andReturn(a2).anyTimes();
        expect(activityService.deleteActivity(a2)).andReturn(false).anyTimes();
        expect(activityTypeDao.getAll()).andReturn(activityTypes).anyTimes();
        List<Activity> activityList = new ArrayList<Activity>();
        activityList.add(a1);
        expect(activityDao.getBySourceId(source.getId())).andReturn(activityList).anyTimes();
        List<PlannedActivity> plannedActivityList = new ArrayList<PlannedActivity>();
        expect(plannedActivityDao.getPlannedActivitiesForActivity(a1.getId())).andReturn(plannedActivityList).anyTimes();

        replayMocks();
        actualModel = controller.handleRequestInternal(request, response).getModel();
        verifyMocks();

        assertTrue("Missing model error object", actualModel.containsKey("error"));
        assertTrue("Missing model object", actualModel.containsKey("enableDeletes"));
        assertTrue("Missing model object", actualModel.containsKey("activitiesPerSource"));
        assertTrue("Missing model object", actualModel.containsKey("activityTypes"));
    }
}
