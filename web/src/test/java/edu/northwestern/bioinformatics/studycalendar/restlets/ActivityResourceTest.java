/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.service.ActivityService;
import org.restlet.data.Method;
import org.restlet.data.Status;

import java.util.ArrayList;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Saurabh Agrarwal
 */
public class ActivityResourceTest extends AuthorizedResourceTestCase<ActivityResource> {
    public static final String SOURCE_NAME = "House of Activities";
    public static final String SOURCE_NAME_ENCODED = "House%20of%20Activities";
    public static final String ACTIVITY_NAME = "Activities";
    public static final String ACTIVITY_TYPE_NAME = "Type";

    private ActivityDao activityDao;
    private Activity activity;
    private PlannedActivityDao plannedActivityDao;
    private SourceDao sourceDao;
    private Source source;
    private ActivityService activityService;
    private ActivityTypeDao activityTypeDao;
    private ActivityType activityType;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        activityDao = registerDaoMockFor(ActivityDao.class);
        plannedActivityDao = registerDaoMockFor(PlannedActivityDao.class);
        request.getAttributes().put(UriTemplateParameters.ACTIVITY_SOURCE_NAME.attributeName(), SOURCE_NAME_ENCODED);
        request.getAttributes().put(UriTemplateParameters.ACTIVITY_CODE.attributeName(), ACTIVITY_NAME);
        sourceDao = registerDaoMockFor(SourceDao.class);
        activityService = registerMockFor(ActivityService.class);
        activity = Fixtures.createNamedInstance(ACTIVITY_NAME, Activity.class);
        source = Fixtures.createNamedInstance(SOURCE_NAME, Source.class);
        activityType = Fixtures.createActivityType(ACTIVITY_TYPE_NAME);
        activityTypeDao = registerDaoMockFor(ActivityTypeDao.class);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected ActivityResource createAuthorizedResource() {
        ActivityResource resource = new ActivityResource();
        resource.setActivityDao(activityDao);
        resource.setActivityService(activityService);
        resource.setXmlSerializer(xmlSerializer);
        resource.setPlannedActivityDao(plannedActivityDao);
        resource.setSourceDao(sourceDao);
        resource.setActivityTypeDao(activityTypeDao);
        return resource;
    }

    public void testGetAndPutAndDeleteAllowed() throws Exception {
        assertAllowedMethods("PUT", "GET", "DELETE");
    }

    public void testGetWithAuthorizedRoles() {
        assertRolesAllowedForMethod(Method.GET,
            STUDY_CALENDAR_TEMPLATE_BUILDER,
            BUSINESS_ADMINISTRATOR,
            DATA_READER);
    }

    public void testPutWithAuthorizedRoles() {
        assertRolesAllowedForMethod(Method.PUT,
            BUSINESS_ADMINISTRATOR);
    }

    public void testDeleteWithAuthorizedRoles() {
        assertRolesAllowedForMethod(Method.DELETE,
            BUSINESS_ADMINISTRATOR);
    }

    public void testGetXmlForExistingActivity() throws Exception {
        expectExistentSource(source);
        expectFoundActivity(activity);
        expectObjectXmlized(activity);

        doGet();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertResponseIsCreatedXml();
    }

    public void testGetXmlForNonExistentActivityIs404() throws Exception {
        expectExistentSource(source);
        expectFoundActivity(null);

        doGet();

        assertEquals("Result not 'not found'", 404, response.getStatus().getCode());
    }

    public void testPutActivityForNonExistentSource() throws Exception {
        expect(sourceDao.getByName(SOURCE_NAME)).andReturn(null);
        expectReadXmlFromRequestAs(activity);

        doPut();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    public void testPutExistingActivity() throws Exception {
        activity.setId(1);
        activity.setType(activityType);
        Activity newActivity = new Activity();
        newActivity.setType(activityType);
        expectExistentSource(source);
        expectExistentType(activityType);
        expectFoundActivity(activity);
        expectReadXmlFromRequestAs(newActivity);
        expectObjectXmlized(newActivity);
        expect(activityDao.getById(1)).andReturn(activity);
        activityService.saveActivity(activity);
        doPut();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertResponseIsCreatedXml();
    }

    public void testDeleteExistingActivityWhichIsNotusedAnyWhere() throws Exception {
        expectExistentSource(source);
        expectFoundActivity(activity);
        expectActivityUsedByPlannedCalendar(activity, false);
        activityDao.delete(activity);
        doDelete();

        assertEquals("Result not success", 200, response.getStatus().getCode());
//        assertResponseIsCreatedXml();
    }

    public void testDeleteExistingActivityWhichIsused() throws Exception {
        expectExistentSource(source);
        expectFoundActivity(activity);
        expectActivityUsedByPlannedCalendar(activity, true);
        doDelete();

        assertEquals("Result is success", 400, response.getStatus().getCode());
//        assertResponseIsCreatedXml();
    }

    public void testPutNewXml() throws Exception {
        expectExistentSource(source);
        expectFoundActivity(null);
        expectObjectXmlized(activity);
        expectReadXmlFromRequestAs(activity);

        activityService.saveActivity(activity);
        doPut();

        assertResponseStatus(Status.SUCCESS_CREATED);
        assertResponseIsCreatedXml();
    }

    private void expectFoundActivity(Activity expectedActivity) {
        expect(activityDao.getByCodeAndSourceName(ACTIVITY_NAME, SOURCE_NAME)).andReturn(expectedActivity);
    }

    private void expectExistentSource(Source expectedSource) {
        expect(sourceDao.getByName(SOURCE_NAME)).andReturn(expectedSource);
    }

    private void expectExistentType(ActivityType expectedType) {
        expect(activityTypeDao.getByName(ACTIVITY_TYPE_NAME)).andReturn(expectedType);
    }
    private void expectActivityUsedByPlannedCalendar(Activity expectedActivity, boolean isExcepted) {
        if (isExcepted) {
            List<PlannedActivity> plannedActivities = new ArrayList<PlannedActivity>();
            plannedActivities.add(new PlannedActivity());
            expect(plannedActivityDao.getPlannedActivitiesForActivity(expectedActivity.getId())).andReturn(plannedActivities);
        } else {
            expect(plannedActivityDao.getPlannedActivitiesForActivity(expectedActivity.getId())).andReturn(null);

        }
    }

}
