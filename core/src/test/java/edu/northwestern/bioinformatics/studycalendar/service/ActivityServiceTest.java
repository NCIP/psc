package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import static org.easymock.EasyMock.expect;

import java.util.Arrays;
import java.util.List;

public class ActivityServiceTest extends StudyCalendarTestCase {
    private ActivityService service;
    private ActivityDao activityDao;
    private PlannedActivityDao plannedActivityDao;
    private Activity activity0;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activityDao = registerDaoMockFor(ActivityDao.class);
        plannedActivityDao = registerDaoMockFor(PlannedActivityDao.class);
        service = new ActivityService();
        service.setActivityDao(activityDao);
        service.setPlannedActivityDao(plannedActivityDao);
        activity0 = setId(20, createActivity("Bone Scan"));
    }

    public void testDeleteActivityWhenUnused() {
        expectActivityNotUsedByPlannedCalendar(activity0);
        activityDao.delete(activity0);

        replayMocks();
        Boolean result = service.deleteActivity(activity0);
        verifyMocks();

        assertTrue("Activity should have been deleted", result);
    }

    public void testDeleteActivityWhenUsed() {
        expectActivityUsedByPlannedCalendar(activity0);

        replayMocks();
        Boolean result = service.deleteActivity(activity0);
        verifyMocks();

        assertFalse("Activity should not have been deleted", result);
    }

    private void expectActivityUsedByPlannedCalendar(Activity expectedActivity) {
        expect(plannedActivityDao.getPlannedActivitiesForActivity(expectedActivity.getId())).
            andReturn(Arrays.asList(new PlannedActivity()));
    }

    private void expectActivityNotUsedByPlannedCalendar(Activity activity) {
        expect(plannedActivityDao.getPlannedActivitiesForActivity(activity.getId())).andReturn(null);
    }

    public void testGetFilteredSources() throws Exception {
        Activity a = createActivity("A");
        Activity b = createActivity("B");
        Source other = createSource("Another");
        ActivityType activityType1 = createActivityType("LAB_TEST");
        ActivityType activityType2 = createActivityType("OTHER");
        Activity c = createActivity("C", "C", other, activityType1);
        // an activity that's in one of the matched sources, but not in the search results
        createActivity("Mismatch", "M", other, activityType2);
        assertEquals("Test setup failure", 2, other.getActivities().size());
        List<Activity> activities = Arrays.asList(b, c, a); // no particular order
        expect(activityDao.getActivitiesBySearchText("search", null, null)).andReturn(activities);

        replayMocks();
        List<Source> actual = service.getFilteredSources("search", null, null);
        verifyMocks();

        assertEquals("Wrong number of sources: " + actual, 2, actual.size());
        assertTransientSource("Problem with first source", actual.get(0), "Another", c);
        assertTransientSource("Problem with second source", actual.get(1), Fixtures.DEFAULT_ACTIVITY_SOURCE.getName(), a, b);
    }

    public void testGetFilteredSourcesIgnoresSourcelessActivities() throws Exception {
        Activity a = createActivity("A");
        a.setSource(null);
        expect(activityDao.getActivitiesBySearchText("search", null, null)).andReturn(Arrays.asList(a));

        replayMocks();
        List<Source> actual = service.getFilteredSources("search", null, null);
        verifyMocks();

        assertEquals("Wrong number of sources: " + actual, 0, actual.size());
    }
    
    public void testGetFilteredSourcesObeysTypeAndSource() throws Exception {
        Activity a = createActivity("A");

        ActivityType expectedType = createActivityType("DISEASE_MEASURE");
        Source expectedSource = Fixtures.DEFAULT_ACTIVITY_SOURCE;
        expect(activityDao.getActivitiesBySearchText(
            "search", expectedType, expectedSource)).andReturn(Arrays.asList(a));

        replayMocks();
        service.getFilteredSources("search", expectedType, expectedSource);
        verifyMocks();
    }

    private void assertTransientSource(String message, Source actual, String expectedName, Activity... expectedActivities) {
        assertEquals(message + " wrong name", expectedName, actual.getName());
        assertTrue(message + " not transient", actual.isMemoryOnly());
        for (int i = 0; i < expectedActivities.length; i++) {
            assertEqualsAndNotSame(message + " activity mismatch at " + i,
                expectedActivities[i], actual.getActivities().get(i));
            assertTrue(message + " activity at " + i + " not transient",
                actual.getActivities().get(i).isMemoryOnly());
        }
    }
}
