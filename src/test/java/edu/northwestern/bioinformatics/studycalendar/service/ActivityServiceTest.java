package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createActivity;

import java.util.List;
import java.util.ArrayList;

import static org.easymock.EasyMock.expect;

public class ActivityServiceTest extends StudyCalendarTestCase {
    private ActivityService service;
    private ActivityDao activityDao;
    private SourceDao sourceDao;
    private PlannedActivityDao plannedActivityDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activityDao = registerDaoMockFor(ActivityDao.class);
        sourceDao = registerDaoMockFor(SourceDao.class);
        plannedActivityDao = registerDaoMockFor(PlannedActivityDao.class);
        service = new ActivityService();
        service.setActivityDao(activityDao);
        service.setPlannedActivityDao(plannedActivityDao);
        service.setSourceDao(sourceDao);
    }

    public void testDeleteActivity() {
        Source existingSource = setId(0, createNamedInstance("ICD-9", Source.class));
        Activity activity0 = assignSource(createActivity("Bone Scan"), existingSource);
        activity0.setId(20);
        Activity activity1 = assignSource(createActivity("CTC Scan"), existingSource);
        activity1.setId(30);

        expectActivityUsedByPlannedCalendar(activity0, false);
        activityDao.delete(activity0);

        replayMocks();
        Boolean result  = service.deleteActivity(activity0);
        verifyMocks();

        assertEquals("Activity wasn't deleted", Boolean.TRUE, result);
    }

    private Activity assignSource(Activity activity, Source source) {
        activity.setSource(source);
        source.getActivities().add(activity);
        return activity;
    }

    private void expectActivityUsedByPlannedCalendar(Activity expectedActivity, boolean isExcepted) {
        if (isExcepted) {
            List<PlannedActivity> plannedActivities = new ArrayList<PlannedActivity>();
            plannedActivities.add(new PlannedActivity());
            expect(plannedActivityDao.getPlannedActivitiesForAcivity(expectedActivity.getId())).andReturn(plannedActivities);
        } else {
            expect(plannedActivityDao.getPlannedActivitiesForAcivity(expectedActivity.getId())).andReturn(null);

        }
    }

}
