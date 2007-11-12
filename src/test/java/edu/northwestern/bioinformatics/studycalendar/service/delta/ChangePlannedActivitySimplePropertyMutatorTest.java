package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Occurred;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;

import java.util.Calendar;
import java.util.Arrays;

import org.easymock.classextension.EasyMock;

/**
 * @author Rhett Sutphin
 */
public class ChangePlannedActivitySimplePropertyMutatorTest extends StudyCalendarTestCase {
    private ChangePlannedActivitySimplePropertyMutator mutator;
    private PropertyChange change;
    private PlannedActivity plannedActivity;
    private Delta<?> delta;
    private ScheduledCalendar scheduledCalendar;

    private ScheduledActivityDao scheduledActivityDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        plannedActivity = Fixtures.createPlannedActivity("Elph", 4);

        change = PropertyChange.create("details", "D", "Dprime");
        delta = Delta.createDeltaFor(plannedActivity, change);

        scheduledActivityDao = registerDaoMockFor(ScheduledActivityDao.class);

        mutator = new ChangePlannedActivitySimplePropertyMutator(change, scheduledActivityDao);
    }

    public void testApplyDetails() throws Exception {
        ScheduledActivity expectedSE = Fixtures.createScheduledEvent("Elph", 2007, Calendar.MARCH, 4);
        expectedSE.setDetails("D");
        EasyMock.expect(scheduledActivityDao.getEventsFromPlannedActivity(plannedActivity, scheduledCalendar))
            .andReturn(Arrays.asList(expectedSE));

        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
        assertEquals("Dprime", expectedSE.getDetails());
    }

    public void testApplyToOccurredDetails() throws Exception {
        ScheduledActivity expectedSE = Fixtures.createScheduledEvent("Elph", 2007, Calendar.MARCH, 4, new Occurred());
        expectedSE.setDetails("D");
        EasyMock.expect(scheduledActivityDao.getEventsFromPlannedActivity(plannedActivity, scheduledCalendar))
            .andReturn(Arrays.asList(expectedSE));

        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
        assertEquals("D", expectedSE.getDetails());
    }
}
