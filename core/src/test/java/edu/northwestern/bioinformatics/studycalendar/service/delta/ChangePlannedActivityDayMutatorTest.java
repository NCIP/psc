/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;
import static org.easymock.EasyMock.expect;

import java.util.Arrays;
import java.io.*;
/**
 * @author Rhett Sutphin
 */
public class ChangePlannedActivityDayMutatorTest extends StudyCalendarTestCase {
    private ChangePlannedActivityDayMutator mutator;
    private PlannedActivity plannedActivity;
    private ScheduledActivity se0, se1;
    private PropertyChange change;
    private Amendment amendment;
    private ScheduleService scheduleService;
    private ScheduledCalendar scheduledCalendar;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        plannedActivity = Fixtures.createPlannedActivity("Rolling", 2);
        se0 = createScheduledActivity();
        se1 = createScheduledActivity();
        scheduledCalendar = new ScheduledCalendar();
        scheduledCalendar.addStudySegment(new ScheduledStudySegment());
        addEvents(scheduledCalendar.getScheduledStudySegments().get(0), se0, se1);
        change = PropertyChange.create("day", "2", "4");
        amendment = createAmendments("Hello");
        amendment.addDelta(Delta.createDeltaFor(plannedActivity, change));

        scheduleService = registerMockFor(ScheduleService.class);
    }

    private ChangePlannedActivityDayMutator getMutator() {
        if (mutator == null) {
            mutator = new ChangePlannedActivityDayMutator(change, scheduleService);
        }
        return mutator;
    }

    private ScheduledActivity createScheduledActivity() {
        ScheduledActivity se = new ScheduledActivity();
        se.changeState(ScheduledActivityMode.SCHEDULED.createStateInstance());
        se.setPlannedActivity(plannedActivity);
        return se;
    }

    public void testShiftForward() throws Exception {
        scheduleService.reviseDate(se0, 2, amendment);
        scheduleService.reviseDate(se1, 2, amendment);

        replayMocks();
        getMutator().apply(scheduledCalendar);
        verifyMocks();
    }

    public void testShiftBack() throws Exception {
        change.setNewValue("1");
        scheduleService.reviseDate(se0, -1, amendment);
        scheduleService.reviseDate(se1, -1, amendment);

        replayMocks();
        getMutator().apply(scheduledCalendar);
        verifyMocks();
    }
}

