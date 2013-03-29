/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
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

    private ActivityDao activityDao;
    private Activity sc;
    private Activity scprime;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        plannedActivity = createPlannedActivity(sc, 4);
        scheduledCalendar = new ScheduledCalendar();

        scheduledCalendar.addStudySegment(new ScheduledStudySegment());
        // For side effects
        PropertyChange change = PropertyChange.create("activity", "S|C", "S|Cprime");
        Delta.createDeltaFor(plannedActivity, change);

        sc = createActivity("C");
        scprime = createActivity("Cprime");
        activityDao = registerDaoMockFor(ActivityDao.class);

        mutator = new ChangePlannedActivityActivityMutator(change, activityDao);
    }

    public void testApplyActivityToPlannedActivity() throws Exception {
        expect(activityDao.getByUniqueKey("S|Cprime")).andReturn(scprime);

        replayMocks();
        mutator.apply(plannedActivity);
        verifyMocks();
        assertEquals("Wrong activity", scprime, plannedActivity.getActivity());
    }

    public void testApplyActivity() throws Exception {
        ScheduledActivity expectedSE = createScheduledActivity(plannedActivity, 2007, Calendar.MARCH, 4);
        expectedSE.setActivity(sc);
        addEvents(scheduledCalendar.getScheduledStudySegments().get(0), expectedSE);
        expect(activityDao.getByUniqueKey("S|Cprime")).andReturn(scprime);

        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
        assertEquals(scprime, expectedSE.getActivity());
    }

    public void testApplyToOccurredActivity() throws Exception {
        ScheduledActivity expectedSE = createScheduledActivity(plannedActivity, 2007, Calendar.MARCH, 4, ScheduledActivityMode.OCCURRED.createStateInstance());
        expectedSE.setActivity(sc);
        addEvents(scheduledCalendar.getScheduledStudySegments().get(0), expectedSE);

        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
        assertEquals(sc, expectedSE.getActivity());
    }
}