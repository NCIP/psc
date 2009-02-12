package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.test.ServicedFixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import static edu.northwestern.bioinformatics.studycalendar.test.ServicedFixtures.createAmendments;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;
import static org.easymock.EasyMock.expect;

import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class ChangePlannedActivityDayMutatorTest extends StudyCalendarTestCase {
    private ChangePlannedActivityDayMutator mutator;
    private PlannedActivity plannedActivity;
    private ScheduledActivity se0, se1;
    private PropertyChange change;
    private ScheduledActivityDao scheduledActivityDao;
    private Amendment amendment;
    private ScheduleService scheduleService;
    private ScheduledCalendar scheduledCalendar;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        plannedActivity = ServicedFixtures.createPlannedActivity("Rolling", 2);
        se0 = createScheduledActivity();
        se1 = createScheduledActivity();
        scheduledCalendar = new ScheduledCalendar();

        change = PropertyChange.create("day", "2", "4");
        amendment = createAmendments("Hello");
        amendment.addDelta(Delta.createDeltaFor(plannedActivity, change));

        scheduleService = registerMockFor(ScheduleService.class);
        scheduledActivityDao = registerDaoMockFor(ScheduledActivityDao.class);
    }

    private ChangePlannedActivityDayMutator getMutator() {
        if (mutator == null) {
            mutator = new ChangePlannedActivityDayMutator(change, scheduledActivityDao, scheduleService);
        }
        return mutator;
    }

    private ScheduledActivity createScheduledActivity() {
        ScheduledActivity se = new ScheduledActivity();
        se.changeState(new Scheduled());
        se.setPlannedActivity(plannedActivity);
        return se;
    }

    public void testShiftForward() throws Exception {
        expect(scheduledActivityDao.getEventsFromPlannedActivity(plannedActivity, scheduledCalendar))
            .andReturn(Arrays.asList(se0, se1));
        scheduleService.reviseDate(se0, 2, amendment);
        scheduleService.reviseDate(se1, 2, amendment);

        replayMocks();
        getMutator().apply(scheduledCalendar);
        verifyMocks();
    }

    public void testShiftBack() throws Exception {
        change.setNewValue("1");

        expect(scheduledActivityDao.getEventsFromPlannedActivity(plannedActivity, scheduledCalendar))
            .andReturn(Arrays.asList(se0, se1));
        scheduleService.reviseDate(se0, -1, amendment);
        scheduleService.reviseDate(se1, -1, amendment);

        replayMocks();
        getMutator().apply(scheduledCalendar);
        verifyMocks();
    }
}

