package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.service.ParticipantService;

/**
 * @author Rhett Sutphin
 */
public class AddPeriodMutatorTest extends StudyCalendarTestCase {
    private static final int ARM_ID = 11;
    private static final int PERIOD_ID = 83;

    private AddPeriodMutator mutator;

    private Amendment amendment;
    private Add add;

    private Arm arm;
    private Period period;
    private ScheduledCalendar scheduledCalendar;

    private PeriodDao periodDao;
    private ParticipantService participantService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        arm = setId(ARM_ID, createNamedInstance("A1", Arm.class));
        period = setId(PERIOD_ID, createPeriod("P1", 4, 17, 8));
        add = Add.create(period);

        amendment = Fixtures.createAmendments("Oops");
        amendment.addDelta(Delta.createDeltaFor(arm, add));

        scheduledCalendar = new ScheduledCalendar();

        periodDao = registerDaoMockFor(PeriodDao.class);
        participantService = registerMockFor(ParticipantService.class); 

        mutator = new AddPeriodMutator(add, periodDao, participantService);
    }

    public void testAppliesToSchedules() throws Exception {
        assertTrue(mutator.appliesToExistingSchedules());
    }

    public void testApplyWhenNoRelevantScheduledArms() throws Exception {
        scheduledCalendar.addArm(createScheduledArm(createNamedInstance("Some other arm", Arm.class)));

        // expect nothing to happen

        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
    }
    
    public void testApplyToOneRelevantScheduledArm() throws Exception {
        scheduledCalendar.addArm(createScheduledArm(createNamedInstance("Some other arm", Arm.class)));
        scheduledCalendar.addArm(createScheduledArm(arm));

        participantService.schedulePeriod(period, amendment, scheduledCalendar.getScheduledArms().get(1));

        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
    }

    public void testApplyToMultipleRelevantScheduledArms() throws Exception {
        scheduledCalendar.addArm(createScheduledArm(arm));
        scheduledCalendar.addArm(createScheduledArm(createNamedInstance("Some other arm", Arm.class)));
        scheduledCalendar.addArm(createScheduledArm(arm));

        participantService.schedulePeriod(period, amendment, scheduledCalendar.getScheduledArms().get(0));
        participantService.schedulePeriod(period, amendment, scheduledCalendar.getScheduledArms().get(2));

        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
    }
}
