package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.service.ParticipantService;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public abstract class PeriodMutatorTestCase<C extends Change> extends StudyCalendarTestCase {
    protected static final int PERIOD_0_REPS = 3;
    protected static final int PERIOD_1_REPS = 2;
    protected static final Date ARM_START_DATE = DateTools.createDate(2010, Calendar.MARCH, 1);
    protected static final String REVISION_DISPLAY_NAME = "02/04/1909 (Oops)";

    protected Amendment amendment;
    private Delta<?> delta;
    protected C change;

    protected Arm arm;
    /** period0 is the period to which the delta applies */
    protected Period period0;
    /** period1 is the period to which the delta applies */
    protected Period period1;
    protected PlannedEvent p0e0, p0e1, p1e0, p1e1;

    protected ScheduledCalendar scheduledCalendar;
    protected ScheduledArm scheduledArm;
    /** Indexes are [period][event][repetition] */
    private ScheduledEvent[][][] scheduledEvents;

    private Mutator mutator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        arm = Epoch.create("E1", "A", "B").getArms().get(0);
        period0 = Fixtures.createPeriod("P0", 1, 9, PERIOD_0_REPS);
        period0.addPlannedEvent(p0e0 = Fixtures.createPlannedEvent("P0.E0", 2));
        period0.addPlannedEvent(p0e1 = Fixtures.createPlannedEvent("P0.E1", 6));
        period1 = Fixtures.createPeriod("P1", 1, 11, PERIOD_1_REPS);
        period1.addPlannedEvent(p1e0 = Fixtures.createPlannedEvent("P1.E0", 1));
        period1.addPlannedEvent(p1e1 = Fixtures.createPlannedEvent("P1.E1", 4));

        change = createChange();
        delta = createDelta();
        amendment = Fixtures.createAmendments("Oops");
        amendment.setDate(DateTools.createDate(1909, Calendar.FEBRUARY, 4));
        amendment.addDelta(delta);
        scheduledCalendar = new ScheduledCalendar();
        scheduledArm = Fixtures.createScheduledArm(arm);
        scheduledArm.setStartDay(1);
        scheduledArm.setStartDate(ARM_START_DATE);
        scheduledCalendar.addArm(scheduledArm);

        ParticipantService participantService = new ParticipantService() {
            @Override
            protected ScheduledEvent createEmptyScheduledEventFor(PlannedEvent event) {
                return createUnschedulableMockEvent(event);
            }
        };
        participantService.schedulePeriod(period0, amendment, scheduledArm);
        participantService.schedulePeriod(period1, amendment, scheduledArm);
        scheduledEvents = new ScheduledEvent[2][2][3];
        for (ScheduledEvent event : scheduledArm.getEvents()) {
            int period, pe;
            if      (event.getPlannedEvent() == p0e0) { period = 0; pe = 0; }
            else if (event.getPlannedEvent() == p0e1) { period = 0; pe = 1; }
            else if (event.getPlannedEvent() == p1e0) { period = 1; pe = 0; }
            else if (event.getPlannedEvent() == p1e1) { period = 1; pe = 1; }
            else throw new Error("Test setup failure: not all planned events accounted for");

            scheduledEvents[period][pe][event.getRepetitionNumber()] = event;
        }
    }

    protected final ScheduledEvent getScheduledEventFixture(PlannedEvent plannedEvent, int repetition) {
        if      (plannedEvent == p0e0) { return getScheduledEventFixture(0, 0, repetition); }
        else if (plannedEvent == p0e1) { return getScheduledEventFixture(0, 1, repetition); }
        else if (plannedEvent == p1e0) { return getScheduledEventFixture(1, 0, repetition); }
        else if (plannedEvent == p1e1) { return getScheduledEventFixture(1, 1, repetition); }
        else throw new Error("Test setup failure: non-fixture PE: " + plannedEvent);
    }

    protected final ScheduledEvent getScheduledEventFixture(int period, int plannedEvent, int repetition) {
        return scheduledEvents[period][plannedEvent][repetition];
    }

    protected abstract C createChange();

    protected Delta<?> createDelta() {
        return Delta.createDeltaFor(period0, change);
    }

    protected abstract Mutator createMutator();

    protected Mutator getMutator() {
        if (mutator == null) mutator = createMutator();
        return mutator;
    }

    public void testApplicableToLiveSchedules() throws Exception {
        assertTrue(getMutator().appliesToExistingSchedules());
    }

    private ScheduledEvent createUnschedulableMockEvent(PlannedEvent event) {
        ScheduledEvent semimock;
        try {
            semimock = registerMockFor(ScheduledEvent.class,
            ScheduledEvent.class.getMethod("unscheduleIfOutstanding", String.class));
        } catch (NoSuchMethodException e) {
            throw new Error("This shouldn't happen", e);
        }
        semimock.setPlannedEvent(event);
        return semimock;
    }
}
