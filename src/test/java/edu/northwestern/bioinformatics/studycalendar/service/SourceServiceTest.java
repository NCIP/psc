package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Saurabh Agrawal
 */
public class SourceServiceTest extends StudyCalendarTestCase {
    private static final String TARGET_SOURCE = "targetSource";
    private static final String SOURCE = "source";

    private SourceService sourceService;

    private Source targetSource;
    private Source source;

    private Activity activity, anotherActivity, activityToUpdate, activityToDelete;

    protected void setUp() throws Exception {
        super.setUp();

        SourceDao sourceDao = registerDaoMockFor(SourceDao.class);
        PlannedActivityDao plannedActivityDao = registerDaoMockFor(PlannedActivityDao.class);

        sourceService = new SourceService();

        sourceService.setSourceDao(sourceDao);
        sourceService.setPlannedActivityDao(plannedActivityDao);
        source = Fixtures.createSource(SOURCE);
        targetSource = Fixtures.createSource(TARGET_SOURCE);

        activity = Fixtures.createActivity("activity1", "code", null, ActivityType.LAB_TEST);

        anotherActivity = Fixtures.createActivity("anotherActivity", "code2", null, ActivityType.LAB_TEST);
        activityToUpdate = Fixtures.createActivity("activityToUpdate", "code2", null, ActivityType.LAB_TEST);
        activityToDelete = Fixtures.createActivity("activityToDelete", "code3", null, ActivityType.LAB_TEST);
    }

    public void testUpdateSourceWhenSourceHasNoActivity() {
        sourceService.updateSource(source, targetSource);

        assertEquals(SOURCE, targetSource.getName());
    }

    public void testUpdateSourceForNewActivity() {
        source.addActivity(activity);
        source.addActivity(anotherActivity);
        sourceService.updateSource(source, targetSource);

        assertEquals(SOURCE, targetSource.getName());
        assertEquals(2, targetSource.getActivities().size());
        assertTrue(targetSource.getActivities().contains(activity));
        assertTrue(targetSource.getActivities().contains(anotherActivity));
    }

    public void testUpdateSourceForUpdateAndDeleteActivity() {
        source.addActivity(activity);
        source.addActivity(anotherActivity);

        targetSource.addActivity(activityToUpdate);
        targetSource.addActivity(activityToDelete);

        sourceService.updateSource(source, targetSource);

        assertEquals(SOURCE, targetSource.getName());
        assertEquals(2, targetSource.getActivities().size());
        assertTrue(targetSource.getActivities().contains(activity));
        assertTrue(targetSource.getActivities().contains(activityToUpdate));
        assertEquals(anotherActivity, activityToUpdate);
    }
}
