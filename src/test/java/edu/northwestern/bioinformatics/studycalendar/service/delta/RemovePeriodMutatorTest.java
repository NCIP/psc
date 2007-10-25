package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class RemovePeriodMutatorTest extends StudyCalendarTestCase {
    private RemovePeriodMutator mutator;

    private Amendment amendment;
    private Delta<?> delta;
    private Remove remove;

    private Arm arm;
    private Period period0, period1;
    private PlannedEvent p0e0, p0e1, p1e0, p1e1;

    private ScheduledCalendar scheduledCalendar;
    private ScheduledArm scheduledArm;
    private ScheduledEvent p0se0, p0se1, p1se0, p1se1;

    private PeriodDao periodDao;
    private TemplateService templateService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        arm = Epoch.create("E1", "A1").getArms().get(0);
        period0 = createPeriod("P0", 1, 9, 3);
        period0.addPlannedEvent(p0e0 = createPlannedEvent("P0.E0", 2));
        period0.addPlannedEvent(p0e1 = createPlannedEvent("P0.E1", 6));
        period1 = createPeriod("P1", 1, 11, 2);
        period1.addPlannedEvent(p1e0 = createPlannedEvent("P1.E0", 1));
        period1.addPlannedEvent(p1e1 = createPlannedEvent("P1.E1", 4));

        remove = Remove.create(period0);
        delta = Delta.createDeltaFor(arm, remove);
        amendment = createAmendments("Oops");
        amendment.setDate("09/22");
        amendment.addDelta(delta);
        scheduledCalendar = new ScheduledCalendar();
        scheduledArm = createScheduledArm(arm);
        scheduledArm.addEvent(p0se0 = createUnschedulableMockEvent(p0e0));
        scheduledArm.addEvent(p0se1 = createUnschedulableMockEvent(p0e1));
        scheduledArm.addEvent(p1se0 = createUnschedulableMockEvent(p1e0));
        scheduledArm.addEvent(p1se1 = createUnschedulableMockEvent(p1e1));

        periodDao = registerDaoMockFor(PeriodDao.class);
        templateService = registerMockFor(TemplateService.class);
        mutator = new RemovePeriodMutator(remove, periodDao, templateService);
    }

    public void testApplicableToLiveSchedules() throws Exception {
        assertTrue(mutator.appliesToExistingSchedules());
    }

    public void testDoesNothingWhenArmNotUsed() throws Exception {
        scheduledCalendar.addArm(createScheduledArm(createNamedInstance("Some other arm", Arm.class)));

        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
    }

    public void testUnschedulesScheduledEventsFromPeriodOnly() throws Exception {
        scheduledCalendar.addArm(scheduledArm);

        expect(templateService.findParent(p0e0)).andReturn(period0);
        expect(templateService.findParent(p0e1)).andReturn(period0);
        expect(templateService.findParent(p1e0)).andReturn(period1);
        expect(templateService.findParent(p1e1)).andReturn(period1);

        String expectedMessage = "Removed in revision Oops (09/22)";
        p0se0.unscheduleIfOutstanding(expectedMessage);
        p0se1.unscheduleIfOutstanding(expectedMessage);

        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
    }

    private ScheduledEvent createUnschedulableMockEvent(PlannedEvent event) throws NoSuchMethodException {
        ScheduledEvent semimock = registerMockFor(ScheduledEvent.class, 
            ScheduledEvent.class.getMethod("unscheduleIfOutstanding", String.class));
        semimock.setPlannedEvent(event);
        return semimock;
    }
}
