package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Occurred;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.classextension.EasyMock.expect;

import java.util.Arrays;
import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class ChangePlannedActivityActivityMutatorTest extends StudyCalendarTestCase {
    private ChangePlannedActivityActivityMutator mutator;
    private PlannedActivity plannedActivity;
    private ScheduledCalendar scheduledCalendar;

    private ScheduledActivityDao scheduledActivityDao;
    private ActivityDao activityDao;
    private Activity sc;
    private Activity scprime;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        plannedActivity = Fixtures.createPlannedActivity("Elph", 4);
        scheduledCalendar = new ScheduledCalendar();

        // For side effects
        PropertyChange change = PropertyChange.create("activity", "S|C", "S|Cprime");
        Delta.createDeltaFor(plannedActivity, change);

        sc = Fixtures.createActivity("C");
        scprime = Fixtures.createActivity("Cprime");

        scheduledActivityDao = registerDaoMockFor(ScheduledActivityDao.class);
        activityDao = registerDaoMockFor(ActivityDao.class);

        mutator = new ChangePlannedActivityActivityMutator(change, scheduledActivityDao, activityDao);
    }

    public void testApplyActivity() throws Exception {
        ScheduledActivity expectedSE = Fixtures.createScheduledActivity("Elph", 2007, Calendar.MARCH, 4);
        expectedSE.setActivity(sc);
        expect(scheduledActivityDao.getEventsFromPlannedActivity(plannedActivity, scheduledCalendar))
            .andReturn(Arrays.asList(expectedSE));
        expect(activityDao.getByUniqueKey("S|Cprime")).andReturn(scprime);

        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
        assertEquals(scprime, expectedSE.getActivity());
    }

    public void testApplyToOccurredActivity() throws Exception {
        ScheduledActivity expectedSE = Fixtures.createScheduledActivity("Elph", 2007, Calendar.MARCH, 4, new Occurred());
        expectedSE.setActivity(sc);
        expect(scheduledActivityDao.getEventsFromPlannedActivity(plannedActivity, scheduledCalendar))
            .andReturn(Arrays.asList(expectedSE));

        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
        assertEquals(sc, expectedSE.getActivity());
    }
}