/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.activity;

import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setId;
import edu.northwestern.bioinformatics.studycalendar.service.ActivityTypeService;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.BUSINESS_ADMINISTRATOR;
import static org.easymock.EasyMock.expect;

/**
 * @author Nataliya Shurupova
 */
public class AddEditActivityTypeControllerTest extends ControllerTestCase {

    private ActivityTypeDao activityTypeDao;
    private ActivityDao activityDao;
    private ActivityTypeService activityTypeService;
    private AddEditActivityTypeController controller;
    private List<ActivityType> activityTypes = new ArrayList<ActivityType>();
    private List<Activity> activities = new ArrayList<Activity>();
    private ActivityType activityType;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activityTypeDao = registerDaoMockFor(ActivityTypeDao.class);
        activityDao = registerDaoMockFor(ActivityDao.class);
        activityTypeService = registerMockFor(ActivityTypeService.class);
        
        controller = new AddEditActivityTypeController();

        controller.setActivityDao(activityDao);
        controller.setActivityTypeDao(activityTypeDao);
        controller.setActivityTypeService(activityTypeService);


        activityType = setId(5, Fixtures.createActivityType("DISEASE_MEASURE"));
        activityTypes.add(activityType);

        Activity activity = Fixtures.createActivity("activity 1", activityType);
        activities.add(activity);
    }

    public void testAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations, BUSINESS_ADMINISTRATOR);
    }

    @SuppressWarnings({ "unchecked" })
    public void testModelForAdd() throws Exception {
        String newActivityTypeName = "New Activity Type";
        ActivityType newActivityType = Fixtures.createActivityType(newActivityTypeName);
        request.addParameter("action","add");
        request.addParameter("activityTypeName", newActivityType.getName());
        Map<String, Object> actualModel;

        expect(activityTypeDao.getByName(newActivityType.getName())).andReturn(null);
        activityTypeDao.save(newActivityType);
        activityTypes.add(newActivityType);

        expect(activityTypeDao.getAll()).andReturn(activityTypes).anyTimes();
        expect(activityDao.getAll()).andReturn(activities).anyTimes();

        replayMocks();
        actualModel = controller.handleRequestInternal(request, response).getModel();
        verifyMocks();

        assertTrue("Missing model object", actualModel.containsKey("activityTypes"));
        assertTrue("Missing model object", actualModel.containsKey("enableDeletes"));
        List<ActivityType> types = (List<ActivityType>) actualModel.get("activityTypes");
        assertEquals("Activity type wasn't added ", 2, types.size());
        assertTrue("Activity type name wasn't changed ", types.get(1).getName().equals(newActivityTypeName));
    }


    @SuppressWarnings({ "unchecked" })
    public void testModelForEdit() throws Exception {
        ActivityType editActivityType = setId(10, Fixtures.createActivityType("Edit Activity Type"));
        activityTypes.add(editActivityType);

        String editedActiivityTypeName = "EditedActivityTypeName";
        request.addParameter("action","save");
        request.addParameter("activityTypeName", editedActiivityTypeName);
        request.addParameter("activityTypeId", "10");
        Map<String, Object> actualModel;

        expect(activityTypeDao.getById(10)).andReturn(editActivityType);
        editActivityType.setName(editedActiivityTypeName);
        activityTypeDao.save(editActivityType);
        expect(activityTypeDao.getAll()).andReturn(activityTypes).anyTimes();
        expect(activityDao.getAll()).andReturn(activities).anyTimes();

        replayMocks();
        actualModel = controller.handleRequestInternal(request, response).getModel();
        verifyMocks();

        assertTrue("Missing model object", actualModel.containsKey("activityTypes"));
        assertTrue("Missing model object", actualModel.containsKey("enableDeletes"));
        List<ActivityType> types = (List<ActivityType>) actualModel.get("activityTypes");
        assertTrue("Activity type name wasn't changed ", types.get(1).getName().equals(editedActiivityTypeName));
    }


    @SuppressWarnings({ "unchecked" })
    public void testModelForDelete() throws Exception {
        String toBeDeletedActivityTypeName = "Delete Activity Type";
        ActivityType toBeDeletedActivityType = setId(10, Fixtures.createActivityType(toBeDeletedActivityTypeName));
        activityTypes.add(toBeDeletedActivityType);
        request.setParameter("action", "delete");
        request.setParameter("activityTypeName", toBeDeletedActivityTypeName);
        request.setParameter("activityTypeId", "10");

        Map<String, Object> actualModel;
        expect(activityTypeDao.getById(10)).andReturn(toBeDeletedActivityType);
        expect(activityTypeService.deleteActivityType(toBeDeletedActivityType)).andReturn(true);
        expect(activityTypeDao.getAll()).andReturn(activityTypes).anyTimes();
        expect(activityDao.getAll()).andReturn(activities).anyTimes();

        replayMocks();
        actualModel = controller.handleRequestInternal(request, response).getModel();
        verifyMocks();

        assertTrue("Missing model object", actualModel.containsKey("activityTypes"));
        assertTrue("Missing model object", actualModel.containsKey("enableDeletes"));

    }
}
