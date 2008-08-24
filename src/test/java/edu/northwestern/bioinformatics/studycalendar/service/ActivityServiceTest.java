package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.EasyMock.expect;

import java.util.Arrays;

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

}
