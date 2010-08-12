package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityLabelDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.*;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import java.util.Calendar;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import static org.easymock.EasyMock.expect;

/**
 * @author Jalpa Patel
 */
public class RemovePlannedActivityLabelMutatorTest extends StudyCalendarTestCase {
    private RemovePlannedActivityLabelMutator mutator;

    private Amendment amendment;
    private Remove remove;

    private PlannedActivity plannedActivity;
    private PlannedActivityLabel paLabel;
    private ScheduledCalendar scheduledCalendar;

    private PlannedActivityLabelDao paLabelDao;
    private ScheduledActivityDao scheduledActivityDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        plannedActivity = setId(21, edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createPlannedActivity("pa", 8));
        paLabel = setId(51, Fixtures.createPlannedActivityLabel(plannedActivity,"TestLabel",5));
        remove = Remove.create(paLabel);
        amendment = createAmendments("amendment1");
        amendment.setDate(DateTools.createDate(1922, Calendar.APRIL, 5));
        amendment.addDelta(Delta.createDeltaFor(plannedActivity,remove));

        scheduledCalendar = new ScheduledCalendar();
        plannedActivity.addPlannedActivityLabel(paLabel);
        paLabelDao = registerDaoMockFor(PlannedActivityLabelDao.class);
        scheduledActivityDao = registerDaoMockFor(ScheduledActivityDao.class);
        mutator = new RemovePlannedActivityLabelMutator(
                    remove, paLabelDao,scheduledActivityDao);
    }

    public void testRemoveLabelFromOneScheduledActivity() throws Exception {
        ScheduledActivity expectedSA = createScheduledActivity(plannedActivity, 2007, Calendar.MARCH, 4);
        expect(scheduledActivityDao.getEventsFromPlannedActivity(plannedActivity, scheduledCalendar))
                .andReturn(Arrays.asList(expectedSA));
        expectedSA.removeLabel(paLabel.getLabel());
        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
        assertEquals("Should be same",0, expectedSA.getLabels().size());
    }

    public void testRemoveLabelFromManyScheduledActivity() throws Exception {
        ScheduledActivity sa1 = createScheduledActivity(plannedActivity, 2007, Calendar.MARCH, 4);
        ScheduledActivity sa2 = createScheduledActivity(plannedActivity, 2007, Calendar.MARCH, 5);
        List<ScheduledActivity> scheduledActivities = new ArrayList<ScheduledActivity>();
        scheduledActivities.add(sa1);
        scheduledActivities.add(sa2);
        expect(scheduledActivityDao.getEventsFromPlannedActivity(plannedActivity, scheduledCalendar))
                        .andReturn(scheduledActivities);
        sa1.removeLabel(paLabel.getLabel());
        sa2.removeLabel(paLabel.getLabel());
        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
        assertEquals("Should be same",0,sa1.getLabels().size());
        assertEquals("Should be same",0,sa2.getLabels().size());
    }

}
