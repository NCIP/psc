package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityLabelDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static org.easymock.EasyMock.expect;


/**
 * @author Jalpa Patel
 */
public class AddPlannedActivityLabelMutatorTest extends StudyCalendarTestCase {

    private AddPlannedActivityLabelMutator mutator;

    private Amendment amendment;
    private Add add;
    private PlannedActivity plannedActivity;
    private PlannedActivityLabel paLabel;
    private ScheduledCalendar scheduledCalendar;

    private PlannedActivityLabelDao paLabelDao;
    private ScheduledActivityDao scheduledActivityDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        plannedActivity = setId(21, Fixtures.createPlannedActivity("pa", 8));
        paLabel = setId(51, Fixtures.createPlannedActivityLabel(plannedActivity,"TestLabel",5));
        add = Add.create(paLabel);
        amendment = createAmendments("amendment1");
        amendment.setDate(DateTools.createDate(1922, Calendar.APRIL, 5));
        amendment.addDelta(Delta.createDeltaFor(plannedActivity, add));

        scheduledCalendar = new ScheduledCalendar();
        plannedActivity.addPlannedActivityLabel(paLabel);
        paLabelDao = registerDaoMockFor(PlannedActivityLabelDao.class);
        scheduledActivityDao = registerDaoMockFor(ScheduledActivityDao.class);
        mutator = new AddPlannedActivityLabelMutator(
                add, paLabelDao,scheduledActivityDao);
    }

    public void testApplyToOneScheduledActivityWithOneLabel() throws Exception {
        ScheduledActivity expectedSA = createScheduledActivity(plannedActivity, 2007, Calendar.MARCH, 4);
        expect(scheduledActivityDao.getEventsFromPlannedActivity(plannedActivity, scheduledCalendar))
            .andReturn(Arrays.asList(expectedSA));
        expectedSA.addLabel("newLabel");
        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
        assertEquals("Should be Same",2,expectedSA.getLabels().size());
    }

    public void testApplyToOneScheduledActivity() throws Exception {
        ScheduledActivity expectedSA = Fixtures.createScheduledActivity("Activity1", 2007, Calendar.MARCH, 4);
        expectedSA.setRepetitionNumber(0);
        expect(scheduledActivityDao.getEventsFromPlannedActivity(plannedActivity, scheduledCalendar))
            .andReturn(Arrays.asList(expectedSA));
        expectedSA.addLabel("newLabel");
        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
        assertEquals("Should be Same",1,expectedSA.getLabels().size());
    }


    public void testApplyToManyScheduledActivity() throws Exception {
        ScheduledActivity sa1 = createScheduledActivity(plannedActivity, 2007, Calendar.MARCH, 4);
        ScheduledActivity sa2 = createScheduledActivity(plannedActivity, 2007, Calendar.MARCH, 5);
        List<ScheduledActivity> scheduledActivities = new ArrayList<ScheduledActivity>();
        scheduledActivities.add(sa1);
        scheduledActivities.add(sa2);
        expect(scheduledActivityDao.getEventsFromPlannedActivity(plannedActivity, scheduledCalendar))
                    .andReturn(scheduledActivities);
        sa1.addLabel("newLabel");
        sa2.addLabel("newLabel");
        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
        assertEquals("Should be Same",2,sa1.getLabels().size());
        assertEquals("Should be Same",2,sa2.getLabels().size());
    }

}
