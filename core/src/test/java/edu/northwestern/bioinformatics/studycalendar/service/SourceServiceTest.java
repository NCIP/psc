package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import static org.easymock.EasyMock.expect;

/**
 * @author Saurabh Agrawal
 */
public class SourceServiceTest extends StudyCalendarTestCase {
    private static final String TARGET_SOURCE = "targetSource";
    private static final String SOURCE = "source";

    private SourceService sourceService;
    private ActivityService activityService;
    private SourceDao sourceDao;

    private Source targetSource;
    private Source source;

    private Activity activity, anotherActivity, activityToUpdate, activityToDelete;

    protected void setUp() throws Exception {
        super.setUp();

        sourceDao = registerDaoMockFor(SourceDao.class);

        activityService = registerMockFor(ActivityService.class);
        sourceService = new SourceService();
        sourceService.setActivityService(activityService);

        sourceService.setSourceDao(sourceDao);
        source = Fixtures.createSource(SOURCE);
        targetSource = Fixtures.createSource(TARGET_SOURCE);

        activity = Fixtures.createActivity("activity1", "code", null, Fixtures.createActivityType("LAB_TEST"));

        anotherActivity = Fixtures.createActivity("anotherActivity", "code2", null, Fixtures.createActivityType("LAB_TEST"));
        activityToUpdate = Fixtures.createActivity("activityToUpdate", "code2", null, Fixtures.createActivityType("LAB_TEST"));
        activityToDelete = Fixtures.createActivity("activityToDelete", "code3", null, Fixtures.createActivityType("LAB_TEST"));
    }

    public void testUpdateSourceWhenSourceHasNoActivity() {
        sourceDao.save(source);
        replayMocks();

        sourceService.updateSource(source, targetSource);

        assertEquals(SOURCE, targetSource.getName());
        verifyMocks();
    }

    public void testUpdateSourceForNewActivity() {
        source.addActivity(activity);
        source.addActivity(anotherActivity);
        sourceDao.save(source);
        replayMocks();

        sourceService.updateSource(source, targetSource);

        assertEquals(SOURCE, targetSource.getName());
        verifyMocks();
        assertEquals(2, targetSource.getActivities().size());
        assertTrue(targetSource.getActivities().contains(activity));
        assertTrue(targetSource.getActivities().contains(anotherActivity));
    }

    public void testUpdateSourceForUpdateAndDeleteActivity() {
        source.addActivity(activity);
        source.addActivity(anotherActivity);

        targetSource.addActivity(activityToUpdate);
        targetSource.addActivity(activityToDelete);

        expect(activityService.deleteActivity(activityToDelete)).andReturn(true);
        sourceDao.save(source);
        replayMocks();

        sourceService.updateSource(source, targetSource);
        verifyMocks();
        assertEquals(SOURCE, targetSource.getName());
        assertEquals(2, targetSource.getActivities().size());
        assertTrue(targetSource.getActivities().contains(activity));
        assertTrue(targetSource.getActivities().contains(activityToUpdate));
        assertEquals(anotherActivity, activityToUpdate);
    }
}
