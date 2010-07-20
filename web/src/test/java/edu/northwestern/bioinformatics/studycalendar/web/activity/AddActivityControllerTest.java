package edu.northwestern.bioinformatics.studycalendar.web.activity;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.northwestern.bioinformatics.studycalendar.web.template.NewActivityCommand;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.BUSINESS_ADMINISTRATOR;
import static org.easymock.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AddActivityControllerTest extends ControllerTestCase {

    private ActivityDao activityDao;
    private PlannedActivityDao plannedActivityDao;
    private SourceDao sourceDao;
    private AddActivityController controller;
    private Source source;
    private NewActivityCommand command;
    private ActivityType activityType;
    private ActivityTypeDao activityTypeDao;
    private List<ActivityType> activityTypes = new ArrayList<ActivityType>();

    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        sourceDao = registerDaoMockFor(SourceDao.class);
        plannedActivityDao = registerDaoMockFor(PlannedActivityDao.class);
        activityDao = registerDaoMockFor(ActivityDao.class);
        activityTypeDao = registerDaoMockFor(ActivityTypeDao.class);

        controller = new AddActivityController() {
            @Override
            public Object getCommand(HttpServletRequest request) {
                return command;
            }
        };
        controller.setControllerTools(controllerTools);

        controller.setSourceDao(sourceDao);
        controller.setActivityDao(activityDao);
        controller.setPlannedActivityDao(plannedActivityDao);
        controller.setActivityTypeDao(activityTypeDao);
        command = new NewActivityCommand(activityDao);

        activityType = setId(2, Fixtures.createActivityType("DISEASE_MEASURE"));
        request.addParameter("activityType", "2");
        expect(activityTypeDao.getById(2)).andReturn(activityType).anyTimes();

        source = setId(11, createNamedInstance("Test Source", Source.class));
        request.addParameter("activitySource", "11");
        expect(sourceDao.getById(source.getId())).andReturn(source).anyTimes();

        List<Activity> activities = new ArrayList<Activity>();
        expect(activityDao.getBySourceId(source.getId())).andReturn(activities).anyTimes();
    }

    public void testAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations, BUSINESS_ADMINISTRATOR);
    }

    public void testBindActivitySource() throws Exception {
        doHandle();
        assertEquals(source, command.getActivitySource());
    }

    public void testBindActivityType() throws Exception {
        doHandle();
        assertEquals(activityType, command.getActivityType());
    }

    private ModelAndView doHandle() throws Exception {
        expect(activityTypeDao.getAll()).andReturn(activityTypes).anyTimes();
        ModelAndView actualModel;

        replayMocks();
        actualModel = controller.handleRequest(request, response);
        verifyMocks();

        return actualModel;
    }

    public void testModel() throws Exception {
        ModelAndView model = doHandle();
        assertTrue("Missing model object", model.getModel().containsKey("enableDeletes"));
        assertTrue("Missing model object", model.getModel().containsKey("activitiesPerSource"));
        assertTrue("Missing model object", model.getModel().containsKey("activityTypes"));
    }

    public void testCreatingNewActivity() throws Exception {
        ActivityType activityType1 = Fixtures.createActivityType("INTERVENTION");
        Activity a1 = Fixtures.createActivity("Activity1", "Code1", setId(12, createNamedInstance("Test Source 1", Source.class)), activityType1);
        command.setActivityCode(a1.getCode());
        command.setActivityName(a1.getName());
        command.setActivityType(a1.getType());
        command.setActivitySource(a1.getSource());
        command.setActivityDescription(a1.getDescription());
        Activity a2 = command.createActivity();
        assertEquals("Activities are not the same", a1.getName(), a2.getName());
    }
}
