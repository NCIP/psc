/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import org.easymock.classextension.EasyMock;

import java.util.Arrays;
import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class ChangePlannedActivitySimplePropertyMutatorTest extends StudyCalendarTestCase {
    private ChangePlannedActivitySimplePropertyMutator mutator;
    private PlannedActivity plannedActivity;
    private ScheduledCalendar scheduledCalendar;

    private ScheduledActivityDao scheduledActivityDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        plannedActivity = Fixtures.createPlannedActivity("Elph", 4);
        scheduledCalendar = new ScheduledCalendar();

        // Needed for side effects
        PropertyChange change = PropertyChange.create("details", "D", "Dprime");
        Delta.createDeltaFor(plannedActivity, change);

        scheduledActivityDao = registerDaoMockFor(ScheduledActivityDao.class);

        mutator = new ChangePlannedActivitySimplePropertyMutator(change, scheduledActivityDao);
    }

    public void testApplyDetails() throws Exception {
        ScheduledActivity expectedSE = Fixtures.createScheduledActivity("Elph", 2007, Calendar.MARCH, 4);
        expectedSE.setDetails("D");
        EasyMock.expect(scheduledActivityDao.getEventsFromPlannedActivity(plannedActivity, scheduledCalendar))
            .andReturn(Arrays.asList(expectedSE));

        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
        assertEquals("Dprime", expectedSE.getDetails());
    }

    public void testApplyToOccurredDetails() throws Exception {
        ScheduledActivity expectedSE = Fixtures.createScheduledActivity("Elph", 2007, Calendar.MARCH, 4, ScheduledActivityMode.OCCURRED.createStateInstance());
        expectedSE.setDetails("D");
        EasyMock.expect(scheduledActivityDao.getEventsFromPlannedActivity(plannedActivity, scheduledCalendar))
            .andReturn(Arrays.asList(expectedSE));

        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
        assertEquals("D", expectedSE.getDetails());
    }
}
